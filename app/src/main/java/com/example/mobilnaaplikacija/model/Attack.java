package com.example.mobilnaaplikacija.model;

public class Attack {
    private String id;
    private String userId;
    private String battleId;
    private int attemptNumber;
    private boolean hit;
    private double damageDealt;

    public Attack(String id, String userId, String battleId, int attemptNumber, boolean hit, double damageDealt) {
        this.id = id;
        this.userId = userId;
        this.battleId = battleId;
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

    public String getBattleId() {
        return battleId;
    }

    public void setBattleId(String battleId) {
        this.battleId = battleId;
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

    public double getDamageDealt() {
        return damageDealt;
    }

    public void setDamageDealt(double damageDealt) {
        this.damageDealt = damageDealt;
    }
}
