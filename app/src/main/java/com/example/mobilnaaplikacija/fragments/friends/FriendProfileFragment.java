package com.example.mobilnaaplikacija.fragments.friends;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.model.Equipment;
import com.example.mobilnaaplikacija.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FriendProfileFragment extends Fragment {

    private ImageView imageAvatar, qrCode;
    private TextView textUsername, textLevelTitle, textXP, textPP, textCoins;
    private TextView textCurrentLevel, textNextLevel;
    private ProgressBar levelProgressBar;
    private LinearLayout badgesContainer,activeContainer, inactiveContainer;
    private LinearLayout activeEquipmentContainer, inactiveEquipmentContainer;
    private Button buttonChangePassword;
    private List<Equipment> userEquipmentList;
    private String friendId;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private static final String ARG_FRIEND_ID = "friendId";

    public static FriendProfileFragment newInstance(String friendId) {
        FriendProfileFragment fragment = new FriendProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FRIEND_ID, friendId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            friendId = getArguments().getString(ARG_FRIEND_ID);
        }
        db = FirebaseFirestore.getInstance();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_profile, container, false);

        imageAvatar = view.findViewById(R.id.imageFriendAvatar);
        qrCode = view.findViewById(R.id.qrCodeFriend);
        textUsername = view.findViewById(R.id.textUsernameFriend);
        textLevelTitle = view.findViewById(R.id.textLevelTitleFriend);
        textXP = view.findViewById(R.id.textXPFriend);
        badgesContainer = view.findViewById(R.id.badgesContainerFriend);

        textCurrentLevel = view.findViewById(R.id.textCurrentLevelFriend);
        textNextLevel = view.findViewById(R.id.textNextLevelFriend);
        levelProgressBar = view.findViewById(R.id.levelProgressBarFriend);
        activeEquipmentContainer = view.findViewById(R.id.activeEquipmentContainerFriend);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loadUserData();
        loadUserEquipment();
        loadUserBadges();

        return view;
    }

    private void loadUserBadges() {
        db.collection("users").document(friendId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        List<Map<String, Object>> badges =
                                (List<Map<String, Object>>) document.get("badges");

                        if (badges != null && !badges.isEmpty()) {
                            showBadges(badges);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Greška pri učitavanju bedževa.", Toast.LENGTH_SHORT).show());
    }

    private void showBadges(List<Map<String, Object>> badges) {
        badgesContainer.removeAllViews();
        Context context = badgesContainer.getContext();

        for (Map<String, Object> badge : badges) {
            // Kreiramo LinearLayout za jedan bedž
            LinearLayout badgeLayout = new LinearLayout(context);
            badgeLayout.setOrientation(LinearLayout.VERTICAL);
            badgeLayout.setGravity(Gravity.CENTER_HORIZONTAL); // CENTRIRA IKONICU
            badgeLayout.setPadding(16, 8, 16, 8);

            float scale = context.getResources().getDisplayMetrics().density;
            int marginInDp = 16;
            int marginInPx = (int) (marginInDp * scale + 0.5f);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(0, 0, marginInPx, 0);
            badgeLayout.setLayoutParams(layoutParams);

            ImageView icon = new ImageView(context);
            int resId = context.getResources().getIdentifier(
                    badge.get("icon").toString(), "drawable", context.getPackageName());
            icon.setImageResource(resId);

            int sizeInDp = 80; // npr 80dp
            int sizeInPx = (int) (sizeInDp * scale + 0.5f);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(sizeInPx, sizeInPx);
            icon.setLayoutParams(iconParams);
            icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

            TextView name = new TextView(context);
            name.setText(badge.get("name").toString());
            name.setTextColor(Color.BLACK);
            name.setGravity(Gravity.CENTER);
            name.setTextSize(12);
            name.setTypeface(null, Typeface.BOLD);

            TextView info = new TextView(context);
            info.setText("Zadaci: " + badge.get("completedTasks"));
            info.setTextColor(Color.DKGRAY);
            info.setGravity(Gravity.CENTER);
            info.setTextSize(10);

            badgeLayout.addView(icon);
            badgeLayout.addView(name);
            badgeLayout.addView(info);

            badgesContainer.addView(badgeLayout);
        }
    }

    private void loadUserData() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("users").document(friendId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        bindFriendData(document, friendId);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Greška pri učitavanju profila.", Toast.LENGTH_SHORT).show());
    }

    private void bindFriendData(@NonNull DocumentSnapshot document, String userId) {
        String username = document.getString("username");
        long xp = document.getLong("experiencePoints") != null ? document.getLong("experiencePoints") : 0;
        long level = document.getLong("level") != null ? document.getLong("level") : 1;
        String title = document.getString("title");
        Long avatarId = document.getLong("avatarId");
        String email = document.getString("email");

        textUsername.setText(username);
        textLevelTitle.setText("Level " + level + " - " + (title != null ? title : "Početnik"));
        textXP.setText("XP: " + xp);

        if (avatarId != null) {
            switch (avatarId.intValue()) {
                case 0: imageAvatar.setImageResource(R.drawable.avatar1); break;
                case 1: imageAvatar.setImageResource(R.drawable.avatar2); break;
                case 2: imageAvatar.setImageResource(R.drawable.avatar3); break;
                case 3: imageAvatar.setImageResource(R.drawable.avatar4); break;
                case 4: imageAvatar.setImageResource(R.drawable.avatar5); break;
            }
        }
        updateLevelUI(xp, userId);
        generateQRCode(userId, username, email);
    }

    private void updateLevelUI(long currentXP, String userId) {
        int level = 1;
        long xpForNextLevel = 200;
        long prevXpForNext = 0;

        while (currentXP >= xpForNextLevel) {
            level++;
            prevXpForNext = xpForNextLevel;
            long next = xpForNextLevel * 2 + xpForNextLevel / 2;
            xpForNextLevel = ((next + 99) / 100) * 100;
        }

        textCurrentLevel.setText(String.valueOf(level));
        textNextLevel.setText(String.valueOf(level + 1));

        String title;
        switch (level) {
            case 1:
                title = "Početnik";
                break;
            case 2:
                title = "Učenik";
                break;
            case 3:
                title = "Iskusni";
                break;
            default:
                title = "Veteran";
                break;
        }
        textLevelTitle.setText("Level " + level + " - " + title);

        int progress = (int) ((currentXP * 100) / xpForNextLevel);
        levelProgressBar.setMax(100);
        levelProgressBar.setProgress(progress);
        textXP.setText("XP: " + currentXP + " / " + xpForNextLevel);

    }

    private void loadUserEquipment() {
        db.collection("users").document(friendId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        List<Map<String, Object>> equipmentData = (List<Map<String, Object>>) document.get("equipment");

                        userEquipmentList = new ArrayList<>();

                        if (equipmentData != null) {
                            for (Map<String, Object> data : equipmentData) {
                                Equipment eq = mapToEquipment(data);
                                userEquipmentList.add(eq);
                            }
                        }

                        displayEquipment();
                    }
                });
    }


    private Equipment mapToEquipment(Map<String, Object> data) {
        Equipment eq = new Equipment();
        eq.setId(((Number) data.get("id")).longValue());
        eq.setName((String) data.get("name"));
        eq.setDescription((String) data.get("description"));
        eq.setBonus((String) data.get("bonus"));
        eq.setDuration(((Number) data.get("duration")).intValue());
        eq.setPrice(((Number) data.get("price")).intValue());
        eq.setQuantity(((Number) data.get("quantity")).intValue());
        eq.setActive((Boolean) data.get("active"));
        eq.setCount(data.get("count") != null ? ((Number) data.get("count")).intValue() : 0);
        eq.setType(Equipment.Type.valueOf((String) data.get("type")));
        return eq;
    }

    private void displayEquipment() {
        activeEquipmentContainer.removeAllViews();

        if (userEquipmentList == null || userEquipmentList.isEmpty()) return;

        for (Equipment eq : userEquipmentList) {
            // Prikaži samo aktivnu opremu koja nije napitak ili oružje
            boolean isClothing = eq.getType() != null &&
                    !(eq.getType().toString().equals("NAPITAK") || eq.getType().toString().equals("ORUZJE"));

            if (eq.isActive() && isClothing) {
                View card = LayoutInflater.from(getContext()).inflate(R.layout.card_user_equipment, null);
                TextView name = card.findViewById(R.id.textEquipmentName);
                TextView desc = card.findViewById(R.id.textEquipmentDescription);
                TextView quantity = card.findViewById(R.id.textEquipmentQuantity);
                Button activateButton = card.findViewById(R.id.buttonActivateEquipment);

                name.setText(eq.getName());
                desc.setText(eq.getDescription());
                quantity.setText("Količina: " + eq.getQuantity());

                activateButton.setText("Aktivirana");
                activateButton.setEnabled(false);

                activeEquipmentContainer.addView(card);
            }
        }
    }


    private void generateQRCode(String userId, String username, String email) {
        MultiFormatWriter writer = new MultiFormatWriter();
        try {
            String qrContent = "username:" + username + ", email:" + email;
            BitMatrix matrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, 300, 300);
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.createBitmap(matrix);
            qrCode.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}