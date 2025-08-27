package com.example.mobilnaaplikacija.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.mobilnaaplikacija.model.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.function.Consumer;

public class UserService {
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public UserService() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    // LOGIN
    public void login(String email, String password, Consumer<Boolean> consumer) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            consumer.accept(true);
                        } else {
                            consumer.accept(false); // nije verifikovan
                        }
                    } else {
                        consumer.accept(false);
                    }
                });
    }

    // REGISTRACIJA
    public void register(User user, String password, Consumer<Boolean> consumer) {
        auth.createUserWithEmailAndPassword(user.getEmail(), password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            // šaljemo email verifikaciju
                            firebaseUser.sendEmailVerification()
                                    .addOnCompleteListener(emailTask -> {
                                        if (emailTask.isSuccessful()) {
                                            // čuvamo podatke u Firestore tek posle uspešne registracije
                                            user.setId(firebaseUser.getUid());
                                            db.collection("users")
                                                    .document(firebaseUser.getUid())
                                                    .set(user)
                                                    .addOnSuccessListener(aVoid -> consumer.accept(true))
                                                    .addOnFailureListener(e -> consumer.accept(false));
                                        } else {
                                            consumer.accept(false);
                                        }
                                    });
                        } else {
                            consumer.accept(false);
                        }
                    } else {
                        consumer.accept(false);
                    }
                });
    }

    // LOGOUT
    public void logout() {
        auth.signOut();
    }

    // VRATI TRENUTNOG USERA IZ FIRESTORE
    public void getCurrentUser(Consumer<User> consumer) {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            consumer.accept(null);
            return;
        }

        db.collection("users").document(firebaseUser.getUid()).get()
                .addOnSuccessListener(snapshot -> {
                    User user = snapshot.toObject(User.class);
                    consumer.accept(user);
                })
                .addOnFailureListener(e -> consumer.accept(null));
    }
}
