package com.example.mobilnaaplikacija.fragments.friends;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.activities.QrScanActivity;
import com.example.mobilnaaplikacija.adapters.UserListAdapter;
import com.example.mobilnaaplikacija.model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TabAllUsersFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserListAdapter adapter;
    private List<User> allUsersList = new ArrayList<>();
    private List<User> friendsList = new ArrayList<>();
    private EditText searchInput;
    private FloatingActionButton fabScanQr;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private static final int QR_SCAN_REQUEST_CODE = 1001;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab_all_users, container, false);
        fabScanQr = view.findViewById(R.id.fabScanQr);
        recyclerView = view.findViewById(R.id.recyclerAllUsers);
        searchInput = view.findViewById(R.id.searchAllUsers);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UserListAdapter(getContext(), allUsersList, friendsList, (user, position) -> {
            if (!user.isFriend() && !user.isRequestSent()) {
                sendFriendRequest(user);
            }
        });        recyclerView.setAdapter(adapter);


        loadFriendsAndUsers();

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        fabScanQr.setOnClickListener(v -> startQrScanner());

        return view;
    }

    private void startQrScanner() {
        Intent intent = new Intent(getActivity(), QrScanActivity.class); // tvoja aktivnost za skener
        startActivityForResult(intent, QR_SCAN_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == QR_SCAN_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            String scannedData = data.getStringExtra("scanned_data");
            handleQrResult(scannedData);
        }
    }

    private void handleQrResult(String scannedData) {
        if (scannedData != null && scannedData.startsWith("userId:")) {
            String scannedUserId = scannedData.substring(7);

            if (scannedUserId.equals(currentUserId)) {
                Toast.makeText(getContext(), "Ne možeš dodati sebe", Toast.LENGTH_SHORT).show();
                return;
            }

            // Dodaj u bazu prijatelja oba korisnika
            db.collection("users").document(currentUserId)
                    .update("friends", FieldValue.arrayUnion(scannedUserId));
            db.collection("users").document(scannedUserId)
                    .update("friends", FieldValue.arrayUnion(currentUserId));

            // Dodaj u UI listu ako već nije
            User newFriend = new User();
            newFriend.setId(scannedUserId);
            newFriend.setUsername("Korisnik"); // možeš kasnije dohvatiti pravo ime iz Firestore
            newFriend.setFriend(true);

            if (friendsList.stream().noneMatch(u -> u.getId().equals(newFriend.getId()))) {
                friendsList.add(newFriend);
            }

            Toast.makeText(getContext(), "Korisnik dodat u prijatelje!", Toast.LENGTH_SHORT).show();
            adapter.setUsers(allUsersList); // refresh adaptera
        } else {
            Toast.makeText(getContext(), "QR kod nije validan", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadFriendsAndUsers() {
        friendsList.clear();

        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(doc -> {
                    List<String> friendIds = (List<String>) doc.get("friends");
                    if (friendIds != null) {
                        for (String fid : friendIds) {
                            User friend = new User();
                            friend.setId(fid);
                            friendsList.add(friend);
                        }
                    }

                    loadAllUsersWithFriends();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Greška pri učitavanju prijatelja", Toast.LENGTH_SHORT).show();
                    loadAllUsersWithFriends(); // i dalje učitaj sve korisnike
                });
    }

    private void loadAllUsersWithFriends() {
        allUsersList.clear();
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    String uid = doc.getId();
                    if (uid.equals(currentUserId)) continue;

                    String username = doc.getString("username");
                    Long avatarLong = doc.getLong("avatarId");
                    int avatarId = avatarLong != null ? avatarLong.intValue() : 0;

                    User user = new User();
                    user.setId(uid);
                    user.setUsername(username != null ? username : "Korisnik");
                    user.setAvatarId(avatarId);

                    boolean isAlreadyFriend = false;
                    for (User friend : friendsList) {
                        if (friend.getId().equals(uid)) {
                            isAlreadyFriend = true;
                            break;
                        }
                    }
                    user.setFriend(isAlreadyFriend);

                    allUsersList.add(user);
                }
                adapter.setUsers(allUsersList);

                // 🔹 dodatno obeleži kome je već poslat zahtev
                markPendingRequests();
            } else {
                Toast.makeText(getContext(), "Greška pri učitavanju korisnika", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void markPendingRequests() {
        db.collection("friend_requests")
                .whereEqualTo("fromUserId", currentUserId)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(query -> {
                    if (query != null && !query.isEmpty()) {
                        for (QueryDocumentSnapshot doc : query) {
                            String toUserId = doc.getString("toUserId");
                            for (User u : allUsersList) {
                                if (u.getId().equals(toUserId)) {
                                    u.setRequestSent(true);
                                    break;
                                }
                            }
                        }
                        adapter.setUsers(allUsersList); // osveži UI
                    }
                });
    }


    private void sendFriendRequest(User user) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> request = new HashMap<>();
        request.put("fromUserId", currentUserId);
        request.put("toUserId", user.getId());
        request.put("status", "pending");
        request.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance()
                .collection("friend_requests")
                .add(request)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(getContext(), "Zahtev poslat korisniku " + user.getUsername(), Toast.LENGTH_SHORT).show();

                    // 🔹 odmah promeni status u UI
                    user.setRequestSent(true);
                    adapter.updateUser(allUsersList.indexOf(user), user);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Greška pri slanju zahteva", Toast.LENGTH_SHORT).show();
                });
    }



    private void filterUsers(String query) {
        List<User> filtered = new ArrayList<>();
        for (User u : allUsersList) {
            if (u.getUsername().toLowerCase().contains(query.toLowerCase())) filtered.add(u);
        }
        adapter.setUsers(filtered);
    }
}
