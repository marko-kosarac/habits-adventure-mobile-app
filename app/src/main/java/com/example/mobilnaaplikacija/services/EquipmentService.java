package com.example.mobilnaaplikacija.services;

import android.content.Context;
import android.util.Log;

import com.example.mobilnaaplikacija.model.Equipment;
import com.example.mobilnaaplikacija.repository.EquipmentRepository;

import java.util.ArrayList;
import java.util.List;
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
        double roll = Math.random();
        if (roll > totalChance) {
            Log.d("Nagrada", "Nema nagrade u vidu opreme.");
            return null;
        }

        double typeRoll = Math.random();
        Equipment.Type rewardType = typeRoll <= 0.95 ? Equipment.Type.ODECA : Equipment.Type.ORUZJE;

        ArrayList<Equipment> allEquipment = equipmentRepository.getAllEquipment();
        //TODO da li treba dodati u bazu?
        allEquipment.add(new Equipment(0, "Mač Snage +5%", "Trajno povećava snagu za 5%",
                Equipment.Type.ORUZJE, "+5% Snage", -1, 500));
        allEquipment.add(new Equipment(0, "Luk i strela +5%",
                "Trajno povećava procenat dobijenog novca za 5%",
                Equipment.Type.ORUZJE, "+5% Novca", -1, 700));

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

}
