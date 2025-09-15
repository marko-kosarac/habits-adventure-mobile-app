package com.example.mobilnaaplikacija.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Task implements Parcelable {
    public Long id;
    public String userId;
    public String name;
    public String description;
    public Long categoryId;
    public FrequencyType frequency;
    public String startDate;
    public String endDate;
    public String time;
    public Boolean isWholeDay;
    public Integer interval;
    public UnitType unit;
    public DifficultyType difficulty;
    public ImportanceType importance;
    public StatusType status;

    public Task() {}

    public Task(String userId, String name, String description, Long categoryId, FrequencyType frequency, String startDate, String endDate, String time, Boolean isWholeDay, Integer interval, UnitType unit, DifficultyType difficulty, ImportanceType importance, StatusType status) {
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.frequency = frequency;
        this.startDate = startDate;
        this.endDate = endDate;
        this.time = time;
        this.isWholeDay = isWholeDay;
        this.interval = interval;
        this.unit = unit;
        this.difficulty = difficulty;
        this.importance = importance;
        this.status = status;
    }

    public Task(Long id, String userId, String name, String description, Long categoryId, FrequencyType frequency, String startDate, String endDate, String time, Boolean isWholeDay, Integer interval, UnitType unit, DifficultyType difficulty, ImportanceType importance, StatusType status) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.frequency = frequency;
        this.startDate = startDate;
        this.endDate = endDate;
        this.time = time;
        this.isWholeDay = isWholeDay;
        this.interval = interval;
        this.unit = unit;
        this.difficulty = difficulty;
        this.importance = importance;
        this.status = status;
    }

    protected Task(Parcel in) {
        id = in.readLong();
        userId = in.readString();
        name = in.readString();
        description = in.readString();
        categoryId = in.readLong();
        String freqName = in.readString();
        frequency = freqName != null ? FrequencyType.valueOf(freqName) : null;
        startDate = in.readString();
        endDate = in.readString();
        time = in.readString();
        isWholeDay = in.readBoolean();
        interval = in.readInt();
        String unitName = in.readString();
        unit = unitName != null ? UnitType.valueOf(unitName) : null;
        String diffName = in.readString();
        difficulty = diffName != null ? DifficultyType.valueOf(diffName) : null;
        String impName = in.readString();
        importance = impName != null ? ImportanceType.valueOf(impName) : null;
        String statusName = in.readString();
        status = statusName != null ? StatusType.valueOf(statusName) : null;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getCategoryId() {return categoryId;}
    public void setCategoryId(Long categoryId) {this.categoryId = categoryId;}

    public FrequencyType getFrequency() { return frequency; }
    public void setFrequency(FrequencyType frequency) { this.frequency = frequency; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public Boolean getWholeDay() { return isWholeDay; }
    public void setWholeDay(Boolean wholeDay) { isWholeDay = wholeDay; }

    public Integer getInterval() { return interval; }
    public void setInterval(Integer interval) { this.interval = interval; }

    public UnitType getUnit() { return unit; }
    public void setUnit(UnitType unit) { this.unit = unit; }

    public DifficultyType getDifficulty() { return difficulty; }
    public void setDifficulty(DifficultyType difficulty) { this.difficulty = difficulty; }

    public ImportanceType getImportance() { return importance; }
    public void setImportance(ImportanceType importance) { this.importance = importance; }

    public StatusType getStatus() { return status; }
    public void setStatus(StatusType status) { this.status = status; }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeLong(id != null ? id : -1);
        parcel.writeString(userId);
        parcel.writeString(name);
        parcel.writeString(description);
        parcel.writeLong(categoryId != null ? categoryId : -1);
        parcel.writeString(frequency != null ? frequency.name() : null);
        parcel.writeString(startDate);
        parcel.writeString(endDate);
        parcel.writeString(time);
        parcel.writeBoolean(isWholeDay != null ? isWholeDay : false);
        parcel.writeInt(interval != null ? interval : 0);
        parcel.writeString(unit != null ? unit.name() : null);
        parcel.writeString(difficulty != null ? difficulty.name() : null);
        parcel.writeString(importance != null ? importance.name() : null);
        parcel.writeString(status != null ? status.name() : null);
    }

    public static final Creator<Task> CREATOR = new Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel in) { return new Task(in); }
        @Override
        public Task[] newArray(int size) { return new Task[size]; }
    };
}
