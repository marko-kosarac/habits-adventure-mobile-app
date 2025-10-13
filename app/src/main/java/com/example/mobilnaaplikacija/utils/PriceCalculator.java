package com.example.mobilnaaplikacija.utils;

import com.example.mobilnaaplikacija.model.Equipment;

public class PriceCalculator {

    /** Parsira string bonus i vraća multiplier prema tipu opreme i bonusu */
    public static double getMultiplier(Equipment.Type type, String bonusStr) {
        // uklanjanje znakova i izvlačenje broja
        double bonusValue = 0;
        try {
            bonusValue = Double.parseDouble(bonusStr.replaceAll("[^0-9.]",""));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        switch(type) {
            case NAPITAK:
                if (bonusValue == 20) return 0.5;    // 50% nagrade
                if (bonusValue == 40) return 0.7;    // 70%
                if (bonusValue == 5)  return 2.0;    // 200% trajni
                if (bonusValue == 10) return 10.0;   // 1000% trajni
                break;

            case ODECA:
                if (bonusValue == 10) return 0.6;    // rukavice i štit
                if (bonusValue == 40) return 0.8;    // čizme
                break;

            case ORUZJE:
                // dodati logiku ako budeš imao oružje
                break;
        }
        return 1.0; // default multiplier
    }

    /** Izračunava cenu na osnovu nivoa korisnika, tipa i bonusa */
    public static int calculatePrice(int currentLevel, Equipment.Type type, String bonusStr) {
        int prevReward = RewardCalculator.getRewardForPreviousLevel(currentLevel);
        double multiplier = getMultiplier(type, bonusStr);
        return (int) Math.round(prevReward * multiplier);
    }
}
