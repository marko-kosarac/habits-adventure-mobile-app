package com.example.mobilnaaplikacija.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.model.User;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder> {

    private Context context;
    private List<User> userList;
    private OnFriendClickListener listener;

    public interface OnFriendClickListener {
        void onAddFriend(User user, int position);
    }

    public UserListAdapter(Context context, List<User> userList, OnFriendClickListener listener) {
        this.context = context;
        this.userList = userList;
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

        // Postavljanje avatara po avatarId
        switch (user.getAvatarId()) {
            case 0: holder.userAvatar.setImageResource(R.drawable.avatar1); break;
            case 1: holder.userAvatar.setImageResource(R.drawable.avatar2); break;
            case 2: holder.userAvatar.setImageResource(R.drawable.avatar3); break;
            case 3: holder.userAvatar.setImageResource(R.drawable.avatar4); break;
            case 4: holder.userAvatar.setImageResource(R.drawable.avatar5); break;
            default: holder.userAvatar.setImageResource(R.drawable.avatar1); break;
        }

        // Ako je korisnik prijatelj, dugme se sakrije i prikaže TextView sa "Prijatelji"
        if (user.isFriend()) {
            holder.buttonAddFriend.setVisibility(View.GONE);
            holder.textFriend.setVisibility(View.VISIBLE);
        } else {
            holder.buttonAddFriend.setVisibility(View.VISIBLE);
            holder.textFriend.setVisibility(View.GONE);

            holder.buttonAddFriend.setOnClickListener(v -> {
                user.setFriend(true);
                notifyItemChanged(position); // osveži da se Button sakrije i TextView prikaže
                listener.onAddFriend(user, position);
            });
        }

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
            textFriend = itemView.findViewById(R.id.textFriend);
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
