package com.example.mobilnaaplikacija.adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilnaaplikacija.R;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AllianceChatAdapter extends RecyclerView.Adapter<AllianceChatAdapter.ChatViewHolder> {

    private final List<Map<String, Object>> messageList;
    private final Context context;
    private final String currentUserId;

    public AllianceChatAdapter(Context context, List<Map<String, Object>> messageList, String currentUserId) {
        this.context = context;
        this.messageList = messageList;
        this.currentUserId = currentUserId != null ? currentUserId : FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Map<String, Object> message = messageList.get(position);
        String fromUserId = (String) message.get("fromUserId");
        String username = (String) message.get("username");
        String text = (String) message.get("message");
        long timestamp = (long) message.get("timestamp");

        holder.textUsername.setText(username);

        // Formatiranje vremena
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(new Date(timestamp));
        holder.textTimestamp.setText(time);

        holder.textMessage.setText(text);

        // Ovde menjamo i balon i poravnanje
        LinearLayout.LayoutParams messageParams =
                (LinearLayout.LayoutParams) holder.textMessage.getLayoutParams();
        LinearLayout.LayoutParams usernameParams =
                (LinearLayout.LayoutParams) holder.textUsername.getLayoutParams();
        LinearLayout.LayoutParams timestampParams =
                (LinearLayout.LayoutParams) holder.textTimestamp.getLayoutParams();

        if (fromUserId.equals(currentUserId)) {
            // Moje poruke -> desno
            holder.textMessage.setBackgroundResource(R.drawable.bg_message_right);

            messageParams.gravity = Gravity.END;
            usernameParams.gravity = Gravity.END;
            timestampParams.gravity = Gravity.END;
        } else {
            // Poruke drugih -> levo
            holder.textMessage.setBackgroundResource(R.drawable.bg_message_left);

            messageParams.gravity = Gravity.START;
            usernameParams.gravity = Gravity.START;
            timestampParams.gravity = Gravity.START;
        }

        holder.textMessage.setLayoutParams(messageParams);
        holder.textUsername.setLayoutParams(usernameParams);
        holder.textTimestamp.setLayoutParams(timestampParams);
    }


    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView textUsername, textMessage, textTimestamp;
        View messageRoot;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            textUsername = itemView.findViewById(R.id.textUsername);
            textMessage = itemView.findViewById(R.id.textMessage);
            textTimestamp = itemView.findViewById(R.id.textTimestamp);
            messageRoot = itemView.findViewById(R.id.message_root);
        }
    }
}
