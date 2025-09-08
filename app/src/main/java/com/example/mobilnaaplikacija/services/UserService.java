package com.example.mobilnaaplikacija.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.mobilnaaplikacija.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserService {

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    public interface OnUserRegisterCallback {
        void onComplete(boolean success, String message);
    }

    public UserService() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Registracija korisnika
     */
    public void register(String email, String password, String username, int avatarId, OnUserRegisterCallback callback) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 1. Napravi korisnika u Auth
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser == null) {
                            callback.onComplete(false, "Greška: korisnik nije kreiran.");
                            return;
                        }

                        String uid = firebaseUser.getUid();

                        // 2. Kreiraj User objekat
                        User user = new User(uid, email, password, username, avatarId);

                        // 3. Snimi u Firestore sa uid kao ID dokumenta
                        db.collection("users").document(uid).set(user)
                                .addOnSuccessListener(aVoid -> {
                                    // 4. Pošalji email verifikaciju
                                    firebaseUser.sendEmailVerification()
                                            .addOnCompleteListener(emailTask -> {
                                                if (emailTask.isSuccessful()) {
                                                    callback.onComplete(true, "Registracija uspešna! Proverite email za verifikaciju.");
                                                } else {
                                                    callback.onComplete(false, "Korisnik snimljen, ali greška pri slanju verifikacionog emaila.");
                                                }
                                                // opcionalno odmah izloguj
                                                auth.signOut();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firestore", "Greška pri snimanju u bazu", e);
                                    callback.onComplete(false, "Greška pri snimanju u bazu: " + e.getMessage());
                                });

                    } else {
                        Log.e("Auth", "Greška pri registraciji", task.getException());
                        callback.onComplete(false, "Greška pri registraciji: " + task.getException().getMessage());
                    }
                });
    }


    /**
     * Login korisnika
     */
    public void login(String email, String password, OnUserRegisterCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null && firebaseUser.isEmailVerified()) {
                            callback.onComplete(true, "Uspešan login.");
                        } else {
                            callback.onComplete(false, "Verifikujte email pre logovanja.");
                        }
                    } else {
                        callback.onComplete(false, "Greška pri loginu.");
                    }
                });
    }

    /**
     * Logout
     */
    public void logout() {
        auth.signOut();
    }

    /**
     * Trenutni korisnik
     */
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    /**
     * Dohvatanje reference na dokument user-a u Firestore
     */
    public DocumentReference getUserDoc(String userId) {
        return db.collection("users").document(userId);
    }
}
