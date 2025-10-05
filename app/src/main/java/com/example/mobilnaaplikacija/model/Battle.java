package com.example.mobilnaaplikacija.model;

import java.util.List;

public class Battle {
    private boolean bossDefeated;
    private double remainingBossHp;
    private int coinsEarned;
    private List<Attack> attacks;

    public Battle(boolean bossDefeated, double remainingBossHp, int coinsEarned, List<Attack> attacks) {
        this.bossDefeated = bossDefeated;
        this.remainingBossHp = remainingBossHp;
        this.coinsEarned = coinsEarned;
        this.attacks = attacks;
    }

    public boolean isBossDefeated() {
        return bossDefeated;
    }

    public void setBossDefeated(boolean bossDefeated) {
        this.bossDefeated = bossDefeated;
    }

    public double getRemainingBossHp() {
        return remainingBossHp;
    }

    public void setRemainingBossHp(double remainingBossHp) {
        this.remainingBossHp = remainingBossHp;
    }

    public int getCoinsEarned() {
        return coinsEarned;
    }

    public void setCoinsEarned(int coinsEarned) {
        this.coinsEarned = coinsEarned;
    }

    public List<Attack> getAttacks() {
        return attacks;
    }

    public void setAttacks(List<Attack> attacks) {
        this.attacks = attacks;
    }
}
