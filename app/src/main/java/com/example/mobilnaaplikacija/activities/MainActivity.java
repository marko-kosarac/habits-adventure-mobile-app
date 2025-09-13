package com.example.mobilnaaplikacija.activities;

import android.content.Intent;
import android.graphics.Color;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashSet;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private AppBarConfiguration appBarConfiguration;
    private NavController navController;
    private AppBarConfiguration mAppBarConfiguration;
    private Set<Integer> topLevelDestinations = new HashSet<>();
    private static final String FRIEND_REQUEST_CHANNEL_ID = "friend_request_channel";
    private ListenerRegistration friendRequestListener;


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
                R.id.friendProfileFragment

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
//        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
//            listenForFriendRequests();
//        }
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
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "Friend Requests";
            String description = "Obaveštenja o zahtevima za prijateljstvo";
            int importance = android.app.NotificationManager.IMPORTANCE_HIGH;
            android.app.NotificationChannel channel = new android.app.NotificationChannel(FRIEND_REQUEST_CHANNEL_ID, name, importance);
            channel.setDescription(description);

            android.app.NotificationManager notificationManager = getSystemService(android.app.NotificationManager.class);
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



    // Helper metoda za provere da li je app u foreground-u
    private boolean isAppInForeground() {
        android.app.ActivityManager.RunningAppProcessInfo appProcess = new android.app.ActivityManager.RunningAppProcessInfo();
        android.app.ActivityManager.getMyMemoryState(appProcess);
        return appProcess.importance == android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
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
        android.app.NotificationManager notificationManager =
                (android.app.NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        android.app.PendingIntent pendingIntent = android.app.PendingIntent.getActivity(
                this, 0, new Intent(this, MainActivity.class),
                android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE
        );

        android.app.Notification.Builder builder = new android.app.Notification.Builder(this, FRIEND_REQUEST_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_friend_request)
                .setContentTitle("Novi zahtev za prijateljstvo")
                .setContentText(fromUsername + " ti je poslao zahtev.")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(android.app.Notification.PRIORITY_HIGH);

        if (notificationManager != null) {
            notificationManager.notify(requestId.hashCode(), builder.build());
        }
    }




}
