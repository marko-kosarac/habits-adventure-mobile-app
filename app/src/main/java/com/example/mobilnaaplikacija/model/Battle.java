package com.example.mobilnaaplikacija.model;

import java.util.List;

public class Battle {
    private String id;
    private String userId;
    private String bossId;
    private boolean bossDefeated;
    private double remainingBossHp;
    private int coinsEarned;
    private List<Attack> attacks;

    public Battle(String id, String userId, String bossId, boolean bossDefeated, double remainingBossHp, int coinsEarned, List<Attack> attacks) {
        this.id = id;
        this.userId = userId;
        this.bossId = bossId;
        this.bossDefeated = bossDefeated;
        this.remainingBossHp = remainingBossHp;
        this.coinsEarned = coinsEarned;
        this.attacks = attacks;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBossId() {
        return bossId;
    }

    public void setBossId(String bossId) {
        this.bossId = bossId;
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
