package com.example.mobilnaaplikacija.fragments;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobilnaaplikacija.R;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class UserProfileFragment extends Fragment {

    private ImageView imageAvatar, qrCode;
    private TextView textUsername, textLevelTitle, textXP, textPP, textCoins;
    private LinearLayout badgesContainer;
    private Button buttonChangePassword;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public UserProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        imageAvatar = view.findViewById(R.id.imageAvatar);
        qrCode = view.findViewById(R.id.qrCode);
        textUsername = view.findViewById(R.id.textUsername);
        textLevelTitle = view.findViewById(R.id.textLevelTitle);
        textXP = view.findViewById(R.id.textXP);
        textPP = view.findViewById(R.id.textPP);
        textCoins = view.findViewById(R.id.textCoins);
        badgesContainer = view.findViewById(R.id.badgesContainer);
        buttonChangePassword = view.findViewById(R.id.buttonChangePassword);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loadUserData();

        return view;
    }

    private void loadUserData() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        bindUserData(document, userId);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Greška pri učitavanju profila.", Toast.LENGTH_SHORT).show());
    }

    private void bindUserData(@NonNull DocumentSnapshot document, String userId) {
        String username = document.getString("username");
        long xp = document.getLong("xp") != null ? document.getLong("xp") : 0;
        long pp = document.getLong("pp") != null ? document.getLong("pp") : 0;
        long coins = document.getLong("coins") != null ? document.getLong("coins") : 0;
        long level = document.getLong("level") != null ? document.getLong("level") : 1;
        String title = document.getString("title");
        Long avatarId = document.getLong("avatarId");
        String email = document.getString("email");

        textUsername.setText(username);
        textLevelTitle.setText("Level " + level + " - " + (title != null ? title : "Početnik"));
        textXP.setText("XP: " + xp);
        textPP.setText("PP: " + pp);
        textCoins.setText("Novčići: " + coins);

        if (avatarId != null) {
            switch (avatarId.intValue()) {
                case 0: imageAvatar.setImageResource(R.drawable.avatar1); break;
                case 1: imageAvatar.setImageResource(R.drawable.avatar2); break;
                case 2: imageAvatar.setImageResource(R.drawable.avatar3); break;
                case 3: imageAvatar.setImageResource(R.drawable.avatar4); break;
                case 4: imageAvatar.setImageResource(R.drawable.avatar5); break;
            }
        }

        generateQRCode(userId, username, email);
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
