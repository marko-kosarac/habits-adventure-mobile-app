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

    public interface OnRewardReady {
        void onSuccess(Equipment reward);
        void onError(Exception e);
    }

    public void getEquipmentReward(String userId, double totalChance, OnRewardReady callback) {
        double roll = Math.random();
        if (roll > totalChance) {
            Log.d("Nagrada", "Nema nagrade u vidu opreme.");
            callback.onSuccess(null);
            return;
        }

        double typeRoll = Math.random();
        Equipment.Type rewardType = typeRoll <= 0.95 ? Equipment.Type.ODECA : Equipment.Type.ORUZJE;

        ArrayList<Equipment> allEquipment = equipmentRepository.getAllEquipment();
        allEquipment.add(new Equipment(10, "Čelični mač", "Trajno povećava snagu za 5%", Equipment.Type.ORUZJE, "+5%", -1, 500, 0));
        allEquipment.add(new Equipment(11, "Luk i strela", "Trajno povećava procenat dobijenog novca za 5%", Equipment.Type.ORUZJE, "+5%", -1, 700, 0));

        List<Equipment> filtered = allEquipment.stream()
                .filter(eq -> eq.getType() == rewardType)
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            callback.onSuccess(null);
            return;
        }

        Equipment rewardItem = filtered.get(new Random().nextInt(filtered.size()));

        userService.addEquipmentToUser(userId, rewardItem,
                aVoid -> {
                    Log.i("Nagrada", "Oprema dodana u inventar: " + rewardItem.getName());
                    callback.onSuccess(rewardItem);
                },
                e -> {
                    Log.e("Nagrada", "Neuspjelo dodavanje opreme: " + e.getMessage());
                    callback.onError(e);
                });
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
                long duration = battleEq.getDuration();
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

                    if (!active || !eq.get("id").equals(battleEq.getId())) continue;

                    if (duration == 0 && eqType == Equipment.Type.NAPITAK && active) {
                        powerPoints = powerPoints / (1 + eqBonus);
                        powerPoints = Math.ceil(powerPoints);
                        iterator.remove();
                        break;
                    }

                    if (eqType == Equipment.Type.ODECA) {
                        eqCount++;
                        eq.put("count", eqCount);
                        if (eqCount >= 2) {
                            iterator.remove();
                        }
                        break;
                    }
                }
            }

            userRef.update("equipment", updatedEquipment,
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
