package com.example.mobilnaaplikacija.fragments.user;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.model.Equipment;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class UserProfileFragment extends Fragment {

    private ImageView imageAvatar, qrCode;
    private TextView textUsername, textLevelTitle, textXP, textPP, textCoins;
    private TextView textCurrentLevel, textNextLevel;
    private ProgressBar levelProgressBar;
    private LinearLayout badgesContainer,activeContainer, inactiveContainer;
    private LinearLayout activeEquipmentContainer, inactiveEquipmentContainer;
    private Button buttonChangePassword;
    private List<Equipment> userEquipmentList;
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

        textCurrentLevel = view.findViewById(R.id.textCurrentLevel);
        textNextLevel = view.findViewById(R.id.textNextLevel);
        levelProgressBar = view.findViewById(R.id.levelProgressBar);
        activeEquipmentContainer = view.findViewById(R.id.activeEquipmentContainer);
        inactiveEquipmentContainer = view.findViewById(R.id.inactiveEquipmentContainer);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loadUserData();
        loadUserEquipment();

        buttonChangePassword.setOnClickListener(v -> showChangePasswordDialog());

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

    private void loadUserEquipment() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        List<Map<String, Object>> equipmentData = (List<Map<String, Object>>) document.get("equipment");

                        userEquipmentList = new ArrayList<>();

                        if (equipmentData != null) {  // ✅ zaštita od null-a
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
        eq.setType(Equipment.Type.valueOf((String) data.get("type")));
        return eq;
    }

    private void displayEquipment() {
        activeEquipmentContainer.removeAllViews();
        inactiveEquipmentContainer.removeAllViews();

        if (userEquipmentList == null || userEquipmentList.isEmpty()) return;

        for (Equipment eq : userEquipmentList) {
            View card = LayoutInflater.from(getContext()).inflate(R.layout.card_user_equipment, null);
            TextView name = card.findViewById(R.id.textEquipmentName);
            TextView desc = card.findViewById(R.id.textEquipmentDescription);
            TextView quantity = card.findViewById(R.id.textEquipmentQuantity);
            Button activateButton = card.findViewById(R.id.buttonActivateEquipment);

            name.setText(eq.getName());
            desc.setText(eq.getDescription());
            quantity.setText("Količina: " + eq.getQuantity());

            if (eq.isActive()) {
                activateButton.setText("Aktivirana");
                activateButton.setEnabled(false);
                activeEquipmentContainer.addView(card);
            } else {
                activateButton.setText("Aktiviraj");
                activateButton.setEnabled(true);
                inactiveEquipmentContainer.addView(card);

                activateButton.setOnClickListener(v -> {
                    activateEquipment(eq, card);
                });
            }
        }
    }


    private void activateEquipment(Equipment eq, View cardView) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(userId);

        // Smanji quantity u objektu
        eq.setQuantity(eq.getQuantity() - 1);

        // Proveri da li ista oprema već postoji u aktivnim
        Equipment activeEq = null;
        for (Equipment e : userEquipmentList) {
            if (e.isActive() && e.getId() == eq.getId()) {
                activeEq = e;
                break;
            }
        }

        if (activeEq != null) {
            // Ako postoji, samo povećaj quantity
            activeEq.setQuantity(activeEq.getQuantity() + 1);
        } else {
            // Ako ne postoji, kreiraj novu aktivnu instancu
            activeEq = new Equipment();
            activeEq.setId(eq.getId());
            activeEq.setName(eq.getName());
            activeEq.setDescription(eq.getDescription());
            activeEq.setBonus(eq.getBonus());
            activeEq.setDuration(eq.getDuration());
            activeEq.setPrice(eq.getPrice());
            activeEq.setQuantity(1);
            activeEq.setType(eq.getType());
            activeEq.setActive(true);
            userEquipmentList.add(activeEq);
        }

        // Ažuriraj Firestore
        Equipment finalActiveEq = activeEq;
        userRef.get().addOnSuccessListener(doc -> {
            List<Map<String, Object>> equipmentData = (List<Map<String, Object>>) doc.get("equipment");
            if (equipmentData == null) equipmentData = new ArrayList<>();
            List<Map<String, Object>> updatedList = new ArrayList<>();

            // Update quantity u inventaru i dodaj aktivne
            for (Map<String, Object> e : equipmentData) {
                long id = ((Number) e.get("id")).longValue();
                int qty = ((Number) e.get("quantity")).intValue();

                if (id == eq.getId()) {
                    qty = qty - 1; // smanji quantity u inventaru
                }

                if (qty > 0) {
                    e.put("quantity", qty);
                    updatedList.add(e);
                }
            }

            // Dodaj ili ažuriraj aktivnu opremu
            boolean found = false;
            for (Map<String, Object> e : updatedList) {
                if (((Number) e.get("id")).longValue() == finalActiveEq.getId() && (Boolean) e.get("active")) {
                    e.put("quantity", finalActiveEq.getQuantity());
                    found = true;
                    break;
                }
            }
            if (!found) {
                Map<String, Object> activeMap = new HashMap<>();
                activeMap.put("id", finalActiveEq.getId());
                activeMap.put("name", finalActiveEq.getName());
                activeMap.put("description", finalActiveEq.getDescription());
                activeMap.put("bonus", finalActiveEq.getBonus());
                activeMap.put("duration", finalActiveEq.getDuration());
                activeMap.put("price", finalActiveEq.getPrice());
                activeMap.put("quantity", finalActiveEq.getQuantity());
                activeMap.put("type", finalActiveEq.getType().name());
                activeMap.put("active", true);
                updatedList.add(activeMap);
            }

            userRef.update("equipment", updatedList).addOnSuccessListener(aVoid -> {
                loadUserEquipment(); // osveži prikaz
                Toast.makeText(getContext(), eq.getName() + " je aktivirana!", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void bindUserData(@NonNull DocumentSnapshot document, String userId) {
        String username = document.getString("username");
        long xp = document.getLong("experiencePoints") != null ? document.getLong("experiencePoints") : 0;
        long pp = document.getLong("powerPoints") != null ? document.getLong("powerPoints") : 0;
        long coins = document.getLong("coins") != null ? document.getLong("coins") : 0;
        long level = document.getLong("level") != null ? document.getLong("level") : 1;
        String title = document.getString("title");
        Long avatarId = document.getLong("avatarId");

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
        updateLevelUI(xp, userId);
        generateQRCode(userId);
    }

    private void updateLevelUI(long currentXP, String userId) {
        //dobavi trenutni level
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) return;

                    Map<String, Object> etapa = (Map<String, Object>) documentSnapshot.get("etapa");
                    //ako korisnik nema aktivnu etapu
                    if (etapa == null) {
                        Log.d("EtapaCheck", "No etapa found — creating initial etapa...");
                        ensureInitialEtapa(userId, 1);
                    } else {
                        Log.d("EtapaCheck", "Active etapa exists for level: " + etapa.get("level"));
                    }

                    long oldLevel = documentSnapshot.contains("level") ? documentSnapshot.getLong("level") : 1;

                    //racunanje XP i level-a
                    int level = 1;
                    long xpForNextLevel = 200;

                    while (currentXP >= xpForNextLevel) {
                        level++;
                        long next = xpForNextLevel * 2 + xpForNextLevel / 2;
                        xpForNextLevel = ((next + 99) / 100) * 100;
                    }

                    //update UI
                    textCurrentLevel.setText(String.valueOf(level));
                    textNextLevel.setText(String.valueOf(level + 1));

                    //dodela titula za prva 3 nivoa
                    String title;
                    switch (level) {
                        case 1: title = "Početnik"; break;
                        case 2: title = "Učenik"; break;
                        case 3: title = "Iskusni"; break;
                        default: title = "Veteran"; break;
                    }
                    textLevelTitle.setText("Level " + level + " - " + title);

                    int progress = (int) ((currentXP * 100) / xpForNextLevel);
                    levelProgressBar.setMax(100);
                    levelProgressBar.setProgress(progress);
                    textXP.setText("XP: " + currentXP + " / " + xpForNextLevel);

                    //racunanje PP-a
                    long pp = 20; //pre nego predje prvi nivo
                    if (level > 1) {
                        pp = 40; //nakon prvog nivoa
                        for (int i = 2; i < level; i++) {
                            pp = pp + (pp * 3 / 4);
                        }
                    }
                    textPP.setText("Snaga: " + pp);

                    //ažuriranje baze: level, title, PP
                    db.collection("users").document(userId)
                            .update("level", level, "title", title, "powerPoints", pp)
                            .addOnSuccessListener(aVoid ->
                                    Log.d("UserProfile", "Level, titula i PP ažurirani"))
                            .addOnFailureListener(e ->
                                    Log.e("UserProfile", "Greška pri ažuriranju levela i PP-a", e));

                    //level up
                    if (level > oldLevel) {
                        onLevelUp((int)oldLevel, level, userId); //nova etapa, borba sa bosom
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("UserProfile", "Greška pri dobavljanju korisnika", e));
    }


    private void generateQRCode(String userId) {
        MultiFormatWriter writer = new MultiFormatWriter();
        try {
            String qrContent = "userId:" + userId;
            BitMatrix matrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, 300, 300);
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.createBitmap(matrix);
            qrCode.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Promena lozinke");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        EditText oldPassword = new EditText(getContext());
        oldPassword.setHint("Stara lozinka");
        oldPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(oldPassword);

        EditText newPassword1 = new EditText(getContext());
        newPassword1.setHint("Nova lozinka");
        newPassword1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(newPassword1);

        EditText newPassword2 = new EditText(getContext());
        newPassword2.setHint("Potvrdi novu lozinku");
        newPassword2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(newPassword2);

        builder.setView(layout);

        builder.setPositiveButton("Promeni", (dialog, which) -> {
            String oldPass = oldPassword.getText().toString().trim();
            String newPass1 = newPassword1.getText().toString().trim();
            String newPass2 = newPassword2.getText().toString().trim();

            if (!newPass1.equals(newPass2)) {
                Toast.makeText(getContext(), "Nove lozinke se ne poklapaju.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPass1.length() < 6) {
                Toast.makeText(getContext(), "Lozinka mora imati najmanje 6 karaktera.", Toast.LENGTH_SHORT).show();
                return;
            }

            changePassword(oldPass, newPass1);
        });

        builder.setNegativeButton("Otkaži", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void changePassword(String oldPassword, String newPassword) {
        FirebaseUser user = auth.getCurrentUser();

        if (user != null && user.getEmail() != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);

            user.reauthenticate(credential)
                    .addOnSuccessListener(aVoid -> user.updatePassword(newPassword)
                            .addOnSuccessListener(aVoid1 ->
                                    Toast.makeText(getContext(), "Lozinka uspešno promenjena.", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Greška: " + e.getMessage(), Toast.LENGTH_SHORT).show()))
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Pogrešna stara lozinka.", Toast.LENGTH_SHORT).show());
        }
    }

    private void onLevelUp(int oldLevel, int newLevel, String userId) {
        long now = System.currentTimeMillis();
        DocumentReference userRef = db.collection("users").document(userId);
        AtomicReference<Map<String, Object>> previousEtapaRef = new AtomicReference<>();

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(userRef);

            //end stare etape
            Map<String, Object> previousEtapa = (Map<String, Object>) snapshot.get("etapa");
            if (previousEtapa != null) {
                previousEtapa.put("end", now);
                previousEtapa.put("level", oldLevel);

                previousEtapaRef.set(previousEtapa);

                //prethodna etapa u istoriju etapa
                String etapaId = "etapa_" + oldLevel;
                DocumentReference historyRef = userRef.collection("etapaHistory").document(etapaId);
                transaction.set(historyRef, previousEtapa);
            }

            //nova etapa
            Map<String, Object> newEtapa = new HashMap<>();
            newEtapa.put("level", newLevel);
            newEtapa.put("start", now);
            newEtapa.put("end", null);
            newEtapa.put("bossDefeated", false);
            newEtapa.put("successRate", 0.0);

            //update korisnikov level i etapu
            Map<String, Object> updates = new HashMap<>();
            updates.put("level", newLevel);
            updates.put("etapa", newEtapa);

            transaction.update(userRef, updates);

            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d("LevelUp", "Etapa " + newLevel + " started successfully!");
            Map<String, Object> previousEtapa = previousEtapaRef.get();
            if (previousEtapa != null) showPrepareBattleDialog(newLevel, previousEtapa);
            else showPrepareBattleDialog(newLevel, null);
            }).addOnFailureListener(e -> {
            Log.e("LevelUp", "Failed to start new etapa", e);
        });
    }

    private void showPrepareBattleDialog(int level, Object previousEtapa) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Novi nivo: " + level)
                .setMessage("Spreman/na za borbu sa bosom?")
                .setPositiveButton("Kreni", (d, w) -> {
                    Bundle bundle = new Bundle();
                    bundle.putInt("oldLevel", level - 1);
                    bundle.putParcelableArrayList("userEquipmentList", new ArrayList<>(userEquipmentList));
                    if (previousEtapa != null) bundle.putSerializable("previousEtapa", (Serializable) previousEtapa);
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.action_userProfileFragment_to_prepareBattleFragment, bundle);
                })
                .show();
    }

    private void ensureInitialEtapa(String userId, long currentLevel) {
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                Map<String, Object> etapa = (Map<String, Object>) snapshot.get("etapa");

                //ako korisnik još nema etapu — kreiraj prvu
                if (etapa == null) {
                    long now = System.currentTimeMillis();
                    Map<String, Object> newEtapa = new HashMap<>();
                    newEtapa.put("level", currentLevel);
                    newEtapa.put("start", now);
                    newEtapa.put("end", null);
                    newEtapa.put("bossDefeated", false);
                    newEtapa.put("successRate", 0.0);

                    userRef.update("etapa", newEtapa)
                            .addOnSuccessListener(aVoid -> Log.d("EtapaInit", "Initial etapa created for level " + currentLevel))
                            .addOnFailureListener(e -> Log.e("EtapaInit", "Failed to create initial etapa", e));
                }
            }
        }).addOnFailureListener(e -> Log.e("EtapaInit", "Error fetching user", e));
    }


}
