package com.example.mobilnaaplikacija.utils;

import com.example.mobilnaaplikacija.model.enums.DifficultyType;
import com.example.mobilnaaplikacija.model.enums.ImportanceType;

public class XpCalculator {

    public static int getDifficultyXP (DifficultyType difficulty) {
        switch (difficulty) {
            case VEOMA_LAK: return 1;
            case LAK: return 3;
            case TEŽAK: return 7;
            case EKSTREMNO_TEŽAK: return 20;
            default: return 0;
        }
    }

    public static int getImportanceXP (ImportanceType importance) {
        switch (importance) {
            case NORMALAN: return 1;
            case VAŽAN: return 3;
            case EKSTREMNO_VAŽAN: return 10;
            case SPECIJALAN: return 100;
            default: return 0;
        }
    }

    public static int getTotalXP (DifficultyType difficulty, ImportanceType importance) {
        return getDifficultyXP(difficulty) + getImportanceXP(importance);
    }
}
