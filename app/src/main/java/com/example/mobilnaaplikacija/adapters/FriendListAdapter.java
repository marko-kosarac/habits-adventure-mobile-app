package com.example.mobilnaaplikacija.adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.fragments.friends.FriendProfileFragment;
import com.example.mobilnaaplikacija.model.User;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.FriendViewHolder> {

    private Context context;
    private List<User> friends;
    private UserListAdapter.OnFriendClickListener listener;

    public FriendListAdapter(Context context, List<User> friends) {
        this.context = context;
        this.friends = friends;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        User user = friends.get(position);
        holder.friendName.setText(user.getUsername());

        // postavljanje avatara
        switch (user.getAvatarId()) {
            case 0: holder.friendAvatar.setImageResource(R.drawable.avatar1); break;
            case 1: holder.friendAvatar.setImageResource(R.drawable.avatar2); break;
            case 2: holder.friendAvatar.setImageResource(R.drawable.avatar3); break;
            case 3: holder.friendAvatar.setImageResource(R.drawable.avatar4); break;
            case 4: holder.friendAvatar.setImageResource(R.drawable.avatar5); break;
            default: holder.friendAvatar.setImageResource(R.drawable.avatar1);
        }
        holder.itemView.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            Bundle args = new Bundle();
            args.putString("friendId", user.getId());
            navController.navigate(R.id.action_friendsFragment_to_friendProfileFragment, args);
        });
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder {
        CircleImageView friendAvatar;
        TextView friendName;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            friendAvatar = itemView.findViewById(R.id.friendAvatar);
            friendName = itemView.findViewById(R.id.friendName);
        }
    }

    public void setFriends(List<User> friends) {
        this.friends = friends;
        notifyDataSetChanged();
    }

}
