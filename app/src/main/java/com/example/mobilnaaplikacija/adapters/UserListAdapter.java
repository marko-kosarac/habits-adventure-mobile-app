package com.example.mobilnaaplikacija.adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.model.User;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder> {

    private Context context;
    private List<User> userList;      // svi korisnici
    private List<User> friendsList;   // lista prijatelja
    private OnFriendClickListener listener;

    public interface OnFriendClickListener {
        void onAddFriend(User user, int position);
    }

    public UserListAdapter(Context context, List<User> userList, List<User> friendsList, OnFriendClickListener listener) {
        this.context = context;
        this.userList = userList;
        this.friendsList = friendsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.userName.setText(user.getUsername());

        // Postavljanje avatara
        switch (user.getAvatarId()) {
            case 0: holder.userAvatar.setImageResource(R.drawable.avatar1); break;
            case 1: holder.userAvatar.setImageResource(R.drawable.avatar2); break;
            case 2: holder.userAvatar.setImageResource(R.drawable.avatar3); break;
            case 3: holder.userAvatar.setImageResource(R.drawable.avatar4); break;
            case 4: holder.userAvatar.setImageResource(R.drawable.avatar5); break;
            default: holder.userAvatar.setImageResource(R.drawable.avatar1);
        }

        // Provera da li je korisnik već prijatelj
        boolean isFriend = false;
        for (User friend : friendsList) {
            if (friend.getId().equals(user.getId())) {
                isFriend = true;
                break;
            }
        }

        if (isFriend) {
            holder.buttonAddFriend.setVisibility(View.GONE);
            holder.textFriend.setVisibility(View.VISIBLE); // prikazi "Prijatelji"
        }else if (user.isRequestSent()) {
            holder.buttonAddFriend.setText("Poslato");
            holder.buttonAddFriend.setEnabled(false);
        }
            else {
            holder.buttonAddFriend.setVisibility(View.VISIBLE);
            holder.textFriend.setVisibility(View.GONE);
            holder.buttonAddFriend.setOnClickListener(v -> listener.onAddFriend(user, position));
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
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        CircleImageView userAvatar;
        TextView userName;
        Button buttonAddFriend;
        TextView textFriend;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userAvatar = itemView.findViewById(R.id.userAvatar);
            userName = itemView.findViewById(R.id.userName);
            buttonAddFriend = itemView.findViewById(R.id.buttonAddFriend);
            textFriend = itemView.findViewById(R.id.textFriend); // TextView koji prikazuje "Prijatelji"
        }
    }

    public void updateUser(int position, User user) {
        userList.set(position, user);
        notifyItemChanged(position);
    }

    public void setUsers(List<User> users) {
        this.userList = users;
        notifyDataSetChanged();
    }
}

