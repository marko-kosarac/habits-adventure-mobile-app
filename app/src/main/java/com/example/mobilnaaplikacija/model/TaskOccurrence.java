package com.example.mobilnaaplikacija.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.time.LocalTime;

public class TaskOccurrence implements Parcelable {
    private String id;
    private String taskId;
    private String name;
    private String description;
    private Long startMillis;
    private Long endMillis;
    private StatusType status;

    public TaskOccurrence() {}

    public TaskOccurrence(String taskId, String name, String description, Long startMillis, Long endMillis, StatusType status) {
        this.taskId = taskId;
        this.name = name;
        this.description = description;
        this.startMillis = startMillis;
        this.endMillis = endMillis;
        this.status = status;
    }

    public TaskOccurrence(String id, String taskId, String name, String description, Long startMillis, Long endMillis, StatusType status) {
        this.id = id;
        this.taskId = taskId;
        this.name = name;
        this.description = description;
        this.startMillis = startMillis;
        this.endMillis = endMillis;
        this.status = status;
    }

    protected TaskOccurrence(Parcel in) {
        id = in.readString();
        taskId = in.readString();
        name = in.readString();
        description = in.readString();
        startMillis = in.readLong();
        endMillis = in.readLong();
        String statusName = in.readString();
        status = statusName != null ? StatusType.valueOf(statusName) : null;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getEndMillis() { return endMillis; }
    public void setEndMillis(Long endMillis) { this.endMillis = endMillis; }

    public Long getStartMillis() { return startMillis; }
    public void setStartMillis(Long startMillis) { this.startMillis = startMillis; }

    public StatusType getStatus() { return status; }
    public void setStatus(StatusType status) { this.status = status; }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(id != null ? id : "-1");
        parcel.writeString(taskId != null ? taskId : "-1");
        parcel.writeString(name);
        parcel.writeString(description);
        parcel.writeString(status != null ? status.name() : null);
    }

    public static final Creator<TaskOccurrence> CREATOR = new Creator<>() {
        @Override
        public TaskOccurrence createFromParcel(Parcel in) { return new TaskOccurrence(in); }
        @Override
        public TaskOccurrence[] newArray(int size) { return new TaskOccurrence[size]; }
    };
}
