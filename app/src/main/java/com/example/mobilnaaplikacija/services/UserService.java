package com.example.mobilnaaplikacija.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.mobilnaaplikacija.model.Equipment;
import com.example.mobilnaaplikacija.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

                                    //Inicijalizacija etape za buduće zadatke
                                    createNewEtapa(uid);
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

                            FirebaseMessaging.getInstance().getToken()
                                    .addOnCompleteListener(tokenTask -> {
                                        if (tokenTask.isSuccessful()) {
                                            String token = tokenTask.getResult();
                                            FirebaseFirestore.getInstance()
                                                    .collection("users")
                                                    .document(firebaseUser.getUid())
                                                    .update("fcmToken", token)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Log.d("UserService", "FCM token snimljen.");
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.e("UserService", "Greška pri snimanju FCM tokena", e);
                                                    });
                                        } else {
                                            Log.e("UserService", "Nije moguće dobiti FCM token", tokenTask.getException());
                                        }
                                    });

                            callback.onComplete(true, "Uspešan login.");
                        } else {
                            callback.onComplete(false, "Verifikujte email pre logovanja.");
                        }
                    } else {
                        callback.onComplete(false, "Greška pri loginu.");
                    }
                });
    }

    public void updateActiveDaysOnTaskAction(String currentUserId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(currentUserId).get().addOnSuccessListener(userDoc -> {
            if (!userDoc.exists()) return;

            long now = System.currentTimeMillis();
            long lastActive = userDoc.contains("lastActive") ? userDoc.getLong("lastActive") : 0L;
            int activeDays = userDoc.contains("activeDays") ? userDoc.getLong("activeDays").intValue() : 0;

            long THIRTY_SECONDS = 30 * 1000; // 30 sekundi
            long TWO_THIRTY_SECONDS = 2 * THIRTY_SECONDS;

            if (now - lastActive >= THIRTY_SECONDS && now - lastActive < TWO_THIRTY_SECONDS) {
                activeDays += 1; // nastavlja streak
            } else if (now - lastActive >= TWO_THIRTY_SECONDS) {
                activeDays = 1; // resetuje streak
            } // else -> još uvek isti "dan", ne povećava se

            db.collection("users").document(currentUserId)
                    .update("activeDays", activeDays, "lastActive", now);
        });
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

    public void getUserLevel(String userId, OnLevelRetrievedCallback callback) {
        if (userId == null || userId.isEmpty()) {
            callback.onFailure("Neispravan ID korisnika.");
            return;
        }

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long levelValue = documentSnapshot.getLong("level");
                        if (levelValue != null) {
                            callback.onSuccess(levelValue.intValue());
                        } else {
                            callback.onFailure("Polje 'level' nije pronađeno.");
                        }
                    } else {
                        callback.onFailure("Korisnik ne postoji u bazi.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("UserService", "Greška pri dohvatanju levela", e);
                    callback.onFailure("Greška pri dohvatanju levela: " + e.getMessage());
                });
    }

    public interface OnLevelRetrievedCallback {
        void onSuccess(int level);
        void onFailure(String errorMessage);
    }

    public void addCoinsToUser(String userId, int coins, Runnable onSuccess, OnFailureListener onFailure) {
        DocumentReference userRef = db.collection("users").document(userId);

        db.runTransaction(transaction -> {
            DocumentReference docRef = db.collection("users").document(userId);
            DocumentSnapshot snapshot = transaction.get(docRef);
            Long currentCoins = snapshot.getLong("coins");
            if (currentCoins == null) currentCoins = 0L;
            transaction.update(docRef, "coins", currentCoins + coins);
            return null;
        }).addOnSuccessListener(aVoid -> {
            if (onSuccess != null) onSuccess.run();
        }).addOnFailureListener(e -> {
            if (onFailure != null) onFailure.onFailure(e);
        });
    }

    public void addEquipmentToUser(String userId, Equipment equipment,
                                   OnSuccessListener<Void> onSuccess,
                                   OnFailureListener onFailure) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(userId);

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot snapshot = transaction.get(userRef);
            List<Map<String, Object>> equipmentList =
                    (List<Map<String, Object>>) snapshot.get("equipment");

            if (equipmentList == null) {
                equipmentList = new ArrayList<>();
            }

            long eqId = equipment.getId();
            Map<String, Object> inactiveItem = null;

            //nadji postojecu opremu
            for (Map<String, Object> item : equipmentList) {
                Number idNum = (Number) item.get("id");
                if (idNum != null && idNum.longValue() == eqId) {
                    Boolean active = (Boolean) item.get("active");
                    if (active != null && !active) {
                        inactiveItem = item;
                        break;
                    }
                }
            }

            if (inactiveItem != null) {
                //povecaj qty
                int qty = ((Number) inactiveItem.get("quantity")).intValue();
                inactiveItem.put("quantity", qty + 1);
            } else {
                //dodaj neaktivnu
                Map<String, Object> newItem = new HashMap<>();
                newItem.put("id", eqId);
                newItem.put("name", equipment.getName());
                newItem.put("description", equipment.getDescription());
                newItem.put("type", equipment.getType().name());
                newItem.put("bonus", equipment.getBonus());
                newItem.put("duration", equipment.getDuration());
                newItem.put("price", equipment.getPrice());
                newItem.put("quantity", 1);
                newItem.put("active", false);
                equipmentList.add(newItem);
            }

            transaction.update(userRef, "equipment", equipmentList);
            return null;
        }).addOnSuccessListener(aVoid -> {
            if (onSuccess != null) onSuccess.onSuccess(null);
        }).addOnFailureListener(e -> {
            if (onFailure != null) onFailure.onFailure(e);
        });
    }


    private void createNewEtapa(String userId) {
        long now = System.currentTimeMillis();

        Map<String, Object> initialEtapa = new HashMap<>();
        initialEtapa.put("level", 1);
        initialEtapa.put("start", now);
        initialEtapa.put("end", null);
        initialEtapa.put("bossDefeated", false);
        initialEtapa.put("successRate", 0.0);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .set(Collections.singletonMap("etapa", initialEtapa), SetOptions.merge())
                .addOnSuccessListener(x -> Log.d("EtapaInit", "Initial etapa created for new user"))
                .addOnFailureListener(e -> Log.e("EtapaInit", "Failed to create etapa", e));
    }


}
