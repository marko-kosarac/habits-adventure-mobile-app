package com.example.mobilnaaplikacija.services;

import android.content.Context;
import android.util.Log;

import com.example.mobilnaaplikacija.database.SQLiteHelper;
import com.example.mobilnaaplikacija.model.Equipment;
import com.example.mobilnaaplikacija.repository.BossRepository;
import com.example.mobilnaaplikacija.repository.EquipmentRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class EquipmentService {
    private final EquipmentRepository equipmentRepository;

    public EquipmentService(Context context) {
        this.equipmentRepository = new EquipmentRepository(context);
    }

    public void getEquipmentReward(String userId, double totalChance) {
        double roll = Math.random();
        if (roll > totalChance) {
            Log.d("Nagrada", "Nema nagrade u vidu opreme.");
            return;
        }

        double typeRoll = Math.random();
        Equipment.Type rewardType = typeRoll <= 0.95 ? Equipment.Type.ODECA : Equipment.Type.ORUZJE;

        ArrayList<Equipment> allEquipment = equipmentRepository.getAllEquipment();

        List<Equipment> filtered = allEquipment.stream()
                .filter(eq -> eq.getType() == rewardType)
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            Log.w("Nagrada", "Nema nagrade tipa: " + rewardType);
            return;
        }

        Equipment rewardItem = filtered.get(new Random().nextInt(filtered.size()));
        Log.i("Reward", "User " + userId + " won equipment: " + rewardItem.getName() + " [" + rewardItem.getType() + "]");

        //TODO: osvojenu opremu pohraniti?
    }

}
