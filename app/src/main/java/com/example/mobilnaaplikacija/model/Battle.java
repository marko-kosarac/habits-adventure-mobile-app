package com.example.mobilnaaplikacija.model;

import android.media.audiofx.DynamicsProcessing;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Battle {
    private String id;
    private String userId;
    private String bossId;
    private List<String> equipmentIds;
    private Boolean userWon;
    private int coinsEarned;
    private List<Attack> attacks;

    public Battle() {}

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

    public List<String> getEquipmentIds() {
        return equipmentIds;
    }

    public void setEquipmentIds(List<String> equipmentIds) {
        this.equipmentIds = equipmentIds;
    }

    public Boolean hasUserWon() {
        return userWon;
    }

    public void setUserWon(Boolean userWon) {
        this.userWon = userWon;
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

    public String getEquipmentIdsAsString() {
        if (equipmentIds == null || equipmentIds.isEmpty()) return "";
        return TextUtils.join(",", equipmentIds);
    }

    public void setEquipmentIdsFromString(String idsString) {
        if (idsString == null || idsString.isEmpty()) {
            equipmentIds = new ArrayList<>();
        } else {
            equipmentIds = new ArrayList<>(Arrays.asList(idsString.split(",")));
        }
    }

}
