package com.example.mobilnaaplikacija.fragments.alliance;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.adapters.AllianceChatAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllianceChatFragment extends Fragment {

    private RecyclerView recyclerMessages;
    private EditText editMessage;
    private ImageButton btnSend;
    private AllianceChatAdapter chatAdapter;
    private final List<Map<String, Object>> messageList = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String currentUserId;
    private String allianceId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alliance_chat, container, false);

        recyclerMessages = view.findViewById(R.id.recyclerMessages);
        editMessage = view.findViewById(R.id.editMessage);
        btnSend = view.findViewById(R.id.btnSend);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        allianceId = getArguments() != null ? getArguments().getString("allianceId") : null;

        chatAdapter = new AllianceChatAdapter(getContext(), messageList, currentUserId);
        recyclerMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerMessages.setAdapter(chatAdapter);

        if (allianceId != null) {
            loadMessages();
        } else {
            Toast.makeText(getContext(), "Alliance ID je null!", Toast.LENGTH_SHORT).show();
        }

        btnSend.setOnClickListener(v -> sendMessage());

        return view;
    }

    private void loadMessages() {
        db.collection("alliances").document(allianceId)
                .collection("messages")
                .orderBy("timestamp")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            Map<String, Object> message = dc.getDocument().getData();

                            // 🔥 Fix za timestamp
                            Object tsObj = message.get("timestamp");
                            if (tsObj instanceof com.google.firebase.Timestamp) {
                                message.put("timestamp", ((com.google.firebase.Timestamp) tsObj).toDate().getTime());
                            } else if (tsObj == null) {
                                message.put("timestamp", System.currentTimeMillis());
                            }

                            // 🔥 Fix za username
                            if (message.get("username") == null) {
                                message.put("username", "Nepoznat");
                            }

                            messageList.add(message);
                            chatAdapter.notifyItemInserted(messageList.size() - 1);
                            recyclerMessages.scrollToPosition(messageList.size() - 1);
                        }
                    }
                });
    }

    private void sendMessage() {
        String text = editMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(userDoc -> {
                    String username = userDoc.getString("username");
                    if (username == null) username = "Nepoznat";

                    Map<String, Object> message = new HashMap<>();
                    message.put("fromUserId", currentUserId);
                    message.put("username", username);
                    message.put("message", text);
                    message.put("timestamp", System.currentTimeMillis());

                    db.collection("alliances").document(allianceId)
                            .collection("messages")
                            .add(message)
                            .addOnSuccessListener(doc -> editMessage.setText(""))
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Greška pri slanju poruke", Toast.LENGTH_SHORT).show());
                });
    }

}
