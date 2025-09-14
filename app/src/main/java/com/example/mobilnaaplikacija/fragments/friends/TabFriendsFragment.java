package com.example.mobilnaaplikacija.fragments.friends;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

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

            List<User> selectedFriends = adapter.getSelectedFriends();
            Toast.makeText(getContext(), "Savez kreiran sa " + selectedFriends.size() + " prijatelja", Toast.LENGTH_SHORT).show();

            dialog.dismiss();
        });

        dialog.show();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (friendRequestListener != null) friendRequestListener.remove();
    }
}
