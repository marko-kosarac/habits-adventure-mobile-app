package com.example.mobilnaaplikacija.services;

import android.content.Context;

import com.example.mobilnaaplikacija.database.SQLiteHelper;
import com.example.mobilnaaplikacija.model.Task;
import com.example.mobilnaaplikacija.model.TaskOccurrence;
import com.example.mobilnaaplikacija.repository.TaskOccurrenceRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskOccurrenceService {

    private final Context context;
    private final TaskOccurrenceRepository taskOccurrenceRepository;
    private TaskService taskService;

    public TaskOccurrenceService(Context context) {
        this.context = context;
        this.taskOccurrenceRepository = new TaskOccurrenceRepository(new SQLiteHelper(context));
        taskService = new TaskService(context);
    }

    public TaskOccurrence add(TaskOccurrence taskOccurrence) {
        return taskOccurrenceRepository.add(taskOccurrence);
    }


    public TaskOccurrence update(TaskOccurrence taskOccurrence) {
        return taskOccurrenceRepository.update(taskOccurrence);
    }

    public List<TaskOccurrence> getTaskOccurencesByTask(String taskId){
        return taskOccurrenceRepository.getTaskOccurrencesByTask(taskId);
    }

    public Boolean deleteById(String id){
        return taskOccurrenceRepository.delete(id) > 0;
    }

    public void deleteByTask(String taskId) {
        ArrayList<TaskOccurrence> taskOccurrences = new ArrayList<>(getTaskOccurencesByTask(taskId));
        for (TaskOccurrence occurrence : taskOccurrences) {
            if (occurrence.getTaskId().equals(taskId))
                deleteById(occurrence.getId());
        }
    }


    public ArrayList<TaskOccurrence> generateOccurrences(Task task) {
        ArrayList<TaskOccurrence> occurrences = new ArrayList<>();
        List<String> dates = taskService.getTaskOcurringDates(task);

        //HH:mm iz start i end millis
        Calendar calStart = Calendar.getInstance();
        calStart.setTimeInMillis(task.getStartMillis());
        int startHour = calStart.get(Calendar.HOUR_OF_DAY);
        int startMinute = calStart.get(Calendar.MINUTE);

        Calendar calEnd = Calendar.getInstance();
        calEnd.setTimeInMillis(task.getEndMillis());
        int endHour = calEnd.get(Calendar.HOUR_OF_DAY);
        int endMinute = calEnd.get(Calendar.MINUTE);

        SimpleDateFormat fmt = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());

        for (String date : dates) {
            try {
                Date baseDate = fmt.parse(date);
                if (baseDate == null) continue;

                Calendar startCal = Calendar.getInstance();
                startCal.setTime(baseDate);
                startCal.set(Calendar.HOUR_OF_DAY, startHour);
                startCal.set(Calendar.MINUTE, startMinute);
                startCal.set(Calendar.SECOND, 0);

                Calendar endCal = Calendar.getInstance();
                endCal.setTime(baseDate);
                endCal.set(Calendar.HOUR_OF_DAY, endHour);
                endCal.set(Calendar.MINUTE, endMinute);
                endCal.set(Calendar.SECOND, 0);

                TaskOccurrence occurrence = new TaskOccurrence();
                occurrence.setTaskId(task.getId());
                occurrence.setName(task.getName());
                occurrence.setDescription(task.getDescription());
                occurrence.setStartMillis(startCal.getTimeInMillis());
                occurrence.setEndMillis(endCal.getTimeInMillis());
                occurrence.setStatus(task.getStatus());

                taskOccurrenceRepository.add(occurrence);
                occurrences.add(occurrence);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return occurrences;
    }
}
