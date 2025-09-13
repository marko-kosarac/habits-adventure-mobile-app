package com.example.mobilnaaplikacija.model;

import java.util.ArrayList;
import java.util.List;
import com.example.mobilnaaplikacija.model.Equipment;

public class User {
    private String id;
    private String email;
    private String password;
    private String username;
    private boolean isFriend;
    private int avatarId; // indeks od 0-4 za izbor avatara

    private boolean isActivated;
    private boolean requestSent = false;

    private long activationDeadline; // timestamp do kada važi aktivacioni link (24h)

    // Profil info
    private int level;
    private String title;
    private int powerPoints;
    private int experiencePoints;
    private int coins;
    private boolean friendRequestSent = false;

    private List<String> badges; // lista naziva bedževa
    private List<Equipment> equipment;
    private List<User> friends;

    private String qrCode; // može biti putanja do slike ili generisani kod

    public User() {
        super();
    }
    // Konstruktor za registraciju

    public User(String id, String username, boolean isFriend) {
        this.id = id;
        this.username = username;
        this.isFriend = isFriend;
    }
    public User(String id,String email, String password, String username, int avatarId) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.username = username;
        this.avatarId = avatarId;

        this.isActivated = false;
        this.activationDeadline = System.currentTimeMillis() + 24 * 60 * 60 * 1000; // 24h

        // Default vrednosti profila
        this.level = 1;
        this.title = "Početnik";
        this.powerPoints = 0;
        this.experiencePoints = 0;
        this.coins = 0;
        this.friends = new ArrayList<>();
    }

    // GETTERI I SETTERI
    public String getId() {
        return id;
    }
    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public int getAvatarId() {
        return avatarId;
    }

    public boolean isActivated() {
        return isActivated;
    }

    public void activateAccount() {
        if (System.currentTimeMillis() <= activationDeadline) {
            this.isActivated = true;
        }
    }

    public int getLevel() {
        return level;
    }

    public String getTitle() {
        return title;
    }

    public int getPowerPoints() {
        return powerPoints;
    }

    public int getExperiencePoints() {
        return experiencePoints;
    }

    public int getCoins() {
        return coins;
    }

    public List<String> getBadges() {
        return badges;
    }

    public List<Equipment> getEquipment() {
        return equipment;
    }

    public String getQrCode() {
        return qrCode;
    }

    // Metod za promenu lozinke
    public boolean changePassword(String oldPass, String newPass) {
        if (this.password.equals(oldPass)) {
            this.password = newPass;
            return true;
        }
        return false;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setAvatarId(int avatarId) {
        this.avatarId = avatarId;
    }

    public void setActivated(boolean activated) {
        isActivated = activated;
    }

    public void setActivationDeadline(long activationDeadline) {
        this.activationDeadline = activationDeadline;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPowerPoints(int powerPoints) {
        this.powerPoints = powerPoints;
    }

    public void setExperiencePoints(int experiencePoints) {
        this.experiencePoints = experiencePoints;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public void setBadges(List<String> badges) {
        this.badges = badges;
    }

    public void setEquipment(List<Equipment> equipment) {
        this.equipment = equipment;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public List<User> getFriends() {
        return friends;
    }

    public void setFriends(List<User> friends) {
        this.friends = friends;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public void setFriend(boolean friend) {
        isFriend = friend;
    }

    public boolean isFriendRequestSent() { return friendRequestSent; }
    public void setFriendRequestSent(boolean sent) { this.friendRequestSent = sent; }

    public boolean isRequestSent() { return requestSent; }
    public void setRequestSent(boolean sent) { this.requestSent = sent; }
}
