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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllianceFriendsAdapter extends RecyclerView.Adapter<AllianceFriendsAdapter.ViewHolder> {

    private final List<User> friends;
    private final List<User> selectedFriends = new ArrayList<>();
    private Map<String, Boolean> selectedMap = new HashMap<>();

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

        holder.checkBox.setOnCheckedChangeListener(null); // ukloni prethodni listener
        boolean isChecked = selectedMap.getOrDefault(friend.getId(), false);
        holder.checkBox.setChecked(isChecked);

        holder.checkBox.setOnCheckedChangeListener((buttonView, checked) -> {
            selectedMap.put(friend.getId(), checked);
            if (checked) {
                if (!selectedFriends.contains(friend)) selectedFriends.add(friend);
            } else {
                selectedFriends.remove(friend);
            }
        });
    }


    public List<String> getSelectedFriendIds() {
        List<String> selectedIds = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : selectedMap.entrySet()) {
            if (entry.getValue()) {
                selectedIds.add(entry.getKey());
            }
        }
        return selectedIds;
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

