package com.example.mobilnaaplikacija.services;

import android.content.Context;
import androidx.annotation.Nullable;

import com.example.mobilnaaplikacija.database.SQLiteHelper;
import com.example.mobilnaaplikacija.repository.TaskRepository;
import com.example.mobilnaaplikacija.model.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TaskService {

    private final Context context;
    private final TaskRepository taskRepository;

    public TaskService(Context context) {
        this.context = context;
        this.taskRepository = new TaskRepository(new SQLiteHelper(context));
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

    public Boolean deleteById(String id){
        return taskRepository.delete(id) > 0;
    }

    public String validate(String name, String category, boolean isRepeating, boolean isOneTime, String startDate, String endDate, String startTime, String endTime, Long startMillis, Long endMillis, String difficultyStr, String importanceStr, String unitStr, String intervalStr) {
        FrequencyType frequency = isRepeating ? FrequencyType.PONAVLJAJUCI :
                (isOneTime ? FrequencyType.JEDNOKRATAN : null);

        if (name.isEmpty()) return "Unesite naziv zadatka!";
        if (category.isEmpty()) return "Izaberite kategoriju zadatka!";
        if (startDate.isEmpty()) return "Izaberite datum početka!";
        if (startTime.isEmpty()) return "Izaberite vreme početka!";
        if (endDate.isEmpty()) return "Izaberite datum završetka!";
        if (endTime.isEmpty()) return "Izaberite vreme završetka!";
        if (startMillis != -1L && endMillis != -1L && endMillis < startMillis) {
            return "Vreme završetka mora biti nakon početka!";
        }
        if (frequency == null) return "Izaberite jednokratan ili ponavljajući zadatak!";
        if (difficultyStr.isEmpty()) return "Izaberite težinu zadatka!";
        if (importanceStr.isEmpty()) return "Izaberite bitnost zadatka!";


        if (frequency == FrequencyType.PONAVLJAJUCI) {
            if (unitStr.isEmpty()) return "Unesite jedinicu zadatka!";
            if (intervalStr.isEmpty()) return "Unesite broj ponavljanja zadatka!";
        }

        return null;
    }

    public String changeStatus (Task task, StatusType newStatus) {
        autoUpdateStatus(task);

        if (!canChangeStatus(task, newStatus)) {
            return "Nevažeća promena statusa.";
        }

        task.setStatus(newStatus);
        taskRepository.update(task);
        return null;
    }

    public Task autoUpdateStatus (Task task) {
        long now = System.currentTimeMillis();
        long threeDaysMills = 3L * 24 * 60 * 60 * 1000;
        if (now - threeDaysMills > task.getEndMillis()) {
            task.setStatus(StatusType.NEURAĐEN);
            taskRepository.update(task);
        }
        return task;
    }

    public boolean canChangeStatus(Task task, StatusType newStatus) {
        StatusType currentStatus = task.getStatus();
        FrequencyType currentFreq = task.getFrequency();

        if (currentStatus == StatusType.NEURAĐEN || currentStatus == StatusType.OTKAZAN || currentStatus == StatusType.URAĐEN) {
            return false;
        }

        switch (currentStatus) {
            case AKTIVAN:
                if (newStatus == StatusType.PAUZIRAN && currentFreq == FrequencyType.PONAVLJAJUCI) {
                    return true;
                } else if (newStatus == StatusType.URAĐEN && System.currentTimeMillis() >= task.getEndMillis() && task.getEndMillis() != null) {
                    return true;
                } else if (newStatus == StatusType.OTKAZAN)
                    return true;
                return false;
            case PAUZIRAN:
                return newStatus == StatusType.AKTIVAN;
            default:
                return false;
        }
    }

    public String isStatusValid (String status, Long start, Long end) {
        if (status.isEmpty()) return "Izaberite status zadatka!";

        if (status.equals(StatusType.URAĐEN.getDisplayName())) {
            Long now = System.currentTimeMillis();
            if (end > now) {
                return "Ne možete označiti zadatak kao urađen pre nego istekne kraj.";
            }
        }
        return null;
    }

    public boolean isTimeValid (Long start, Long end) {
        if(start > 0 && end > 0 && start > end) return false;

        Calendar startCal = Calendar.getInstance();
        startCal.setTimeInMillis(start);
        int hourSt = startCal.get(Calendar.HOUR_OF_DAY);
        int minSt = startCal.get(Calendar.MINUTE);
        Calendar endCal = Calendar.getInstance();
        endCal.setTimeInMillis(end);
        int hourEnd = endCal.get(Calendar.HOUR_OF_DAY);
        int minEnd = endCal.get(Calendar.MINUTE);

        if (hourEnd < hourSt || (hourSt == hourEnd && minEnd <= minSt)) {
            return false;
        }
        return true;
    }

    public ArrayList<Task> filterByFrequency(ArrayList<Task> tasks, @Nullable FrequencyType type) {
        if (type == null)
            return tasks;

        ArrayList<Task> filteredTasks = new ArrayList<>();
        for (Task t : tasks) {
            if (t.getFrequency() == type) {
                filteredTasks.add(t);
            }
        }
        return filteredTasks;
    }

    public Long copyDateButKeepTime(Long sourceDateMillis, Long targetDateMillis) {
        Calendar source = Calendar.getInstance();
        source.setTimeInMillis(sourceDateMillis);

        Calendar target = Calendar.getInstance();
        target.setTimeInMillis(targetDateMillis);

        source.set(Calendar.YEAR, target.get(Calendar.YEAR));
        source.set(Calendar.MONTH, target.get(Calendar.MONTH));
        source.set(Calendar.DAY_OF_MONTH, target.get(Calendar.DAY_OF_MONTH));

        return source.getTimeInMillis();
    }

    public ArrayList<Task> filterCurrentFutureTasks (ArrayList<Task> tasks) {
        ArrayList<Task> filtered = new ArrayList<>();
        long now = System.currentTimeMillis();

        for (Task task : tasks) {
            if (task.getEndMillis() >= now) {
                filtered.add(task);
            }
        }
        return filtered;
    }

    public void deleteByCategory(String categoryId, String userId) {
        ArrayList<Task> tasks = new ArrayList<>(getTasksByUser(userId));
        for (Task task : tasks) {
            if (task.getCategoryId().equals(categoryId))
                deleteById(task.getId());
        }
    }
}
