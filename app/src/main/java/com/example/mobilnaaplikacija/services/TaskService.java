package com.example.mobilnaaplikacija.services;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.Nullable;

import com.example.mobilnaaplikacija.database.SQLiteHelper;
import com.example.mobilnaaplikacija.model.enums.FrequencyType;
import com.example.mobilnaaplikacija.model.enums.StatusType;
import com.example.mobilnaaplikacija.model.enums.UnitType;
import com.example.mobilnaaplikacija.repository.TaskRepository;
import com.example.mobilnaaplikacija.model.*;
import com.example.mobilnaaplikacija.utils.XpCalculator;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

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

    public ArrayList<Task> addRepeatingTask(Task task) {
        ArrayList<Task> taskOccurrences = new ArrayList<>();
        String taskId;
        if (task.getTaskId() == null) taskId = UUID.randomUUID().toString();
        else taskId = task.getTaskId();
        List<String> dates = getTaskOccurringDates(task);
        SimpleDateFormat fmt = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());

        //HH:mm iz start i end millis
        Calendar calStart = Calendar.getInstance();
        calStart.setTimeInMillis(task.getStartMillis());
        int startHour = calStart.get(Calendar.HOUR_OF_DAY);
        int startMinute = calStart.get(Calendar.MINUTE);

        Calendar calEnd = Calendar.getInstance();
        calEnd.setTimeInMillis(task.getEndMillis());
        int endHour = calEnd.get(Calendar.HOUR_OF_DAY);
        int endMinute = calEnd.get(Calendar.MINUTE);

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

                Task taskOccurrence = new Task();
                taskOccurrence.setId(task.getId());
                taskOccurrence.setUserId(task.getUserId());
                taskOccurrence.setTaskId(taskId);
                taskOccurrence.setName(task.getName());
                taskOccurrence.setDescription(task.getDescription());
                taskOccurrence.setCategoryId(task.getCategoryId());
                taskOccurrence.setFrequency(task.getFrequency());
                taskOccurrence.setStartMillis(startCal.getTimeInMillis());
                taskOccurrence.setEndMillis(endCal.getTimeInMillis());
                taskOccurrence.setInterval(task.getInterval());
                taskOccurrence.setUnit(task.getUnit());
                taskOccurrence.setDifficulty(task.getDifficulty());
                taskOccurrence.setImportance(task.getImportance());
                taskOccurrence.setStatus(task.getStatus());

                taskRepository.add(taskOccurrence);
                taskOccurrences.add(taskOccurrence);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return taskOccurrences;
    }

    public Task update(Task task) {
        return taskRepository.update(task);
    }

    public ArrayList<Task> updateFutureOccurrences(Task task) {
        ArrayList<Task> taskOccurrences = new ArrayList<>();
        Long now = System.currentTimeMillis();

        List<String> allDates = getTaskOccurringDates(task);

        //Filtrira danas i buduce datume
        List<String> futureDates = new ArrayList<>();
        SimpleDateFormat fmt = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());

        Calendar calStart = Calendar.getInstance();
        calStart.setTimeInMillis(task.getStartMillis());
        int startHour = calStart.get(Calendar.HOUR_OF_DAY);
        int startMinute = calStart.get(Calendar.MINUTE);

        for (String dateStr : allDates) {
            try {
                Date date = fmt.parse(dateStr);
                if (date == null) continue;

                Calendar startCal = Calendar.getInstance();
                startCal.setTime(date);
                startCal.set(Calendar.HOUR_OF_DAY, startHour);
                startCal.set(Calendar.MINUTE, startMinute);
                startCal.set(Calendar.SECOND, 0);
                startCal.set(Calendar.MILLISECOND, 0);

                //startMillis u buducnosti => update
                if (startCal.getTimeInMillis() > now) {
                    futureDates.add(dateStr);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        taskRepository.deleteFutureOccurrences(task.getTaskId(), System.currentTimeMillis());
        taskOccurrences = createTasksFromFutureDates(task, task.getTaskId(), futureDates);

        return taskOccurrences;
    }

    private ArrayList<Task> createTasksFromFutureDates(Task task, String taskId, List<String> dates) {
        ArrayList<Task> taskOccurrences = new ArrayList<>();
        SimpleDateFormat fmt = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());

        Calendar calStart = Calendar.getInstance();
        calStart.setTimeInMillis(task.getStartMillis());
        int startHour = calStart.get(Calendar.HOUR_OF_DAY);
        int startMinute = calStart.get(Calendar.MINUTE);

        Calendar calEnd = Calendar.getInstance();
        calEnd.setTimeInMillis(task.getEndMillis());
        int endHour = calEnd.get(Calendar.HOUR_OF_DAY);
        int endMinute = calEnd.get(Calendar.MINUTE);

        for (String dateStr : dates) {
            try {
                Date baseDate = fmt.parse(dateStr);
                if (baseDate == null) continue;

                Calendar startCal = Calendar.getInstance();
                startCal.setTime(baseDate);
                startCal.set(Calendar.HOUR_OF_DAY, startHour);
                startCal.set(Calendar.MINUTE, startMinute);
                startCal.set(Calendar.SECOND, 0);
                startCal.set(Calendar.MILLISECOND, 0);

                Calendar endCal = Calendar.getInstance();
                endCal.setTime(baseDate);
                endCal.set(Calendar.HOUR_OF_DAY, endHour);
                endCal.set(Calendar.MINUTE, endMinute);
                endCal.set(Calendar.SECOND, 0);
                endCal.set(Calendar.MILLISECOND, 0);

                Task taskOccurrence = new Task();
                taskOccurrence.setId(task.getId());
                taskOccurrence.setUserId(task.getUserId());
                taskOccurrence.setTaskId(taskId);
                taskOccurrence.setName(task.getName());
                taskOccurrence.setDescription(task.getDescription());
                taskOccurrence.setCategoryId(task.getCategoryId());
                taskOccurrence.setFrequency(task.getFrequency());
                taskOccurrence.setStartMillis(startCal.getTimeInMillis());
                taskOccurrence.setEndMillis(endCal.getTimeInMillis());
                taskOccurrence.setInterval(task.getInterval());
                taskOccurrence.setUnit(task.getUnit());
                taskOccurrence.setDifficulty(task.getDifficulty());
                taskOccurrence.setImportance(task.getImportance());
                taskOccurrence.setStatus(task.getStatus());

                taskRepository.add(taskOccurrence);
                taskOccurrences.add(taskOccurrence);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return taskOccurrences;
    }

    public List<Task> getTasksByUser(String userId){
        return taskRepository.getTasksByUser(userId);
    }

    public Boolean deleteById(String id){
        return taskRepository.delete(id) > 0;
    }

    public Boolean deleteFutureOccurrences(String id){
        long now = System.currentTimeMillis();
        return taskRepository.deleteFutureOccurrences(id, now);
    }

    public String validate(String name, String category, boolean isRepeating, boolean isOneTime, String startDate, String endDate, String startTime, String endTime, Long startMillis, Long endMillis, String difficultyStr, String importanceStr, String unitStr, String intervalStr) {
        FrequencyType frequency = isRepeating ? FrequencyType.PONAVLJAJUCI :
                (isOneTime ? FrequencyType.JEDNOKRATAN : null);

        if (name.isEmpty()) return "Unesite naziv zadatka!";
        if (category.equals("-1")) return "Izaberite kategoriju zadatka!";
        if (startDate.isEmpty()) return "Izaberite datum početka!";
        if (endDate.isEmpty()) return "Izaberite datum završetka!";
        if (startTime.isEmpty()) return "Izaberite vreme početka!";
        if (endTime.isEmpty()) return "Izaberite vreme završetka!";
        if (startMillis != -1L && endMillis != -1L && endMillis < startMillis) {
            return "Vreme i datum završetka moraju biti nakon početka!";
        }
        if (frequency == null) return "Izaberite jednokratan ili ponavljajući zadatak!";
        if (difficultyStr.isEmpty()) return "Izaberite težinu zadatka!";
        if (importanceStr.isEmpty()) return "Izaberite bitnost zadatka!";


        if (frequency == FrequencyType.PONAVLJAJUCI) {
            if (unitStr.isEmpty()) return "Unesite jedinicu zadatka!";
            if (intervalStr.isEmpty()) return "Unesite broj ponavljanja zadatka!";
        } else if (frequency == FrequencyType.JEDNOKRATAN && !startDate.equals(endDate)) {
            return "Jednokratan zadatak zahteva isti početni i krajnji datum!";
        }

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
                } else if (newStatus == StatusType.URAĐEN && System.currentTimeMillis() >= task.getStartMillis() && task.getEndMillis() != null) {
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

    public String isTimeValid(Long start, Long end, boolean isRepeating) {
        Long now = System.currentTimeMillis();
        if (start <= now) {
            return "Početak mora biti u budućnosti.";
        }

        if (end <= now) {
            return "Kraj mora biti u budućnosti.";
        }

        if (end <= start) {
            return "Kraj mora biti nakon početka.";
        }

        if (isRepeating) {
            Calendar calStart = Calendar.getInstance();
            calStart.setTimeInMillis(start);

            Calendar calEnd = Calendar.getInstance();
            calEnd.setTimeInMillis(end);

            int startHour = calStart.get(Calendar.HOUR_OF_DAY);
            int startMinute = calStart.get(Calendar.MINUTE);
            int endHour = calEnd.get(Calendar.HOUR_OF_DAY);
            int endMinute = calEnd.get(Calendar.MINUTE);

            if (endHour < startHour || (endHour == startHour && endMinute <= startMinute)) {
                return "Kraj mora biti nakon početka unutar istog dana.";
            }
        }
        return null;
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

    public List<String> getTaskOccurringDates(Task task) {
        List<String> dates = new ArrayList<>();
        SimpleDateFormat fmt = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
        fmt.setLenient(false);
        Calendar cal = Calendar.getInstance();

        if(task.getFrequency() == FrequencyType.JEDNOKRATAN) {
            try {
                Date date = parseMillisToDate(task.getStartMillis());
                if(date != null) {
                    cal.setTime(date);
                    dates.add(fmt.format(cal.getTime()));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                Date startDate = parseMillisToDate(task.getStartMillis());
                Date endDate = parseMillisToDate(task.getEndMillis());
                if (startDate == null || endDate == null)
                    return dates;

                cal.setTime(startDate);
                while (!cal.getTime().after(endDate)) {
                    dates.add(fmt.format(cal.getTime()));

                    if (task.getUnit() == UnitType.DAN) {
                        cal.add(Calendar.DAY_OF_MONTH, task.getInterval());
                    } else if (task.getUnit() == UnitType.SEDMICA) {
                        cal.add(Calendar.WEEK_OF_YEAR, task.getInterval());
                    } else if (task.getUnit() == UnitType.MJESEC) {
                        cal.add(Calendar.MONTH, task.getInterval());
                    } else {
                        cal.add(Calendar.YEAR, task.getInterval());
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return dates;
    }

    private Date parseMillisToDate(Long millis) {
        if (millis != null) {
            return new Date(millis);
        }
        return null;
    }

    public Pair<Long, Long> getTaskGroupBounds(String taskId) {
        return taskRepository.getTaskGroupBounds(taskId);
    }

    public boolean isInPast(Task task) {
        return task.getEndMillis() < System.currentTimeMillis();
    }

    public void updateRepeatingTaskStatus(String taskId, StatusType oldStatus, StatusType newStatus) {
        taskRepository.updateRepeatingTaskStatus(taskId, System.currentTimeMillis(), oldStatus, newStatus);
    }

    public void awardXP(Task task, FirebaseUser firebaseUser) {
        if (task == null || firebaseUser == null) return;
        if (task.getStatus() != StatusType.URAĐEN) return; //nije uradjen
        long now = System.currentTimeMillis();
        if (task.getStartMillis() > now) return; //nije ni pocao

        int xpToAward = XpCalculator.getTotalXP(task.getDifficulty(), task.getImportance());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(firebaseUser.getUid());

        db.runTransaction(transaction -> {
                    DocumentSnapshot snapshot = transaction.get(userRef);
                    long currentXP = snapshot.getLong("experiencePoints") != null ? snapshot.getLong("experiencePoints") : 0;
                    long newXP = currentXP + xpToAward;
                    transaction.update(userRef, "experiencePoints", newXP);
                    Log.d("XP", "Dodeljeno: " + xpToAward + " XP. Novi ukupni XP: " + newXP);
                    return null;
                }).addOnSuccessListener(aVoid -> Log.d("XP", "XP uspešno ažuriran u Firestore"))
                .addOnFailureListener(e -> Log.e("XP", "Greška pri dodeli XP: ", e));
    }

}
