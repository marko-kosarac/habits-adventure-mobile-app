package com.example.mobilnaaplikacija.model;

import java.util.List;

public class Battle {
    private String id;
    private String userId;
    private String bossId;
    private int coinsEarned;
    private List<Attack> attacks;

    public Battle() {}

    public Battle(String id, String userId, String bossId, int coinsEarned, List<Attack> attacks) {
        this.id = id;
        this.userId = userId;
        this.bossId = bossId;
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
