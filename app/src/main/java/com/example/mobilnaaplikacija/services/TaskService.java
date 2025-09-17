package com.example.mobilnaaplikacija.services;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.example.mobilnaaplikacija.database.SQLiteHelper;
import com.example.mobilnaaplikacija.repository.TaskRepository;
import com.example.mobilnaaplikacija.model.*;
import com.google.firebase.auth.FirebaseUser;

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

    public String validate(String name, String category, boolean isRepeating, boolean isOneTime, String startDate, String endDate, String time, boolean isWholeDay, String difficultyStr, String importanceStr, String unitStr, String intervalStr) {
        FrequencyType frequency = isRepeating ? FrequencyType.PONAVLJAJUCI :
                (isOneTime ? FrequencyType.JEDNOKRATAN : null);

        if (name.isEmpty()) return "Unesite naziv zadatka!";
        if (category.isEmpty()) return "Izaberite kategoriju zadatka!";
        if (startDate.isEmpty()) return "Izaberite datum početka!";
        if (endDate.isEmpty()) return "Izaberite datum završetka!";
        if (time.isEmpty() && !isWholeDay) return "Unesite vrijeme zadatka!";
        if (frequency == null) return "Izaberite jednokratan ili ponavljajući zadatak!";
        if (difficultyStr.isEmpty()) return "Izaberite težinu zadatka!";
        if (importanceStr.isEmpty()) return "Izaberite bitnost zadatka!";


        if (frequency == FrequencyType.PONAVLJAJUCI) {
            if (unitStr.isEmpty()) return "Unesite jedinicu zadatka!";
            if (intervalStr.isEmpty()) return "Unesite broj ponavljanja zadatka!";
        }

        return null;
    }

    public Task add(Task task) {
        return taskRepository.add(task);
    }


    public Task update(Task task) {
        return taskRepository.update(task);
    }

    public List<Task> getTasksByUser(String userId){
        return taskRepository.getTasksByUser(userId);
    }
}
