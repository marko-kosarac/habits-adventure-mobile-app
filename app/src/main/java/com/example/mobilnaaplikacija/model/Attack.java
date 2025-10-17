package com.example.mobilnaaplikacija.model;

public class Attack {
    private String id;
    private String userId;
    private String bossId;
    private int attemptNumber;
    private boolean hit;
    private int damageDealt;

    public Attack(){}
    public Attack(String id, String userId, String bossId, int attemptNumber, boolean hit, int damageDealt) {
        this.id = id;
        this.userId = userId;
        this.bossId = bossId;
        this.attemptNumber = attemptNumber;
        this.hit = hit;
        this.damageDealt = damageDealt;
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

    public int getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(int attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    public boolean isHit() {
        return hit;
    }

    public void setHit(boolean hit) {
        this.hit = hit;
    }

    public int getDamageDealt() {
        return damageDealt;
    }

    public void setDamageDealt(int damageDealt) {
        this.damageDealt = damageDealt;
    }
}
