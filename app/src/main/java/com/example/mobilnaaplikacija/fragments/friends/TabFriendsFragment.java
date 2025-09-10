package com.example.mobilnaaplikacija.fragments.friends;

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
import com.example.mobilnaaplikacija.adapters.FriendListAdapter;
import com.example.mobilnaaplikacija.adapters.UserListAdapter;
import com.example.mobilnaaplikacija.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab_friends, container, false);

        recyclerView = view.findViewById(R.id.recyclerFriends);
        searchInput = view.findViewById(R.id.searchFriends);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<User> allUsersList = new ArrayList<>();
        adapter = new FriendListAdapter(getContext(), friendsList);
        recyclerView.setAdapter(adapter);

        loadFriends();

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

        // Prvo učitaj listu ID-jeva prijatelja trenutnog korisnika
        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(doc -> {
                    List<String> friendIds = (List<String>) doc.get("friends");
                    if (friendIds == null || friendIds.isEmpty()) {
                        adapter.setFriends(friendsList); // nema prijatelja
                        return;
                    }

                    // Sada učitaj podatke za svakog prijatelja
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

                                friendsList.add(user);
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
            if (u.getUsername().toLowerCase().contains(query.toLowerCase())) filtered.add(u);
        }
        adapter.setFriends(filtered);
    }
    public void addFriendToList(User user) {
        if (!friendsList.contains(user)) {
            friendsList.add(user);
            adapter.setFriends(friendsList);
        }
    }
}
