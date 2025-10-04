package com.example.mobilnaaplikacija.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.example.mobilnaaplikacija.model.enums.DifficultyType;
import com.example.mobilnaaplikacija.model.enums.FrequencyType;
import com.example.mobilnaaplikacija.model.enums.ImportanceType;
import com.example.mobilnaaplikacija.model.enums.StatusType;
import com.example.mobilnaaplikacija.model.enums.UnitType;

public class Task implements Parcelable {
    private String id;
    private String taskId;
    private String userId;
    private String name;
    private String description;
    private String categoryId;
    private FrequencyType frequency;
    private Long startMillis;
    private Long endMillis;
    private Integer interval;
    private UnitType unit;
    private DifficultyType difficulty;
    private ImportanceType importance;
    private StatusType status;

    public Task() {}

    public Task(String userId, String taskId, String name, String description, String categoryId, FrequencyType frequency, Long startMillis, Long endMillis, Integer interval, UnitType unit, DifficultyType difficulty, ImportanceType importance, StatusType status) {
        this.userId = userId;
        this.taskId = taskId;
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.frequency = frequency;
        this.startMillis = startMillis;
        this.endMillis = endMillis;
        this.interval = interval;
        this.unit = unit;
        this.difficulty = difficulty;
        this.importance = importance;
        this.status = status;
    }

    public Task(String id, String userId, String taskId, String name, String description, String categoryId, FrequencyType frequency, Long startMillis, Long endMillis, Integer interval, UnitType unit, DifficultyType difficulty, ImportanceType importance, StatusType status) {
        this.id = id;
        this.userId = userId;
        this.taskId = taskId;
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.frequency = frequency;
        this.startMillis = startMillis;
        this.endMillis = endMillis;
        this.interval = interval;
        this.unit = unit;
        this.difficulty = difficulty;
        this.importance = importance;
        this.status = status;
    }

    protected Task(Parcel in) {
        id = in.readString();
        userId = in.readString();
        taskId = in.readString();
        name = in.readString();
        description = in.readString();
        categoryId = in.readString();
        String freqName = in.readString();
        frequency = freqName != null ? FrequencyType.valueOf(freqName) : null;
        startMillis = in.readLong();
        endMillis = in.readLong();
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

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategoryId() {return categoryId;}
    public void setCategoryId(String categoryId) {this.categoryId = categoryId;}

    public FrequencyType getFrequency() { return frequency; }
    public void setFrequency(FrequencyType frequency) { this.frequency = frequency; }

    public Long getEndMillis() { return endMillis; }

    public void setEndMillis(Long endMillis) { this.endMillis = endMillis; }

    public Long getStartMillis() {
        return startMillis;
    }

    public void setStartMillis(Long startMillis) {
        this.startMillis = startMillis;
    }

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
        parcel.writeString(id != null ? id : "-1");
        parcel.writeString(taskId != null ? taskId : "-1");
        parcel.writeString(userId != null ? userId : "-1");
        parcel.writeString(name);
        parcel.writeString(description);
        parcel.writeString(categoryId != null ? categoryId : "-1");
        parcel.writeString(frequency != null ? frequency.name() : null);
        parcel.writeInt(interval != null ? interval : 0);
        parcel.writeString(unit != null ? unit.name() : null);
        parcel.writeString(difficulty != null ? difficulty.name() : null);
        parcel.writeString(importance != null ? importance.name() : null);
        parcel.writeString(status != null ? status.name() : null);
    }

    public static final Creator<Task> CREATOR = new Creator<>() {
        @Override
        public Task createFromParcel(Parcel in) { return new Task(in); }
        @Override
        public Task[] newArray(int size) { return new Task[size]; }
    };
}
