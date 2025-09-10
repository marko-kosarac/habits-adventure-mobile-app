package com.example.mobilnaaplikacija.fragments;

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
import com.example.mobilnaaplikacija.adapters.UserListAdapter;
import com.example.mobilnaaplikacija.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class TabAllUsersFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserListAdapter adapter;
    private List<User> allUsersList = new ArrayList<>();
    private EditText searchInput;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab_all_users, container, false);

        recyclerView = view.findViewById(R.id.recyclerAllUsers);
        searchInput = view.findViewById(R.id.searchAllUsers);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UserListAdapter(getContext(), allUsersList, (user, position) -> addFriend(user, position));
        recyclerView.setAdapter(adapter);

        loadAllUsers();

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void loadAllUsers() {
        allUsersList.clear();
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    String uid = doc.getId();
                    if (uid.equals(currentUserId)) continue; // ne prikazuj samog sebe

                    String username = doc.getString("username");
                    Long avatarLong = doc.getLong("avatarId");
                    int avatarId = avatarLong != null ? avatarLong.intValue() : 0;

                    User user = new User();
                    user.setId(uid);
                    user.setUsername(username != null ? username : "Korisnik");
                    user.setAvatarId(avatarId);
                    user.setFriend(false); // default, kasnije se može proveriti iz liste prijatelja

                    allUsersList.add(user);
                }
                adapter.setUsers(allUsersList);
            } else {
                Toast.makeText(getContext(), "Greška pri učitavanju korisnika", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addFriend(User user, int position) {
//        // TODO: implement Firebase logiku za dodavanje prijatelja
//        user.setFriend(true);
//        adapter.updateUser(position, user);
//        Toast.makeText(getContext(), user.getUsername() + " je dodat kao prijatelj!", Toast.LENGTH_SHORT).show();
    }

    private void filterUsers(String query) {
//        List<User> filtered = new ArrayList<>();
//        for (User u : allUsersList) {
//            if (u.getUsername().toLowerCase().contains(query.toLowerCase())) filtered.add(u);
//        }
//        adapter.setUsers(filtered);
    }
}
