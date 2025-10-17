package com.example.mobilnaaplikacija.services;

import android.content.Context;
import android.media.audiofx.DynamicsProcessing;
import android.util.Log;

import com.example.mobilnaaplikacija.model.Equipment;
import com.example.mobilnaaplikacija.repository.EquipmentRepository;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class EquipmentService {
    private final EquipmentRepository equipmentRepository;
    private UserService userService;

    public EquipmentService(Context context) {
        this.equipmentRepository = new EquipmentRepository(context);
        this.userService = new UserService();
    }

    public Equipment getEquipmentReward(String userId, double totalChance) {
        //double roll = Math.random();
        double roll = 0; //TODO FIX LATER
        if (roll > totalChance) {
            Log.d("Nagrada", "Nema nagrade u vidu opreme.");
            return null;
        }

        double typeRoll = Math.random();
        Equipment.Type rewardType = typeRoll <= 0.95 ? Equipment.Type.ORUZJE : Equipment.Type.ODECA; //TODO FIX LATER

        ArrayList<Equipment> allEquipment = equipmentRepository.getAllEquipment();
        allEquipment.add(new Equipment(10, "Čelični mač", "Trajno povećava snagu za 5%",
                Equipment.Type.ORUZJE, "+5%", -1, 500, 0));
        allEquipment.add(new Equipment(11, "Luk i strela",
                "Trajno povećava procenat dobijenog novca za 5%",
                Equipment.Type.ORUZJE, "+5%", -1, 700, 0));

        List<Equipment> filtered = allEquipment.stream()
                .filter(eq -> eq.getType() == rewardType)
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            Log.w("Nagrada", "Nema nagrade tipa: " + rewardType);
            return null;
        }

        Equipment rewardItem = filtered.get(new Random().nextInt(filtered.size()));
        userService.addEquipmentToUser(userId, rewardItem,
                aVoid -> Log.i("Nagrada", "Oprema dodana u inventar: " + rewardItem.getName()),
                e -> Log.e("Nagrada", "Neuspjelo dodavanje opreme: " + e.getMessage()));

        Log.i("Nagrada", "Korisnik " + userId + " je osvojio opremu: " + rewardItem.getName() + " [" + rewardItem.getType() + "]");

        return rewardItem;
    }

    public void manageEquipmentAfterBattle(String userId, List<Equipment> equipmentFromBattle) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.get().addOnSuccessListener(userDoc -> {
            if (!userDoc.exists()) return;

            Double powerPointsObj = userDoc.getDouble("powerPoints");
            double powerPoints = powerPointsObj != null ? powerPointsObj : 0.0;

            List<Map<String, Object>> userEquipment = (List<Map<String, Object>>) userDoc.get("equipment");
            if (userEquipment == null) userEquipment = new ArrayList<>();

            List<Map<String, Object>> updatedEquipment = new ArrayList<>(userEquipment);

            for (Equipment battleEq : equipmentFromBattle) {
                Equipment.Type type = battleEq.getType();
                long duration = battleEq.getDuration();
                long count = battleEq.getCount();
                double bonus = parseBonus(battleEq.getBonus());

                Iterator<Map<String, Object>> iterator = updatedEquipment.iterator();

                while (iterator.hasNext()) {
                    Map<String, Object> eq = iterator.next();
                    boolean active = (boolean) eq.get("active");
                    Equipment.Type eqType = Equipment.Type.valueOf(((String) eq.get("type")).toUpperCase());
                    long eqCount = 0;
                    if (eq.containsKey("count") && eq.get("count") != null) {
                        eqCount = ((Number) eq.get("count")).longValue();
                    }
                    double eqBonus = parseBonus((String) eq.get("bonus"));

                    // match by id or name
                    if (!active || !eq.get("id").equals(battleEq.getId())) continue;

                    // NAPITAK logic
                    if (duration == 0 && eqType == Equipment.Type.NAPITAK && active) {
                        powerPoints = powerPoints / (1 + eqBonus);
                        powerPoints = Math.round(powerPoints);
                        iterator.remove();
                        break; // remove one matching active NAPITAK
                    }

                    // ODECA logic
                    if (eqType == Equipment.Type.ODECA) {
                        if (eqCount >= 2) {
                            iterator.remove(); // remove if count >=2
                        } else {
                            eq.put("count", eqCount + 1); // increment count
                        }
                        break; // processed one matching piece
                    }
                }
            }

            // Update Firestore
            userRef.update(
                            "equipment", updatedEquipment,
                            "powerPoints", powerPoints
                    ).addOnSuccessListener(v -> Log.d("Battle", "Equipment and powerPoints updated successfully"))
                    .addOnFailureListener(e -> Log.e("Battle", "Error updating user equipment", e));
        });
    }

    private double parseBonus(String bonusStr) {
        if (bonusStr == null || bonusStr.isEmpty()) return 0.0;
        try {
            String clean = bonusStr.replace("%", "").replace("+", "").trim();
            return Double.parseDouble(clean) / 100.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }





}
