package com.example.mobilnaaplikacija.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class UserService {
    private static final String TAG = "UserService";
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    public UserService() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public void registerUser(String email, String password, String username, int avatarId,
                             OnCompleteListener<AuthResult> listener) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(listener);
    }

    public void saveUserData(String uid, String email, String username, int avatarId) {
        DocumentReference userRef = db.collection("users").document(uid);

        Map<String, Object> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("username", username);
        userData.put("avatarId", avatarId);
        userData.put("level", 1);
        userData.put("title", "Početnik");
        userData.put("powerPoints", 0);
        userData.put("xp", 0);
        userData.put("coins", 0);
        userData.put("badges", new java.util.ArrayList<>());
        userData.put("inventory", new java.util.ArrayList<>());
        userData.put("isRegistrationConfirmed", false);

        userRef.set(userData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User profile saved successfully"))
                .addOnFailureListener(e -> Log.w(TAG, "Error saving user profile", e));
    }

    public void sendVerificationEmail(FirebaseUser user) {
        if (user != null) {
            user.sendEmailVerification()
                    .addOnSuccessListener(unused -> Log.d(TAG, "Verification email sent"))
                    .addOnFailureListener(e -> Log.w(TAG, "Failed to send verification email", e));
        }
    }
    public void login(String email, String password, Consumer<Boolean> consumer) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> consumer.accept(task.isSuccessful()));
    }

    public void logout() {
        auth.signOut();
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }
}
