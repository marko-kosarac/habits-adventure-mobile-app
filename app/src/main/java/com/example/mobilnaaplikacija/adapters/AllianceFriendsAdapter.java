package com.example.mobilnaaplikacija.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.model.User;

import java.util.ArrayList;
import java.util.List;

public class AllianceFriendsAdapter extends RecyclerView.Adapter<AllianceFriendsAdapter.ViewHolder> {

    private final List<User> friends;
    private final List<User> selectedFriends = new ArrayList<>();

    public AllianceFriendsAdapter(List<User> friends) {
        this.friends = friends;
    }

    public List<User> getSelectedFriends() {
        return selectedFriends;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend_checkbox, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User friend = friends.get(position);
        holder.checkBox.setText(friend.getUsername());
        holder.checkBox.setChecked(selectedFriends.contains(friend));

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedFriends.contains(friend)) selectedFriends.add(friend);
            } else {
                selectedFriends.remove(friend);
            }
        });
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        ViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkboxFriend);
        }
    }
}

