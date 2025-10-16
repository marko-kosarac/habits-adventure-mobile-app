package com.example.mobilnaaplikacija.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Boss implements Parcelable{
    private String id;
    private int currentHp;
    private int maxHp;
    private int level; //currently fighting boss (1st, 2nd...)
    private boolean defeated;
    public Boss () {}

    public Boss (String id, int maxHp, boolean defeated) {
        this.id = id;
        this.currentHp = maxHp;
        this.maxHp = maxHp;
        this.level = 1;
        this.defeated = defeated;
    }

    public Boss(String id, int currentHp, int maxHp, int level, boolean defeated) {
        this.id = id;
        this.currentHp = currentHp;
        this.maxHp = maxHp;
        this.level = level;
        this.defeated = defeated;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public void setCurrentHp(int currentHp) {
        this.currentHp = currentHp;
    }
    public boolean isDefeated() { return defeated; }

    public void setDefeated(boolean defeated) {
        this.defeated = defeated;
    }

    protected Boss(Parcel in) {
        id = in.readString();
        currentHp = in.readInt();
        maxHp = in.readInt();
        level = in.readInt();
        defeated = in.readByte() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeInt(currentHp);
        dest.writeInt(maxHp);
        dest.writeInt(level);
        dest.writeByte((byte) (defeated ? 1 : 0));
    }

    public static final Creator<Boss> CREATOR = new Creator<Boss>() {
        @Override
        public Boss createFromParcel(Parcel in) {
            return new Boss(in);
        }

        @Override
        public Boss[] newArray(int size) {
            return new Boss[size];
        }
    };

}
