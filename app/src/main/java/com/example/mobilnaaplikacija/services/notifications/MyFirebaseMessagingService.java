package com.example.mobilnaaplikacija.services.notifications;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.mobilnaaplikacija.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.firestore.FirebaseFirestore;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Novi FCM token: " + token);

        // Snimi token u Firestore za ulogovanog korisnika
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .update("fcmToken", token)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Token snimljen u Firestore"))
                    .addOnFailureListener(e -> Log.e(TAG, "Greška pri snimanju tokena", e));
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData().size() > 0) {
            String type = remoteMessage.getData().get("type");

            if ("FRIEND_REQUEST".equals(type)) {
                showFriendRequestNotification(remoteMessage);
            }
        }
    }

    private void showFriendRequestNotification(RemoteMessage remoteMessage) {
        String fromUser = remoteMessage.getData().get("fromUser");

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // Provera permisije za Android 13+
        boolean canNotify = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            canNotify = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }

        if (!canNotify) {
            // Ako nema permisiju, pošalji korisnika u settings
            Intent intent = new Intent();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
            } else {
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return; // ne prikazuj notifikaciju dok permisija nije data
        }

        // Kreiraj Accept/Decline PendingIntent-e
        Intent acceptIntent = new Intent(this, FriendRequestReceiver.class);
        acceptIntent.setAction("ACCEPT");
        acceptIntent.putExtra("fromUser", fromUser);

        Intent declineIntent = new Intent(this, FriendRequestReceiver.class);
        declineIntent.setAction("DECLINE");
        declineIntent.putExtra("fromUser", fromUser);

        PendingIntent acceptPendingIntent = PendingIntent.getBroadcast(
                this, 0, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        PendingIntent declinePendingIntent = PendingIntent.getBroadcast(
                this, 1, declineIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Kreiranje notifikacije
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "friend_requests")
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle("Novi zahtev za prijateljstvo")
                .setContentText(fromUser + " ti je poslao zahtev za prijateljstvo")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .addAction(R.drawable.ic_check, "Accept", acceptPendingIntent)
                .addAction(R.drawable.ic_close, "Decline", declinePendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(1001, builder.build());
    }


}
