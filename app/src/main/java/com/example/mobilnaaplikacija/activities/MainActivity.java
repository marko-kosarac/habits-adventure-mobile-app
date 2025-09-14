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
                // vodi korisnika samo ako se aktivnost prvi put kreira
                navController.navigate(R.id.mainFragment);
            }
        }
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            listenForFriendRequests();
            listenForAllianceInvites();
            listenForAllianceAcceptancesForLeader();
            showPendingAllianceAcceptancesForLeader();
        }

        // Top-level destinacije (hamburger se prikazuje za ove)
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
                R.id.myAllianceFragment

        ).setOpenableLayout(drawer).build();

        // Poveži Toolbar i Drawer sa NavController-om
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Drawer toggle (opciono, ali sync-uje hamburger animaciju)
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
            }
            else if (id == R.id.nav_statistics){
                navController.navigate((R.id.statistics_page));
            }
            else if( id == R.id.nav_shop){
                navController.navigate((R.id.shopFragment));

            }
            else if(id == R.id.nav_friends){
                navController.navigate((R.id.friendsFragment));
            }
            else if(id == R.id.nav_alliance){
                navController.navigate((R.id.myAllianceFragment));
            }else if (id == R.id.nav_logout)
            {
                FirebaseAuth.getInstance().signOut(); // stvarni logout

//                navigationView.getMenu().clear();
//                navigationView.inflateMenu(R.menu.logged_out_drawer);
                View header_blanc = navigationView.getHeaderView(0);
                TextView name_blanc = header_blanc.findViewById(R.id.nav_header_name);
                ImageView avatar_blanc = header_blanc.findViewById(R.id.nav_header_avatar);
                name_blanc.setVisibility(View.GONE);
                avatar_blanc.setImageResource(R.drawable.blank_picture);
                restartApp();
                return true;

//                navController.navigate(R.id.homeFragment);
            }

            drawer.closeDrawers();
            return true;
        });
        hideSystemUI();
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
            // --- default za izlogovanog korisnika ---
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
            friendRequestListener.remove(); // ukloni prethodni listener ako postoji
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

                                // Dohvati username pošiljaoca
                                db.collection("users").document(fromUserId)
                                        .get()
                                        .addOnSuccessListener(doc -> {
                                            String fromUsername = doc.getString("username");

                                            // Prikaz dijaloga
                                            showFriendRequestDialog(fromUserId, requestId);

                                            // Prikaz lokalne notifikacije
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
                            String fromUserId = dc.getDocument().getString("fromUserId");
                            String allianceId = dc.getDocument().getString("allianceId");

                            // Prikaži dijalog za poziv
                            showAllianceInviteDialog(allianceId, fromUserId, dc.getDocument().getId());
                        }
                    }
                });
    }


    private void showAllianceAcceptanceNotification(String username) {
        String channelId = "alliance_notifications";
        String channelName = "Alliance Notifications";

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Novi član saveza")
                .setContentText(username + " je prihvatio poziv u savez!")
                .setSmallIcon(R.drawable.ic_alliance) // tvoja ikona
                .setAutoCancel(true)
                .build();

        try {
            notificationManager.notify((int) System.currentTimeMillis(), notification);
        } catch (Exception e) {
            Log.e("NotificationError", "Greška pri slanju notifikacije", e);
        }
    }
//    public void listenForAllianceNotifications() {
//        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//        // Prvo dohvatimo saveze gde je korisnik vođa
//        db.collection("alliances")
//                .whereEqualTo("leaderId", currentUserId)
//                .get()
//                .addOnSuccessListener(querySnapshot -> {
//                    for (DocumentSnapshot allianceDoc : querySnapshot.getDocuments()) {
//                        String allianceId = allianceDoc.getId();
//
//                        // Sada pratimo prihvaćene pozive za taj savez
//                        db.collection("alliance_invites")
//                                .whereEqualTo("allianceId", allianceId)
//                                .whereEqualTo("status", "accepted")
//                                .addSnapshotListener((snapshots, error) -> {
//                                    if (error != null || snapshots == null) return;
//
//                                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
//                                        if (dc.getType() == DocumentChange.Type.ADDED) {
//                                            String acceptedUserId = dc.getDocument().getString("toUserId");
//
//                                            // Ignoriši ako je vođa sam (svoj poziv ne prikazujemo)
//                                            if (acceptedUserId != null && !acceptedUserId.equals(currentUserId)) {
//                                                // Dohvati username korisnika koji je prihvatio
//                                                db.collection("users").document(acceptedUserId)
//                                                        .get()
//                                                        .addOnSuccessListener(userDoc -> {
//                                                            String username = userDoc.getString("username");
//                                                            if (username == null) username = "Korisnik";
//
//                                                            // Prikazi dijalog
//                                                            String finalUsername = username;
//                                                            runOnUiThread(() -> {
//                                                                new AlertDialog.Builder(MainActivity.this)
//                                                                        .setTitle("Novi član saveza")
//                                                                        .setMessage(finalUsername + " je prihvatio poziv u tvoj savez!")
//                                                                        .setPositiveButton("OK", null)
//                                                                        .setCancelable(true)
//                                                                        .show();
//                                                            });
//                                                        });
//                                            }
//                                        }
//                                    }
//                                });
//                    }
//                });
//    }

    public void showPendingAllianceAcceptancesForLeader() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Dohvati sve saveze gde je korisnik vođa
        db.collection("alliances")
                .whereEqualTo("leaderId", currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot allianceDoc : querySnapshot.getDocuments()) {
                        String allianceId = allianceDoc.getId();

                        // Dohvati sve pozive gde je status "accepted"
                        db.collection("alliance_invites")
                                .whereEqualTo("allianceId", allianceId)
                                .whereEqualTo("status", "accepted")
                                .get()
                                .addOnSuccessListener(acceptedSnapshots -> {
                                    for (DocumentSnapshot acceptedDoc : acceptedSnapshots.getDocuments()) {
                                        String acceptedUserId = acceptedDoc.getString("toUserId");
                                        if (acceptedUserId != null && !acceptedUserId.equals(currentUserId)) {
                                            // Dohvati username korisnika
                                            db.collection("users").document(acceptedUserId)
                                                    .get()
                                                    .addOnSuccessListener(userDoc -> {
                                                        String username = userDoc.getString("username");
                                                        if (username == null) username = "Korisnik";

                                                        // Prikaži dijalog
                                                        String finalUsername = username;
                                                        runOnUiThread(() -> {
                                                            new AlertDialog.Builder(MainActivity.this)
                                                                    .setTitle("Novi član saveza")
                                                                    .setMessage(finalUsername + " je prihvatio poziv u tvoj savez!")
                                                                    .setPositiveButton("OK", null)
                                                                    .setCancelable(true)
                                                                    .show();
                                                        });
                                                    });
                                        }
                                    }
                                });
                    }
                });
    }


    public void listenForAllianceAcceptancesForLeader() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Prvo dohvatimo sve saveze gde je korisnik vođa
        db.collection("alliances")
                .whereEqualTo("leaderId", currentUserId)
                .addSnapshotListener((alliancesSnapshots, error) -> {
                    if (error != null || alliancesSnapshots == null) return;

                    for (DocumentChange allianceChange : alliancesSnapshots.getDocumentChanges()) {
                        if (allianceChange.getType() == DocumentChange.Type.ADDED ||
                                allianceChange.getType() == DocumentChange.Type.MODIFIED) {

                            String allianceId = allianceChange.getDocument().getId();

                            // Listener za sve prihvaćene pozive u tom savezu
                            db.collection("alliance_invites")
                                    .whereEqualTo("allianceId", allianceId)
                                    .whereEqualTo("status", "accepted")
                                    .addSnapshotListener((snapshots, e) -> {
                                        if (e != null || snapshots == null) return;

                                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                                            // bilo da je novi ili vec postojeći accepted
                                            if (dc.getType() == DocumentChange.Type.ADDED ||
                                                    dc.getType() == DocumentChange.Type.MODIFIED) {

                                                String acceptedUserId = dc.getDocument().getString("toUserId");
                                                if (acceptedUserId != null && !acceptedUserId.equals(currentUserId)) {
                                                    db.collection("users").document(acceptedUserId)
                                                            .get()
                                                            .addOnSuccessListener(userDoc -> {
                                                                String username = userDoc.getString("username");
                                                                if (username == null) username = "Korisnik";

                                                                String finalUsername = username;
                                                                runOnUiThread(() -> {
                                                                    new AlertDialog.Builder(MainActivity.this)
                                                                            .setTitle("Novi član saveza")
                                                                            .setMessage(finalUsername + " je prihvatio poziv u tvoj savez!")
                                                                            .setPositiveButton("OK", null)
                                                                            .setCancelable(true)
                                                                            .show();
                                                                });
                                                            });
                                                }
                                            }
                                        }
                                    });
                        }
                    }
                });
    }





    private void showAllianceInviteDialog(String allianceId, String fromUserId, String inviteDocId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Dohvati username pošiljaoca i ime saveza
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

                                // Dijalog koji ne može da se zatvori dok korisnik ne odabere
                                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                builder.setTitle("Poziv u savez");
                                builder.setMessage("Korisnik " + finalFromUsername + " te pozvao u savez \"" + allianceName + "\".");
                                builder.setCancelable(false);

                                builder.setPositiveButton("Prihvati", (dialog, which) -> acceptAllianceInvite(allianceId, inviteDocId));
                                builder.setNegativeButton("Odbij", (dialog, which) -> declineAllianceInvite(inviteDocId));

                                builder.show();

                                // Lokalna notifikacija sa imenom saveza
                                showAllianceInviteNotification(finalFromUsername, allianceName, inviteDocId);
                            });
                });
    }


    private void showAllianceInviteNotification(String fromUsername, String allianceId, String inviteDocId) {
        String ALLIANCE_INVITE_CHANNEL_ID = "alliance_invite_channel";
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Dohvati ime saveza
        db.collection("alliances").document(allianceId).get().addOnSuccessListener(doc -> {
            String allianceName = "savez";
            if (doc.exists() && doc.getString("name") != null) {
                allianceName = doc.getString("name");
            }

            // Kreiraj kanal ako je potrebno
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

        // Dohvati trenutnog korisnika
        db.collection("users").document(currentUserId).get().addOnSuccessListener(userDoc -> {
            String currentAllianceId = userDoc.getString("currentAllianceId");

            if (currentAllianceId != null && !currentAllianceId.isEmpty()) {
                // Proveri da li prethodni savez postoji
                db.collection("alliances").document(currentAllianceId).get()
                        .addOnSuccessListener(prevAllianceDoc -> {
                            if (prevAllianceDoc.exists()) {
                                // Ukloni korisnika iz prethodnog saveza
                                db.collection("alliances").document(currentAllianceId)
                                        .update("members", FieldValue.arrayRemove(currentUserId));
                            } else {
                                Log.w("Alliance", "Prethodni savez ne postoji, preskačem remove");
                            }

                            // Sada dodaj korisnika u novi savez
                            addUserToAlliance(db, allianceId, currentUserId, inviteDocId);
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Alliance", "Greška pri proveri prethodnog saveza", e);
                            addUserToAlliance(db, allianceId, currentUserId, inviteDocId);
                        });
            } else {
                // Nema prethodnog saveza, direktno dodaj korisnika
                addUserToAlliance(db, allianceId, currentUserId, inviteDocId);
            }
        });
    }

    // Pomoćna funkcija za dodavanje korisnika u savez i slanje notifikacije
    private void addUserToAlliance(FirebaseFirestore db, String allianceId, String userId, String inviteDocId) {
        // Dodaj korisnika u savez (kreira dokument ako ne postoji)
        db.collection("alliances").document(allianceId)
                .set(new HashMap<String, Object>() {{
                    put("members", FieldValue.arrayUnion(userId));
                }}, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    // Ažuriraj korisnika da pokaže trenutni savez
                    db.collection("users").document(userId)
                            .update("currentAllianceId", allianceId);

                    // Promeni status poziva u accepted
                    db.collection("alliance_invites").document(inviteDocId)
                            .update("status", "accepted");

                    // Dohvati kreatora saveza da pošaljemo notifikaciju
                    db.collection("alliances").document(allianceId)
                            .get().addOnSuccessListener(allianceDoc -> {
                                if (allianceDoc.exists()) {
                                    String leaderId = allianceDoc.getString("leaderId");
                                    if (leaderId != null && !leaderId.equals(userId)) {
                                        // Pošalji lokalnu notifikaciju ili update za kreatora saveza
                                        sendAllianceAcceptedNotification(leaderId, userId);
                                    }
                                }
                            });

                    Toast.makeText(this, "Pridružio si se savezu!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Greška pri pridruživanju savezu!", Toast.LENGTH_SHORT).show();
                    Log.e("Alliance", "Neuspelo pridruživanje savezu", e);
                });
    }

    // Primer funkcije za notifikaciju kreatoru saveza
    private void sendAllianceAcceptedNotification(String leaderId, String newMemberId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(newMemberId)
                .get().addOnSuccessListener(newMemberDoc -> {
                    String newMemberName = newMemberDoc.getString("username");

                    // Ovde možeš dodati kod za Firestore "notifications" kolekciju ili push notifikaciju
                    // npr:
                    Map<String, Object> notification = new HashMap<>();
                    notification.put("type", "alliance_accepted");
                    notification.put("message", newMemberName + " je prihvatio poziv u tvoj savez!");
                    notification.put("timestamp", FieldValue.serverTimestamp());

                    db.collection("users").document(leaderId)
                            .collection("notifications")
                            .add(notification);
                });
    }


    private void declineAllianceInvite(String inviteDocId) {
        FirebaseFirestore.getInstance().collection("alliance_invites").document(inviteDocId)
                .update("status", "declined");

        Toast.makeText(this, "Odbio si poziv.", Toast.LENGTH_SHORT).show();
    }





    // Helper metoda za provere da li je app u foreground-u
    private boolean isAppInForeground() {
        ActivityManager.RunningAppProcessInfo appProcess = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(appProcess);
        return appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
    }

    // Dijalog za prihvatanje/odbijanje
    private void showFriendRequestDialog(String fromUserId, String requestId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(fromUserId)
                .get()
                .addOnSuccessListener(doc -> {
                    String fromUsername = doc.getString("username");

                    new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Novi zahtev za prijateljstvo")
                            .setMessage(fromUsername + " ti je poslao zahtev.")
                            .setPositiveButton("Prihvati", (dialog, which) -> acceptFriendRequest(fromUserId, requestId))
                            .setNegativeButton("Odbij", (dialog, which) -> declineFriendRequest(requestId))
                            .setCancelable(false)
                            .show();
                });
    }

    // Prihvatanje zahteva
    private void acceptFriendRequest(String fromUserId, String requestId) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(currentUserId)
                .update("friends", FieldValue.arrayUnion(fromUserId));
        db.collection("users").document(fromUserId)
                .update("friends", FieldValue.arrayUnion(currentUserId));

        db.collection("friend_requests").document(requestId)
                .update("status", "accepted");

        Toast.makeText(this, "Prihvatio si zahtev!", Toast.LENGTH_SHORT).show();
    }

    // Odbijanje zahteva
    private void declineFriendRequest(String requestId) {
        FirebaseFirestore.getInstance().collection("friend_requests")
                .document(requestId)
                .update("status", "declined");

        Toast.makeText(this, "Odbio si zahtev.", Toast.LENGTH_SHORT).show();
    }

    // Lokalna notifikacija
    private void showFriendRequestNotification(String fromUsername, String requestId) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification.Builder builder = new Notification.Builder(this, FRIEND_REQUEST_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_friend_request)
                .setContentTitle("Novi zahtev za prijateljstvo")
                .setContentText(fromUsername + " ti je poslao zahtev.")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH);

        if (notificationManager != null) {
            notificationManager.notify(requestId.hashCode(), builder.build());
        }
    }




}
