package com.example.mobilnaaplikacija.fragments.friends;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.adapters.AllianceFriendsAdapter;
import com.example.mobilnaaplikacija.adapters.FriendListAdapter;
import com.example.mobilnaaplikacija.model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TabFriendsFragment extends Fragment {

    private RecyclerView recyclerView;
    private FriendListAdapter adapter;
    private List<User> friendsList = new ArrayList<>();
    private EditText searchInput;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private ListenerRegistration friendRequestListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab_friends, container, false);

        recyclerView = view.findViewById(R.id.recyclerFriends);
        searchInput = view.findViewById(R.id.searchFriends);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FriendListAdapter(getContext(), friendsList);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fabCreate = view.findViewById(R.id.fabCreateAlliance);
        fabCreate.setOnClickListener(v -> showCreateAllianceDialog());

        loadFriends(); // učitaj sve postojeće prijatelje
        listenForFriendAcceptances(); // osluškuj nove u real-time

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFriends(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void loadFriends() {
        friendsList.clear();

        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(doc -> {
                    List<String> friendIds = (List<String>) doc.get("friends");
                    if (friendIds == null || friendIds.isEmpty()) {
                        adapter.setFriends(friendsList);
                        return;
                    }

                    db.collection("users").get().addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String uid = document.getId();
                                if (!friendIds.contains(uid)) continue;

                                String username = document.getString("username");
                                Long avatarLong = document.getLong("avatarId");
                                int avatarId = avatarLong != null ? avatarLong.intValue() : 0;

                                User user = new User();
                                user.setId(uid);
                                user.setUsername(username != null ? username : "Korisnik");
                                user.setAvatarId(avatarId);
                                user.setFriend(true);

                                // ⚡ Dodaj samo ako već nije u listi
                                if (friendsList.stream().noneMatch(u -> u.getId().equals(uid))) {
                                    friendsList.add(user);
                                }
                            }
                            adapter.setFriends(friendsList);
                        } else {
                            Toast.makeText(getContext(), "Greška pri učitavanju prijatelja", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Greška pri učitavanju prijatelja", Toast.LENGTH_SHORT).show());
    }

    private void filterFriends(String query) {
        List<User> filtered = new ArrayList<>();
        for (User u : friendsList) {
            if (u.getUsername().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(u);
            }
        }
        adapter.setFriends(filtered);
    }

    public void addFriendToList(User user) {
        if (friendsList.stream().noneMatch(u -> u.getId().equals(user.getId()))) {
            friendsList.add(user);
            adapter.setFriends(friendsList);
        }
    }

    private void listenForFriendAcceptances() {
        friendRequestListener = db.collection("friend_requests")
                .whereEqualTo("toUserId", currentUserId)
                .whereEqualTo("status", "accepted")
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    for (DocumentChange dc : value.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            String fromUserId = dc.getDocument().getString("fromUserId");

                            // ⚡ Ako već postoji, preskoči
                            if (friendsList.stream().anyMatch(u -> u.getId().equals(fromUserId))) {
                                continue;
                            }

                            db.collection("users").document(fromUserId).get()
                                    .addOnSuccessListener(doc -> {
                                        String username = doc.getString("username");
                                        Long avatarLong = doc.getLong("avatarId");
                                        int avatarId = avatarLong != null ? avatarLong.intValue() : 0;

                                        User newFriend = new User();
                                        newFriend.setId(fromUserId);
                                        newFriend.setUsername(username != null ? username : "Korisnik");
                                        newFriend.setAvatarId(avatarId);
                                        newFriend.setFriend(true);

                                        addFriendToList(newFriend);
                                    });
                        }
                    }
                });
    }

    private void showCreateAllianceDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_create_alliance, null);
        EditText editAllianceName = dialogView.findViewById(R.id.editAllianceName);
        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerAllianceFriends);
        Button buttonCreate = dialogView.findViewById(R.id.buttonCreateAlliance);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // učitaj prijatelje iz friendsList
        AllianceFriendsAdapter adapter = new AllianceFriendsAdapter(friendsList);
        recyclerView.setAdapter(adapter);

        buttonCreate.setOnClickListener(v -> {
            String allianceName = editAllianceName.getText().toString().trim();
            if (allianceName.isEmpty()) {
                Toast.makeText(getContext(), "Unesite naziv saveza", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> selectedFriendIds = adapter.getSelectedFriendIds();
            createAllianceAndSendInvites(allianceName, selectedFriendIds);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void createAllianceAndSendInvites(String allianceName, List<String> inviteUserIds) {
        String creatorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Proveri da li korisnik već ima savez
        db.collection("users").document(creatorId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    String currentAllianceId = userDoc.getString("currentAllianceId");

                    if (currentAllianceId != null && !currentAllianceId.isEmpty()) {
                        // Dohvati prethodni savez da proverimo da li je vođa
                        db.collection("alliances").document(currentAllianceId)
                                .get()
                                .addOnSuccessListener(prevAllianceDoc -> {
                                    if (prevAllianceDoc.exists()) {
                                        String leaderId = prevAllianceDoc.getString("leaderId");

                                        if (creatorId.equals(leaderId)) {
                                            // Ako je vođa, ne može kreirati novi savez
                                            new AlertDialog.Builder(getContext())
                                                    .setTitle("Ne možete kreirati novi savez")
                                                    .setMessage("Prvo morate ukinuti prethodni savez pre nego što kreirate novi.")
                                                    .setPositiveButton("OK", null)
                                                    .show();
                                        } else {
                                            // Ako nije vođa, napušta prethodni savez sa potvrdom
                                            showLeavePreviousAllianceDialog(currentAllianceId, allianceName, inviteUserIds);
                                        }
                                    } else {
                                        // Prethodni savez ne postoji, može kreirati novi
                                        actuallyCreateAlliance(allianceName, creatorId, inviteUserIds);
                                    }
                                });
                    } else {
                        // Nema prethodnog saveza, može kreirati novi
                        actuallyCreateAlliance(allianceName, creatorId, inviteUserIds);
                    }
                });
    }

    // Dijalog za potvrdu napuštanja prethodnog saveza
    private void showLeavePreviousAllianceDialog(String previousAllianceId, String newAllianceName, List<String> inviteUserIds) {
        new AlertDialog.Builder(getContext())
                .setTitle("Napuštanje prethodnog saveza")
                .setMessage("Automatski ćete napustiti prethodni savez da biste kreirali novi. Da li želite da nastavite?")
                .setPositiveButton("Da", (dialog, which) -> leaveAlliance(previousAllianceId, () ->
                        actuallyCreateAlliance(newAllianceName, FirebaseAuth.getInstance().getCurrentUser().getUid(), inviteUserIds)))
                .setNegativeButton("Ne", null)
                .show();
    }

    // Funkcija za kreiranje saveza i slanje poziva
    private void actuallyCreateAlliance(String allianceName, String creatorId, List<String> inviteUserIds) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String allianceId = db.collection("alliances").document().getId();

        Map<String, Object> allianceData = new HashMap<>();
        allianceData.put("name", allianceName);
        allianceData.put("leaderId", creatorId);
        allianceData.put("members", FieldValue.arrayUnion(creatorId));

        db.collection("alliances").document(allianceId)
                .set(allianceData)
                .addOnSuccessListener(aVoid -> {
                    // Ažuriraj kreatora
                    db.collection("users").document(creatorId)
                            .update("currentAllianceId", allianceId);

                    // Kreiraj pozive za sve inviteUserIds sa proverom
                    for (String userId : inviteUserIds) {
                        db.collection("users").document(userId).get()
                                .addOnSuccessListener(userDoc -> {
                                    String userAllianceId = userDoc.getString("currentAllianceId");

                                    if (userAllianceId != null && !userAllianceId.isEmpty()) {
                                        // Dohvati savez korisnika
                                        db.collection("alliances").document(userAllianceId)
                                                .get().addOnSuccessListener(userAllianceDoc -> {
                                                    String leaderId = userAllianceDoc.getString("leaderId");

                                                    if (userId.equals(leaderId)) {
                                                        // Korisnik je vođa drugog saveza → prikazi toast i ne šalji poziv
                                                        String username = userDoc.getString("username");
                                                        Toast.makeText(getContext(),
                                                                (username != null ? username : "Korisnik") +
                                                                        " je vođa drugog saveza i ne može biti pozvan.",
                                                                Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        // Može primiti poziv
                                                        sendAllianceInvite(userId, allianceId, creatorId);
                                                    }
                                                });
                                    } else {
                                        // Korisnik nema savez → šalje se poziv
                                        sendAllianceInvite(userId, allianceId, creatorId);
                                    }
                                });
                    }

                    Toast.makeText(getContext(), "Savez kreiran i pozivi poslati!", Toast.LENGTH_SHORT).show();

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Greška pri kreiranju saveza!", Toast.LENGTH_SHORT).show();
                    Log.e("Alliance", "Neuspelo kreiranje saveza", e);
                });
    }

    private void sendAllianceInvite(String toUserId, String allianceId, String creatorId) {
        Map<String, Object> inviteData = new HashMap<>();
        inviteData.put("fromUserId", creatorId);
        inviteData.put("toUserId", toUserId);
        inviteData.put("allianceId", allianceId);
        inviteData.put("status", "pending");

        FirebaseFirestore.getInstance().collection("alliance_invites").add(inviteData);
    }


    private void leaveAlliance(String allianceId, Runnable onComplete) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("alliances").document(allianceId)
                .update("members", FieldValue.arrayRemove(currentUserId))
                .addOnSuccessListener(aVoid -> {
                    db.collection("users").document(currentUserId)
                            .update("currentAllianceId", null)
                            .addOnSuccessListener(aVoid2 -> onComplete.run());
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Greška pri napuštanju prethodnog saveza.", Toast.LENGTH_SHORT).show());
    }

    private void listenForAllianceInviteAcceptances(String allianceId) {
        String creatorId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("alliance_invites")
                .whereEqualTo("allianceId", allianceId)
                .whereEqualTo("status", "accepted")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED || dc.getType() == DocumentChange.Type.MODIFIED) {
                            String acceptedUserId = dc.getDocument().getString("toUserId");

                            if (acceptedUserId != null && !acceptedUserId.equals(creatorId)) {
                                // Dohvati korisnika koji je prihvatio poziv
                                FirebaseFirestore.getInstance().collection("users").document(acceptedUserId)
                                        .get().addOnSuccessListener(userDoc -> {
                                            String username = userDoc.getString("username");

                                            // Sistemska notifikacija za vođu
                                            showAllianceAcceptanceNotification(username != null ? username : "Korisnik");
                                        });
                            }
                        }
                    }
                });
    }


    private void showAllianceAcceptanceNotification(String username) {
        String channelId = "alliance_notifications";
        String channelName = "Alliance Notifications";

        NotificationManager notificationManager =
                (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        // Kreiraj kanal samo jednom (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(getContext(), channelId)
                .setContentTitle("Novi član saveza")
                .setContentText(username + " je prihvatio poziv u savez!")
                .setSmallIcon(R.drawable.ic_alliance) // zameni sa svojom ikonoom iz res/drawable
                .setAutoCancel(true)
                .build();

        notificationManager.notify((int) System.currentTimeMillis(), notification);
    }




    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (friendRequestListener != null) friendRequestListener.remove();
    }
}
