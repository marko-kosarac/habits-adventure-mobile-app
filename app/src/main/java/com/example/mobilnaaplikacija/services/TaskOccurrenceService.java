package com.example.mobilnaaplikacija.services;

import android.content.Context;

import com.example.mobilnaaplikacija.database.SQLiteHelper;
import com.example.mobilnaaplikacija.model.Task;
import com.example.mobilnaaplikacija.model.TaskOccurrence;
import com.example.mobilnaaplikacija.repository.TaskOccurrenceRepository;

import java.util.ArrayList;
import java.util.List;

public class TaskOccurrenceService {

    private final Context context;
    private final TaskOccurrenceRepository taskOccurrenceRepository;

    public TaskOccurrenceService(Context context) {
        this.context = context;
        this.taskOccurrenceRepository = new TaskOccurrenceRepository(new SQLiteHelper(context));
    }

    public TaskOccurrence add(TaskOccurrence taskOccurrence) {
        return taskOccurrenceRepository.add(taskOccurrence);
    }


    public TaskOccurrence update(TaskOccurrence taskOccurrence) {
        return taskOccurrenceRepository.update(taskOccurrence);
    }

    public List<TaskOccurrence> getTasksByTask(String taskId){
        return taskOccurrenceRepository.getTaskOccurrencesByTask(taskId);
    }

    public Boolean deleteById(String id){
        return taskOccurrenceRepository.delete(id) > 0;
    }

    public void deleteByTask(String taskId) {
        ArrayList<TaskOccurrence> taskOccurrences = new ArrayList<>(getTasksByTask(taskId));
        for (TaskOccurrence occurrence : taskOccurrences) {
            if (occurrence.getTaskId().equals(taskId))
                deleteById(occurrence.getId());
        }
    }
}
