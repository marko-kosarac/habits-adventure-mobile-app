package com.example.mobilnaaplikacija.services;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.example.mobilnaaplikacija.database.SQLiteHelper;
import com.example.mobilnaaplikacija.repository.TaskRepository;
import com.example.mobilnaaplikacija.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TaskService {

    private final Context context;
    private final TaskRepository taskRepository;

    public TaskService(Context context) {
        this.context = context;
        this.taskRepository = new TaskRepository(new SQLiteHelper(context));
    }

    public Task validateAndCreateTask(
            String name,
            String description,
            String category,
            boolean isRepeating,
            boolean isOneTime,
            String startDate,
            String endDate,
            String time,
            boolean isWholeDay,
            String difficultyStr,
            String importanceStr,
            String unitStr,
            String intervalStr
    ) {
        FrequencyType frequency = isRepeating ? FrequencyType.PONAVLJAJUCI :
                (isOneTime ? FrequencyType.JEDNOKRATAN : null);

        // Validation
        if (name.isEmpty()) return showError("Unesite naziv zadatka!");
        if (category.isEmpty()) return showError("Izaberite kategoriju zadatka!");
        if (startDate.isEmpty()) return showError("Izaberite datum početka!");
        if (endDate.isEmpty()) return showError("Izaberite datum završetka!");
        if (time.isEmpty() && !isWholeDay) return showError("Unesite vrijeme zadatka!");
        if (frequency == null) return showError("Izaberite jednokratan ili ponavljajući zadatak!");
        if (difficultyStr.isEmpty()) return showError("Izaberite težinu zadatka!");
        if (importanceStr.isEmpty()) return showError("Izaberite bitnost zadatka!");

        Integer interval = null;
        UnitType unit = null;

        if (frequency == FrequencyType.PONAVLJAJUCI) {
            if (unitStr.isEmpty()) return showError("Unesite jedinicu zadatka!");
            unit = UnitType.valueOf(unitStr.toUpperCase(Locale.ROOT));

            if (intervalStr.isEmpty()) return showError("Unesite broj ponavljanja zadatka!");
            interval = Integer.valueOf(intervalStr);
        }

        DifficultyType difficulty = DifficultyType.valueOf(difficultyStr.toUpperCase(Locale.ROOT).replace(" ", "_"));
        ImportanceType importance = ImportanceType.valueOf(importanceStr.toUpperCase(Locale.ROOT).replace(" ", "_"));
        StatusType status = StatusType.AKTIVAN;

        return new Task(
                0L,
                "",
                name,
                description,
                0L, //TODO categoryId if used
                frequency,
                startDate,
                endDate,
                time,
                isWholeDay,
                interval,
                unit,
                difficulty,
                importance,
                status
        );
    }

    public long saveTask(Task task, String userId) {
        task.setUserId(userId);
        return taskRepository.insertTask(task, String.valueOf(userId));
    }

    private Task showError(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        return null;
    }

    public List<Task> getTasksById(String userId){
        List<Task> tasks = taskRepository.getTasksById(userId);
        return tasks;
    }

}
