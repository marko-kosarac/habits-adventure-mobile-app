package com.example.mobilnaaplikacija.services.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;

public class FriendRequestReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String fromUserId = intent.getStringExtra("fromUser");
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if ("ACCEPT".equals(action)) {
            // Dodaj prijatelja u listu
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currentUserId)
                    .update("friends", com.google.firebase.firestore.FieldValue.arrayUnion(fromUserId));

            Toast.makeText(context, "Prihvatio si zahtev!", Toast.LENGTH_SHORT).show();
        } else if ("DECLINE".equals(action)) {
            Toast.makeText(context, "Odbio si zahtev.", Toast.LENGTH_SHORT).show();
        }
    }
}
