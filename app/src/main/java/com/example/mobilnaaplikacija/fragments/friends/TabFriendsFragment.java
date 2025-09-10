package com.example.mobilnaaplikacija.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

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

import java.util.ArrayList;
import java.util.List;

public class TabFriendsFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserListAdapter adapter;
    private List<User> friendsList = new ArrayList<>();
    private EditText searchInput;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab_friends, container, false);

        recyclerView = view.findViewById(R.id.recyclerFriends);
        searchInput = view.findViewById(R.id.searchFriends);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UserListAdapter(getContext(), friendsList, (user, position) -> {});
        recyclerView.setAdapter(adapter);

        loadFriends();

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFriends(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void loadFriends() {

    }

    private void filterFriends(String query) {
        List<User> filtered = new ArrayList<>();
        for (User u : friendsList) {
            if (u.getUsername().toLowerCase().contains(query.toLowerCase())) filtered.add(u);
        }
        adapter.setUsers(filtered);
    }
}
