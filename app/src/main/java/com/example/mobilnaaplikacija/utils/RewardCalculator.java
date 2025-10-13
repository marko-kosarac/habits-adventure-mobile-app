package com.example.mobilnaaplikacija.utils;

public class RewardCalculator {

    private static final double START_REWARD = 200.0;   // nagrada za 1. nivo
    private static final double LEVEL_MULTIPLIER = 1.2; // 20% više po nivou

    /** Dobija nagradu za dati nivo */
    public static int getRewardForLevel(int level) {
        if (level <= 1) return (int) START_REWARD;
        double reward = START_REWARD * Math.pow(LEVEL_MULTIPLIER, level - 1);
        return (int) Math.round(reward);
    }

    /** Dobija nagradu za prethodni nivo */
    public static int getRewardForPreviousLevel(int currentLevel) {
        int previousLevel = currentLevel - 1;
        if (previousLevel < 1) previousLevel = 1;
        return getRewardForLevel(previousLevel);
    }
}
