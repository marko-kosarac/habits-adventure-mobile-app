package com.example.mobilnaaplikacija.activities;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private AppBarConfiguration appBarConfiguration;
    private NavController navController;
    private AppBarConfiguration mAppBarConfiguration;
    private Set<Integer> topLevelDestinations = new HashSet<>();
    private final Set<String> processedNotifications = new HashSet<>();
    private static final String FRIEND_REQUEST_CHANNEL_ID = "friend_request_channel";
    private ListenerRegistration friendRequestListener, allianceInviteListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.appBarMain.toolbar;
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        updateDrawerHeader();

        topLevelDestinations.add(R.id.action_settings);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            navigationView.inflateMenu(R.menu.logged_out_drawer);
            navController.navigate(R.id.homeFragment);
        } else {
            navigationView.inflateMenu(R.menu.main_drawer);
            if (savedInstanceState == null) {
                navController.navigate(R.id.mainFragment);
            }
        }

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            listenForFriendRequests();
            listenForAllianceInvites();
            listenForNotifications();
        }

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.homeFragment,
                R.id.mainFragment,
                R.id.nav_profile,
                R.id.profile_page,
                R.id.nav_register,
                R.id.statistics_page,
                R.id.shopFragment,
                R.id.friendsFragment,
                R.id.friendProfileFragment,
                R.id.myAllianceFragment,
                R.id.allianceChatFragment
        ).setOpenableLayout(drawer).build();

        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        createNotificationChannel();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                navController.navigate(R.id.mainFragment);
            } else if (id == R.id.nav_profile) {
                navController.navigate(R.id.profile_page);
            } else if (id == R.id.nav_statistics){
                navController.navigate(R.id.statistics_page);
            } else if( id == R.id.nav_shop){
                navController.navigate(R.id.shopFragment);
            } else if(id == R.id.nav_friends){
                navController.navigate(R.id.friendsFragment);
            } else if(id == R.id.nav_alliance){
                navController.navigate(R.id.myAllianceFragment);
            } else if (id == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();

                View header_blanc = navigationView.getHeaderView(0);
                TextView name_blanc = header_blanc.findViewById(R.id.nav_header_name);
                ImageView avatar_blanc = header_blanc.findViewById(R.id.nav_header_avatar);
                name_blanc.setVisibility(View.GONE);
                avatar_blanc.setImageResource(R.drawable.blank_picture);
                restartApp();
                return true;
            }

            drawer.closeDrawers();
            return true;
        });
//        hideSystemUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDrawerHeader();
    }

    private void restartApp() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    public void updateDrawerHeader() {
        NavigationView navigationView = binding.navView;
        View headerView = navigationView.getHeaderView(0);
        CircleImageView avatarView = headerView.findViewById(R.id.nav_header_avatar);
        TextView nameView = headerView.findViewById(R.id.nav_header_name);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            Long avatarId = documentSnapshot.getLong("avatarId");

                            nameView.setVisibility(View.VISIBLE);
                            nameView.setText(username != null ? username : "Korisnik");

                            int avatarResId = R.drawable.avatar1; // default
                            if (avatarId != null) {
                                switch (avatarId.intValue()) {
                                    case 0: avatarResId = R.drawable.avatar1; break;
                                    case 1: avatarResId = R.drawable.avatar2; break;
                                    case 2: avatarResId = R.drawable.avatar3; break;
                                    case 3: avatarResId = R.drawable.avatar4; break;
                                    case 4: avatarResId = R.drawable.avatar5; break;
                                }
                            }
                            avatarView.setImageResource(avatarResId);
                        }
                    })
                    .addOnFailureListener(e -> Log.e("DrawerHeader", "Greška pri učitavanju headera", e));
        } else {
            nameView.setVisibility(View.GONE);
            avatarView.setImageResource(R.drawable.blank_picture);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void setMainDrawer() {
        NavigationView navView = findViewById(R.id.nav_view);
        navView.getMenu().clear();
        navView.inflateMenu(R.menu.main_drawer);
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (friendRequestListener != null) {
            friendRequestListener.remove();
        }
        if (allianceInviteListener != null) {
            allianceInviteListener.remove();
        }

    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Friend Requests";
            String description = "Obaveštenja o zahtevima za prijateljstvo";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(FRIEND_REQUEST_CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public void listenForFriendRequests() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (friendRequestListener != null) {
            friendRequestListener.remove();
        }

        friendRequestListener = db.collection("friend_requests")
                .whereEqualTo("toUserId", currentUserId)
                .whereEqualTo("status", "pending")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    if (value != null && !value.isEmpty()) {
                        for (DocumentChange dc : value.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                String requestId = dc.getDocument().getId();
                                String fromUserId = dc.getDocument().getString("fromUserId");

                                db.collection("users").document(fromUserId)
                                        .get()
                                        .addOnSuccessListener(doc -> {
                                            String fromUsername = doc.getString("username");
                                            showFriendRequestDialog(fromUserId, requestId);
                                            showFriendRequestNotification(fromUsername, requestId);
                                        });
                            }
                        }
                    }
                });
    }

    public void listenForAllianceInvites() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        allianceInviteListener = db.collection("alliance_invites")
                .whereEqualTo("toUserId", currentUserId)
                .whereEqualTo("status", "pending")
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    for (DocumentChange dc : value.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            DocumentSnapshot doc = dc.getDocument();
                            Boolean notificationSent = doc.getBoolean("notificationSent");
                            if (notificationSent != null && notificationSent) continue;

                            String fromUserId = doc.getString("fromUserId");
                            String allianceId = doc.getString("allianceId");
                            String inviteDocId = doc.getId();

                            db.collection("alliance_invites").document(inviteDocId)
                                    .update("notificationSent", true);

                            showAllianceInviteDialog(allianceId, fromUserId, inviteDocId);
                        }
                    }
                });
    }

    public void listenForNotifications() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("notifications")
                .whereEqualTo("toUserId", currentUserId)
                .whereEqualTo("seen", false)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            DocumentSnapshot doc = dc.getDocument();
                            String docId = doc.getId();

                            if (processedNotifications.contains(docId)) continue;
                            processedNotifications.add(docId);

                            String message = doc.getString("message");

                            if (message != null) {
                                runOnUiThread(() -> {
                                    if (!isFinishing() && !isDestroyed()) {
                                        new AlertDialog.Builder(this)
                                                .setTitle("Obaveštenje o savezu")
                                                .setMessage(message)
                                                .setPositiveButton("OK", (dialog, which) -> {
                                                    doc.getReference().update("seen", true);
                                                })
                                                .show();

                                        showAllianceAcceptanceNotificationSafe(message);
                                    }
                                });
                            }
                        }
                    }
                });
    }

    private void showAllianceAcceptanceNotificationSafe(String message) {
        String channelId = "alliance_channel";
        String channelName = "Alliance Notifications";

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, channelName, NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle("Novi član u savezu")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }


    private void showAllianceInviteDialog(String allianceId, String fromUserId, String inviteDocId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(fromUserId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    String fromUsername = userDoc.getString("username");
                    if (fromUsername == null) fromUsername = "Korisnik";

                    String finalFromUsername = fromUsername;
                    db.collection("alliances").document(allianceId)
                            .get()
                            .addOnSuccessListener(allianceDoc -> {
                                String allianceName = allianceDoc.getString("name");
                                if (allianceName == null) allianceName = "savez";

                                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                builder.setTitle("Poziv u savez");
                                builder.setMessage("Korisnik " + finalFromUsername + " te pozvao u savez \"" + allianceName + "\".");
                                builder.setCancelable(false);

                                builder.setPositiveButton("Prihvati", (dialog, which) -> acceptAllianceInvite(allianceId, inviteDocId));
                                builder.setNegativeButton("Odbij", (dialog, which) -> declineAllianceInvite(inviteDocId));

                                builder.show();

                                showAllianceInviteNotification(finalFromUsername, allianceId, inviteDocId);
                            });
                });
    }

    private void showAllianceInviteNotification(String fromUsername, String allianceId, String inviteDocId) {
        String ALLIANCE_INVITE_CHANNEL_ID = "alliance_invite_channel";
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("alliances").document(allianceId).get().addOnSuccessListener(doc -> {
            String allianceName = "savez";
            if (doc.exists() && doc.getString("name") != null) {
                allianceName = doc.getString("name");
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        ALLIANCE_INVITE_CHANNEL_ID,
                        "Alliance Invites",
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("Obaveštenja o pozivima u saveze");
                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                }
            }

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this, 0, new Intent(this, MainActivity.class),
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            Notification.Builder builder = new Notification.Builder(this, ALLIANCE_INVITE_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_alliance)
                    .setContentTitle("Poziv u savez")
                    .setContentText(fromUsername + " te je pozvao u savez \"" + allianceName + "\".")
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setPriority(Notification.PRIORITY_HIGH);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            if (notificationManager != null) {
                notificationManager.notify(inviteDocId.hashCode(), builder.build());
            }
        });
    }

    private void acceptAllianceInvite(String allianceId, String inviteDocId) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(currentUserId).get().addOnSuccessListener(userDoc -> {
            String currentAllianceId = userDoc.getString("currentAllianceId");

            if (currentAllianceId != null && !currentAllianceId.isEmpty()) {
                db.collection("alliances").document(currentAllianceId).get()
                        .addOnSuccessListener(prevAllianceDoc -> {
                            if (prevAllianceDoc.exists()) {
                                db.collection("alliances").document(currentAllianceId)
                                        .update("members", FieldValue.arrayRemove(currentUserId));
                            }
                            addUserToAlliance(db, allianceId, currentUserId, inviteDocId);
                        })
                        .addOnFailureListener(e -> addUserToAlliance(db, allianceId, currentUserId, inviteDocId));
            } else {
                addUserToAlliance(db, allianceId, currentUserId, inviteDocId);
            }
        });
    }

    private void addUserToAlliance(FirebaseFirestore db, String allianceId, String userId, String inviteDocId) {
        DocumentReference allianceRef = db.collection("alliances").document(allianceId);
        DocumentReference userRef = db.collection("users").document(userId);
        DocumentReference inviteRef = db.collection("alliance_invites").document(inviteDocId);

        allianceRef.update("members", FieldValue.arrayUnion(userId))
                .addOnSuccessListener(aVoid -> {
                    userRef.update("currentAllianceId", allianceId)
                            .addOnSuccessListener(v -> {
                                Map<String, Object> inviteUpdates = new HashMap<>();
                                inviteUpdates.put("status", "accepted");
                                inviteUpdates.put("notificationSent", false); // za eventualne dodatne listener-e
                                inviteRef.update(inviteUpdates)
                                        .addOnSuccessListener(done -> {
                                            Log.d("Alliance", "Korisnik " + userId + " dodat u savez " + allianceId);

                                            sendAllianceAcceptedNotification(allianceRef, userId, allianceId);
                                            Toast.makeText(
                                                    getApplicationContext(),
                                                    "Uspešno ste pristupili savezu!",
                                                    Toast.LENGTH_SHORT
                                            ).show();
                                        });
                            })
                            .addOnFailureListener(e -> Log.e("Alliance", "Greška pri update-u korisnika", e));
                })
                .addOnFailureListener(e -> Log.e("Alliance", "Greška pri dodavanju u savez", e));
    }




    private void sendAllianceAcceptedNotification(DocumentReference allianceRef, String newMemberId, String allianceId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        allianceRef.get().addOnSuccessListener(allianceDoc -> {
            if (!allianceDoc.exists()) return;
            String leaderId = allianceDoc.getString("leaderId");
            if (leaderId == null || leaderId.isEmpty()) return;

            db.collection("users").document(newMemberId).get().addOnSuccessListener(newMemberDoc -> {
                String newMemberName = newMemberDoc.getString("username");
                if (newMemberName == null) newMemberName = "Korisnik";

                // Kreiraj dokument notifikacije za lidera
                Map<String, Object> notification = new HashMap<>();
                notification.put("toUserId", leaderId);
                notification.put("type", "alliance_invite_accepted");
                notification.put("fromUserId", newMemberId);
                notification.put("allianceId", allianceId);
                notification.put("message", newMemberName + " je prihvatio poziv u savez!");
                notification.put("timestamp", System.currentTimeMillis());
                notification.put("seen", false);          // OBAVEZNO: lider još nije video
                notification.put("notificationSent", false); // OBAVEZNO: nije poslato UI-u

                db.collection("notifications").add(notification);
            });
        });
    }



    private void declineAllianceInvite(String inviteDocId) {
        FirebaseFirestore.getInstance().collection("alliance_invites")
                .document(inviteDocId)
                .update("status", "declined");
    }

    private void showFriendRequestDialog(String fromUserId, String requestId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Zahtev za prijateljstvo");
        builder.setMessage("Korisnik te je poslao zahtev za prijateljstvo.");
        builder.setCancelable(false);

        builder.setPositiveButton("Prihvati", (dialog, which) -> acceptFriendRequest(fromUserId, requestId));
        builder.setNegativeButton("Odbij", (dialog, which) -> declineFriendRequest(requestId));

        builder.show();
    }

    private void showFriendRequestNotification(String fromUsername, String requestId) {
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, FRIEND_REQUEST_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_friend_request)
                .setContentTitle("Zahtev za prijateljstvo")
                .setContentText(fromUsername + " ti je poslao zahtev za prijateljstvo.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(requestId.hashCode(), builder.build());
        }
    }

    private void acceptFriendRequest(String fromUserId, String requestId) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("friend_requests").document(requestId)
                .update("status", "accepted")
                .addOnSuccessListener(aVoid -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("friendId", fromUserId);
                    db.collection("users").document(currentUserId)
                            .collection("friends").document(fromUserId).set(userMap, SetOptions.merge());
                    db.collection("users").document(fromUserId)
                            .collection("friends").document(currentUserId).set(userMap, SetOptions.merge());
                });
    }

    private void declineFriendRequest(String requestId) {
        FirebaseFirestore.getInstance().collection("friend_requests")
                .document(requestId)
                .update("status", "declined");
    }
}
