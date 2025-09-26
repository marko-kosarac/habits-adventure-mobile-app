package com.example.mobilnaaplikacija.fragments.alliance;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.adapters.AllianceFriendsAdapter;
import com.example.mobilnaaplikacija.adapters.AllianceMemberAdapter;
import com.example.mobilnaaplikacija.model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MyAllianceFragment extends Fragment {

    private TextView textAllianceName, textAllianceLeader, textMembersLabel;
    private Button btnDeleteAlliance, btnLeaveAlliance, btnAddMembers;
    private FloatingActionButton btnChat;
    private RecyclerView recyclerAllianceMembers;
    private AllianceMemberAdapter memberAdapter;
    private List<String> memberList = new ArrayList<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_alliance, container, false);

        textAllianceName = view.findViewById(R.id.textAllianceName);
        textAllianceLeader = view.findViewById(R.id.textAllianceLeader);
        recyclerAllianceMembers = view.findViewById(R.id.recyclerAllianceMembers);
        btnDeleteAlliance = view.findViewById(R.id.btnDeleteAlliance);
        btnLeaveAlliance = view.findViewById(R.id.btnLeaveAlliance);
        textMembersLabel = view.findViewById(R.id.textMembersLabel);
        btnChat = view.findViewById(R.id.fabOpenChat);
        btnAddMembers = view.findViewById(R.id.btnAddMembers);

        btnAddMembers.setOnClickListener(v -> showAddMembersDialog());

        recyclerAllianceMembers.setLayoutManager(new LinearLayoutManager(getContext()));
        memberAdapter = new AllianceMemberAdapter(memberList);
        recyclerAllianceMembers.setAdapter(memberAdapter);

        btnDeleteAlliance.setOnClickListener(v -> showDeleteConfirmation());
        btnLeaveAlliance.setOnClickListener(v -> showLeaveConfirmation());


        btnChat.setOnClickListener(v -> {
            db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .get()
                    .addOnSuccessListener(userDoc -> {
                        String allianceId = userDoc.getString("currentAllianceId");
                        if (allianceId != null && !allianceId.isEmpty()) {
                            Bundle bundle = new Bundle();
                            bundle.putString("allianceId", allianceId);

                            NavController navController = Navigation.findNavController(requireView());
                            navController.navigate(R.id.action_myAllianceFragment_to_allianceChatFragment, bundle);
                        } else {
                            Toast.makeText(getContext(), "Niste u savezu.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        loadAllianceData();

        return view;
    }

    private void showAddMembersDialog() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    String currentAllianceId = userDoc.getString("currentAllianceId");
                    if (currentAllianceId == null) return;

                    // 🔹 Prvo dohvatimo trenutni savez da uzmemo članove
                    db.collection("alliances").document(currentAllianceId)
                            .get()
                            .addOnSuccessListener(allianceDoc -> {
                                List<String> currentMembers = (List<String>) allianceDoc.get("members");
                                if (currentMembers == null) currentMembers = new ArrayList<>();

                                // 🔹 Dohvati sve pending pozive za ovaj savez
                                List<String> finalCurrentMembers = currentMembers;
                                db.collection("alliance_invites")
                                        .whereEqualTo("allianceId", currentAllianceId)
                                        .whereEqualTo("status", "pending")
                                        .get()
                                        .addOnSuccessListener(invitesSnapshot -> {

                                            Set<String> pendingUserIds = new HashSet<>();
                                            for (DocumentSnapshot inviteDoc : invitesSnapshot.getDocuments()) {
                                                String toUserId = inviteDoc.getString("toUserId");
                                                if (toUserId != null) pendingUserIds.add(toUserId);
                                            }

                                            // 🔹 Dohvati prijatelje trenutnog korisnika
                                            db.collection("users")
                                                    .whereArrayContains("friends", currentUserId)
                                                    .get()
                                                    .addOnSuccessListener(friendsSnapshot -> {

                                                        List<User> friendsNotInAlliance = new ArrayList<>();
                                                        List<com.google.android.gms.tasks.Task<DocumentSnapshot>> tasks = new ArrayList<>();

                                                        for (DocumentSnapshot doc : friendsSnapshot.getDocuments()) {
                                                            String friendId = doc.getId();
                                                            String username = doc.getString("username");
                                                            String friendAllianceId = doc.getString("currentAllianceId");

                                                            // ❌ preskoči ako je već član saveza
                                                            if (finalCurrentMembers.contains(friendId)) continue;

                                                            // ❌ preskoči ako već postoji pending zahtev
                                                            if (pendingUserIds.contains(friendId)) continue;

                                                            // ✔️ provera da li je vođa drugog saveza
                                                            tasks.add(db.collection("alliances")
                                                                    .document(friendAllianceId != null ? friendAllianceId : "nonexistent")
                                                                    .get()
                                                                    .continueWith(t -> {
                                                                        if (t.isSuccessful() && t.getResult() != null && t.getResult().exists()) {
                                                                            String leaderId = t.getResult().getString("leaderId");
                                                                            if (!friendId.equals(leaderId)) {
                                                                                friendsNotInAlliance.add(new User(friendId, username));
                                                                            }
                                                                        } else if (friendAllianceId == null || friendAllianceId.isEmpty()) {
                                                                            friendsNotInAlliance.add(new User(friendId, username));
                                                                        }
                                                                        return null;
                                                                    }));
                                                        }

                                                        com.google.android.gms.tasks.Tasks.whenAll(tasks)
                                                                .addOnSuccessListener(v -> {
                                                                    if (friendsNotInAlliance.isEmpty()) {
                                                                        Toast.makeText(getContext(), "Nema prijatelja za dodavanje.", Toast.LENGTH_SHORT).show();
                                                                        return;
                                                                    }

                                                                    // 🔹 Prikazi dijalog sa listom prijatelja
                                                                    AllianceFriendsAdapter adapter = new AllianceFriendsAdapter(friendsNotInAlliance);
                                                                    RecyclerView recyclerView = new RecyclerView(getContext());
                                                                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                                                                    recyclerView.setAdapter(adapter);

                                                                    AlertDialog dialog = new AlertDialog.Builder(getContext())
                                                                            .setTitle("Dodaj članove u savez")
                                                                            .setView(recyclerView)
                                                                            .setPositiveButton("Pošalji pozive", (d, which) -> {
                                                                                List<User> selectedFriends = adapter.getSelectedFriends();
                                                                                for (User friend : selectedFriends) {
                                                                                    sendAllianceInvite(friend.getId(), currentAllianceId, currentUserId);
                                                                                }
                                                                            })
                                                                            .setNegativeButton("Otkaži", null)
                                                                            .create();

                                                                    dialog.show();
                                                                });
                                                    });
                                        });
                            });
                });
    }




    private void showFriendsCheckboxDialog(List<String> friendIds, String allianceId) {
        String[] friendNames = new String[friendIds.size()];
        boolean[] checkedItems = new boolean[friendIds.size()];

        for (int i = 0; i < friendIds.size(); i++) {
            String friendId = friendIds.get(i);
            int finalI = i;
            db.collection("users").document(friendId).get().addOnSuccessListener(doc -> {
                String username = doc.getString("username");
                if (username != null) friendNames[finalI] = username;
            });
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Odaberite prijatelje")
                .setMultiChoiceItems(friendNames, checkedItems, (dialog, which, isChecked) -> checkedItems[which] = isChecked)
                .setPositiveButton("Pošalji pozive", (dialog, which) -> {
                    for (int i = 0; i < friendIds.size(); i++) {
                        if (checkedItems[i]) {
                            sendAllianceInvite(friendIds.get(i), allianceId, FirebaseAuth.getInstance().getCurrentUser().getUid());
                        }
                    }
                })
                .setNegativeButton("Otkaži", null)
                .show();
    }

    private void sendAllianceInvite(String toUserId, String allianceId, String fromUserId) {
        Map<String, Object> invite = new HashMap<>();
        invite.put("toUserId", toUserId);
        invite.put("fromUserId", fromUserId);
        invite.put("allianceId", allianceId);
        invite.put("status", "pending");
        invite.put("notificationSent", false);

        db.collection("alliance_invites").add(invite)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Poziv poslat!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Greška pri slanju poziva.", Toast.LENGTH_SHORT).show());
    }





    private void loadAllianceData() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    String allianceId = userDoc.getString("currentAllianceId");

                    if (allianceId != null && !allianceId.isEmpty()) {
                        db.collection("alliances").document(allianceId)
                                .get()
                                .addOnSuccessListener(allianceDoc -> {
                                    if (allianceDoc.exists()) {
                                        String allianceName = allianceDoc.getString("name");
                                        String leaderId = allianceDoc.getString("leaderId");
                                        List<String> members = (List<String>) allianceDoc.get("members");

                                        textAllianceName.setText(allianceName != null ? allianceName : "Savez");

                                        if (leaderId != null) {
                                            db.collection("users").document(leaderId)
                                                    .get()
                                                    .addOnSuccessListener(leaderDoc -> {
                                                        String leaderName = leaderDoc.getString("username");
                                                        textAllianceLeader.setText("Vođa: " + (leaderName != null ? leaderName : "Nepoznato"));
                                                    });
                                        }

                                        if (members != null) {
                                            memberList.clear();
                                            memberList.addAll(members);
                                            memberAdapter.notifyDataSetChanged();
                                        }
                                        textMembersLabel.setVisibility(View.VISIBLE);
                                        recyclerAllianceMembers.setVisibility(View.VISIBLE);
                                        btnChat.setVisibility(View.VISIBLE);

                                        // Prikaz dugmadi
                                        if (currentUserId.equals(leaderId)) {
                                            btnDeleteAlliance.setVisibility(View.VISIBLE);
                                            btnLeaveAlliance.setVisibility(View.GONE);
                                            btnAddMembers.setVisibility(View.VISIBLE);
                                        } else {
                                            btnDeleteAlliance.setVisibility(View.GONE);
                                            btnLeaveAlliance.setVisibility(View.VISIBLE);
                                            btnAddMembers.setVisibility(View.GONE);
                                        }

                                    } else {
                                        clearAllianceUI();
                                    }
                                });
                    } else {
                        clearAllianceUI();
                    }
                });
    }

    private void clearAllianceUI() {
        textAllianceName.setText("Trenutno niste član nijednog saveza");
        textAllianceLeader.setText("");
        recyclerAllianceMembers.setVisibility(View.GONE);
        btnDeleteAlliance.setVisibility(View.GONE);
        btnLeaveAlliance.setVisibility(View.GONE);
        textMembersLabel.setVisibility(View.GONE);
        memberList.clear();
        memberAdapter.notifyDataSetChanged();
    }

    private void showLeaveConfirmation() {
        new AlertDialog.Builder(getContext())
                .setTitle("Napusti savez")
                .setMessage("Da li ste sigurni da želite napustiti savez?")
                .setPositiveButton("Da", (dialog, which) -> leaveAlliance())
                .setNegativeButton("Ne", null)
                .show();
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(getContext())
                .setTitle("Obriši savez")
                .setMessage("Da li ste sigurni da želite obrisati savez?")
                .setPositiveButton("Da", (dialog, which) -> deleteAlliance())
                .setNegativeButton("Ne", null)
                .show();
    }

    private void leaveAlliance() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    String allianceId = userDoc.getString("currentAllianceId");
                    if (allianceId != null) {
                        db.collection("alliances").document(allianceId)
                                .update("members", FieldValue.arrayRemove(currentUserId))
                                .addOnSuccessListener(aVoid -> db.collection("users").document(currentUserId)
                                        .update("currentAllianceId", null)
                                        .addOnSuccessListener(aVoid1 -> {
                                            Toast.makeText(getContext(), "Napustili ste savez.", Toast.LENGTH_SHORT).show();
                                            loadAllianceData();
                                        }))
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Greška pri napuštanju saveza.", Toast.LENGTH_SHORT).show());
                    }
                });
    }

    private void deleteAlliance() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    String allianceId = userDoc.getString("currentAllianceId");
                    if (allianceId != null) {
                        db.collection("alliances").document(allianceId)
                                .get()
                                .addOnSuccessListener(allianceDoc -> {
                                    if (allianceDoc.exists()) {
                                        List<String> members = (List<String>) allianceDoc.get("members");
                                        if (members != null) {
                                            for (String memberId : members) {
                                                db.collection("users").document(memberId)
                                                        .update("currentAllianceId", null);
                                            }
                                        }

                                        db.collection("alliances").document(allianceId)
                                                .delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(getContext(), "Savez je uspešno obrisan.", Toast.LENGTH_SHORT).show();
                                                    loadAllianceData();
                                                })
                                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Greška pri brisanju saveza.", Toast.LENGTH_SHORT).show());
                                    }
                                });
                    }
                });
    }
}
