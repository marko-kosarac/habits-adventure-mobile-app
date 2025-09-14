package com.example.mobilnaaplikacija.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilnaaplikacija.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllianceMemberAdapter extends RecyclerView.Adapter<AllianceMemberAdapter.MemberViewHolder> {

    private List<String> memberIds;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public AllianceMemberAdapter(List<String> memberIds) {
        this.memberIds = memberIds;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alliance_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        String memberId = memberIds.get(position);

        // Dohvati username i avatar člana
        db.collection("users").document(memberId)
                .get()
                .addOnSuccessListener(doc -> {
                    String username = doc.getString("username");
                    Long avatarId = doc.getLong("avatarId"); // pretpostavljamo da je spremljen kao Long

                    holder.textView.setText(username != null ? username : "Nepoznato");

                    // Postavi avatar prema avatarId
                    int avatarResId = R.drawable.avatar1; // default avatar
                    if (avatarId != null) {
                        switch (avatarId.intValue()) {
                            case 0: avatarResId = R.drawable.avatar1; break;
                            case 1: avatarResId = R.drawable.avatar2; break;
                            case 2: avatarResId = R.drawable.avatar3; break;
                            case 3: avatarResId = R.drawable.avatar4; break;
                            case 4: avatarResId = R.drawable.avatar5; break;
                        }
                    }
                    holder.avatarView.setImageResource(avatarResId);
                });
    }


    @Override
    public int getItemCount() {
        return memberIds.size();
    }

    public static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        CircleImageView avatarView;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textMemberName);
            avatarView = itemView.findViewById(R.id.imageMemberAvatar);
        }
    }
}
