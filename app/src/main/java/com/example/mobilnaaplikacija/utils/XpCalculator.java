package com.example.mobilnaaplikacija.utils;

import com.example.mobilnaaplikacija.model.enums.DifficultyType;
import com.example.mobilnaaplikacija.model.enums.ImportanceType;
import com.google.firebase.auth.FirebaseUser;

public class XpCalculator {
    //level 1
    private static final int BASE_VEOMA_LAK = 1;
    private static final int BASE_LAK = 3;
    private static final int BASE_TEŽAK = 7;
    private static final int BASE_EKSTREMNO_TEŽAK = 20;
    private static final int BASE_NORMALAN = 1;
    private static final int BASE_VAŽAN = 3;
    private static final int BASE_EKSTREMNO_VAŽAN = 10;
    private static final int BASE_SPECIJALAN = 100;

    public static int getTotalXP (DifficultyType difficulty, ImportanceType importance, int userLevel) {
        return getDifficultyXP(difficulty, userLevel) + getImportanceXP(importance, userLevel); // xp svakog zadatka
    }

    //na osnovu nivoa racuna XP tezine
    public static int getDifficultyXP (DifficultyType difficulty, int userLevel) {
        double xp;
        switch (difficulty) {
            case VEOMA_LAK:
                xp = getScaledXP(BASE_VEOMA_LAK, userLevel);
                break;
            case LAK:
                xp = getScaledXP(BASE_LAK, userLevel);
                break;
            case TEŽAK:
                xp = getScaledXP(BASE_TEŽAK, userLevel);
                break;
            case EKSTREMNO_TEŽAK:
                xp = getScaledXP(BASE_EKSTREMNO_TEŽAK, userLevel);
                break;
            default:
                xp = 0;
        }
        return (int) Math.round(xp);
    }

    //na osnovu nivoa racuna XP bitnosti
    public static int getImportanceXP(ImportanceType importance, int userLevel) {
        double xp;
        switch (importance) {
            case NORMALAN:
                xp = getScaledXP(BASE_NORMALAN, userLevel);
                break;
            case VAŽAN:
                xp = getScaledXP(BASE_VAŽAN, userLevel);
                break;
            case EKSTREMNO_VAŽAN:
                xp = getScaledXP(BASE_EKSTREMNO_VAŽAN, userLevel);
                break;
            case SPECIJALAN:
                xp = getScaledXP(BASE_SPECIJALAN, userLevel);
                break;
            default:
                xp = 0;
        }
        return (int) Math.round(xp);
    }

    private static double getScaledXP(double baseXP, int userLevel) {
        if (userLevel <= 1) return baseXP;

        double xp = baseXP;
        for (int i = 2; i <= userLevel; i++) {
            xp = Math.round(xp + xp / 2.0); // XP bitnosti/tezine za prethodni nivo + XP bitnosti/tezine za prethodni nivo / 2
        }
        return xp;
    }

}
