package com.example.mobilnaaplikacija.services;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.Nullable;

import com.example.mobilnaaplikacija.database.SQLiteHelper;
import com.example.mobilnaaplikacija.model.enums.DifficultyType;
import com.example.mobilnaaplikacija.model.enums.FrequencyType;
import com.example.mobilnaaplikacija.model.enums.ImportanceType;
import com.example.mobilnaaplikacija.model.enums.StatusType;
import com.example.mobilnaaplikacija.model.enums.UnitType;
import com.example.mobilnaaplikacija.repository.TaskRepository;
import com.example.mobilnaaplikacija.model.*;
import com.example.mobilnaaplikacija.utils.XpCalculator;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class TaskService {

    private final Context context;
    private final TaskRepository taskRepository;
    private XPAwardListener xpAwardListener;
    private FirebaseFirestore db;
    public TaskService(Context context) {
        this.context = context;
        this.taskRepository = new TaskRepository(new SQLiteHelper(context));
        this.db = FirebaseFirestore.getInstance();
    }

    public void setXPAwardListener(XPAwardListener listener) {
        this.xpAwardListener = listener;
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
                taskOccurrence.setGroupStartMillis(task.getGroupStartMillis());
                taskOccurrence.setGroupEndMillis(task.getGroupEndMillis());
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
                taskOccurrence.setGroupStartMillis(task.getGroupStartMillis());
                taskOccurrence.setGroupEndMillis(task.getGroupEndMillis());
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

    public boolean isInPast(Task task) {
        return task.getEndMillis() < System.currentTimeMillis();
    }

    public void updateRepeatingTaskStatus(String taskId, StatusType oldStatus, StatusType newStatus) {
        taskRepository.updateRepeatingTaskStatus(taskId, System.currentTimeMillis(), oldStatus, newStatus);
    }

    public void awardXP(Task task, FirebaseUser firebaseUser) {
        if (task == null || firebaseUser == null) return;
        if (task.getStatus() != StatusType.URAĐEN) return; //mora biti: uradjen
        long now = System.currentTimeMillis();
        if (task.getStartMillis() > now) return; //mora biti: u toku/zavrsen

        final int diffXp = XpCalculator.getDifficultyXP(task.getDifficulty());
        final int impXp  = XpCalculator.getImportanceXP(task.getImportance());
        String userId = firebaseUser.getUid();

        checkQuotaAndUpdateXP(this.db, userId, task, diffXp, impXp);
    }

    private void checkQuotaAndUpdateXP(FirebaseFirestore db, String userId, Task task, int diffXp, int impXp) {
        String dayId = getPeriodId("daily");
        String weekId = getPeriodId("weekly");
        String monthId = getPeriodId("monthly");

        DocumentReference dayRef = db.collection("users").document(userId).collection("xpLogs").document(dayId);
        DocumentReference weekRef = db.collection("users").document(userId).collection("xpLogs").document(weekId);
        DocumentReference monthRef = db.collection("users").document(userId).collection("xpLogs").document(monthId);


        Tasks.whenAllSuccess(dayRef.get(), weekRef.get(), monthRef.get())
                .addOnSuccessListener(results -> {
                    DocumentSnapshot daySnap = (DocumentSnapshot) results.get(0);
                    DocumentSnapshot weekSnap = (DocumentSnapshot) results.get(1);
                    DocumentSnapshot monthSnap = (DocumentSnapshot) results.get(2);

                    boolean allowDiff = isDifficultyWithinQuota(daySnap, weekSnap, task.getDifficulty());
                    boolean allowImp  = isImportanceWithinQuota(daySnap, monthSnap, task.getImportance());
                    int awardedDiffXp = allowDiff ? diffXp : 0;
                    int awardedImpXp  = allowImp ? impXp : 0;
                    int totalAwardedXp = awardedDiffXp + awardedImpXp;

                    if (totalAwardedXp == 0) {
                        if (xpAwardListener != null) xpAwardListener.onXPAwarded(0, allowDiff, allowImp);
                        logCurrentQuotas(db, userId); //TODO
                        return;
                    }
                    updateXPAndLog(db, userId, task, totalAwardedXp, allowDiff, allowImp, dayRef, weekRef, monthRef);
                })
                .addOnFailureListener(e -> {
                    Log.e("XP", "Error fetching quota docs", e);
                    if (xpAwardListener != null) xpAwardListener.onXPAwardFailed("Greška pri proveri kvote.");
                });
    }

    private boolean isDifficultyWithinQuota(DocumentSnapshot day, DocumentSnapshot week, DifficultyType difficulty) {
        int dailyDifficultyCount = getCount(day, "difficultyCounts", difficulty.name());
        int weeklyDifficultyCount = getCount(week, "difficultyCounts", difficulty.name());

        switch (difficulty) {
            case VEOMA_LAK:
            case LAK:
                return dailyDifficultyCount < 5;
            case TEŽAK:
                return dailyDifficultyCount < 2;
            case EKSTREMNO_TEŽAK:
                return weeklyDifficultyCount < 1;
            default:
                return true;
        }
    }

    private boolean isImportanceWithinQuota(DocumentSnapshot day, DocumentSnapshot month, ImportanceType importance) {
        int dailyImportanceCount = getCount(day, "importanceCounts", importance.name());
        int monthlyImportanceCount = getCount(month, "importanceCounts", importance.name());

        switch (importance) {
            case NORMALAN:
            case VAŽAN:
                return dailyImportanceCount < 5;
            case EKSTREMNO_VAŽAN:
                return dailyImportanceCount < 2;
            case SPECIJALAN:
                return monthlyImportanceCount < 1;
            default:
                return true;
        }
    }

    private int getCount(DocumentSnapshot snapshot, String mapName, String key) {
        if (snapshot == null || !snapshot.exists()) return 0;
        Map<String, Object> map = (Map<String, Object>) snapshot.get(mapName);
        if (map == null) return 0;
        Object val = map.get(key);
        return (val instanceof Number) ? ((Number) val).intValue() : 0;
    }

    private void updateXPAndLog(FirebaseFirestore db, String userId, Task task, int totalXp, boolean allowDiff, boolean allowImp, DocumentReference dayRef, DocumentReference weekRef, DocumentReference monthRef) {

        DocumentReference userRef = db.collection("users").document(userId);

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot userSnap = transaction.get(userRef);
            DocumentSnapshot daySnap = transaction.get(dayRef);
            DocumentSnapshot weekSnap = transaction.get(weekRef);
            DocumentSnapshot monthSnap = transaction.get(monthRef);

            transaction.update(userRef, "experiencePoints", FieldValue.increment(totalXp)); //TODO

            if (allowDiff || allowImp) {
                if (task.getDifficulty() == DifficultyType.VEOMA_LAK || task.getDifficulty() == DifficultyType.LAK ||
                        task.getDifficulty() == DifficultyType.TEŽAK || task.getImportance() == ImportanceType.NORMALAN || task.getImportance() == ImportanceType.VAŽAN ||
                        task.getImportance() == ImportanceType.EKSTREMNO_VAŽAN) {
                    updateQuotaDocInTransaction(transaction, dayRef, daySnap, task, allowDiff, allowImp);
                }

                if (task.getDifficulty() == DifficultyType.EKSTREMNO_TEŽAK) {
                    updateQuotaDocInTransaction(transaction, weekRef, weekSnap, task, allowDiff, allowImp);
                }

                if (task.getImportance() == ImportanceType.SPECIJALAN) {
                    updateQuotaDocInTransaction(transaction, monthRef, monthSnap, task, allowDiff, allowImp);
                }
            }

            Map<String, Object> historyEntry = new HashMap<>();
            historyEntry.put("taskId", task.getTaskId());
            historyEntry.put("at", new Timestamp(new Date()));
            historyEntry.put("xp", totalXp);
            historyEntry.put("difficulty", task.getDifficulty().name());
            historyEntry.put("importance", task.getImportance().name());
            transaction.update(userRef, "xpHistory", FieldValue.arrayUnion(historyEntry));

            transaction.update(userRef, "lastAwardedXP", totalXp);
            transaction.update(userRef, "lastAwardedAt", FieldValue.serverTimestamp());
            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d("XP", "User awarded " + totalXp + " XP");
            if (xpAwardListener != null) xpAwardListener.onXPAwarded(totalXp, allowDiff, allowImp);
            logCurrentQuotas(db, userId); //TODO
        }).addOnFailureListener(e -> {
            Log.e("XP", "XP transaction failed", e);
            if (xpAwardListener != null) xpAwardListener.onXPAwardFailed("Greška pri dodeli XP.");
            logCurrentQuotas(db, userId); //TODO
        });
    }

    private void updateQuotaDocInTransaction(Transaction transaction, DocumentReference ref, DocumentSnapshot snapshot, Task task, boolean incDiff, boolean incImp) throws FirebaseFirestoreException {
        String diffKey = task.getDifficulty().name();
        String impKey = task.getImportance().name();

        if (snapshot != null && snapshot.exists()) {
            if (incDiff) {
                transaction.update(ref, "difficultyCounts." + diffKey, FieldValue.increment(1));
            }
            if (incImp) {
                transaction.update(ref, "importanceCounts." + impKey, FieldValue.increment(1));
            }
        } else {
            Map<String, Object> diffMap = new HashMap<>();
            Map<String, Object> impMap = new HashMap<>();
            if (incDiff) diffMap.put(diffKey, 1);
            if (incImp) impMap.put(impKey, 1);

            Map<String, Object> doc = new HashMap<>();
            doc.put("difficultyCounts", diffMap);
            doc.put("importanceCounts", impMap);
            doc.put("updatedAt", FieldValue.serverTimestamp());

            transaction.set(ref, doc, SetOptions.merge());
        }
    }

    private String getPeriodId(String periodType) {
        Calendar cal = Calendar.getInstance();

        switch (periodType) {
            case "daily":
                return String.format(Locale.US, "%04d-%02d-%02d",
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH) + 1,
                        cal.get(Calendar.DAY_OF_MONTH));
            case "weekly":
                int week = cal.get(Calendar.WEEK_OF_YEAR);
                return String.format(Locale.US, "%04d-W%02d", cal.get(Calendar.YEAR), week);
            case "monthly":
                return String.format(Locale.US, "%04d-%02d",
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH) + 1);
            default:
                return "";
        }
    }

    public interface XPAwardListener {
        void onXPAwarded(int xp, boolean diffAwarded, boolean impAwarded);
        void onXPAwardFailed(String error);
    }

    public void logCurrentQuotas(FirebaseFirestore db, String userId) {
        String dayId = getPeriodId("daily");
        String weekId = getPeriodId("weekly");
        String monthId = getPeriodId("monthly");

        DocumentReference dayRef = db.collection("users").document(userId)
                .collection("xpLogs").document(dayId);
        DocumentReference weekRef = db.collection("users").document(userId)
                .collection("xpLogs").document(weekId);
        DocumentReference monthRef = db.collection("users").document(userId)
                .collection("xpLogs").document(monthId);

        Tasks.whenAllSuccess(dayRef.get(), weekRef.get(), monthRef.get())
                .addOnSuccessListener(results -> {
                    DocumentSnapshot daySnap = (DocumentSnapshot) results.get(0);
                    DocumentSnapshot weekSnap = (DocumentSnapshot) results.get(1);
                    DocumentSnapshot monthSnap = (DocumentSnapshot) results.get(2);

                    Log.d("XP_LOG", "=== Current Quotas ===");
                    Log.d("XP_LOG", "Daily: " + (daySnap.exists() ? daySnap.getData() : "empty"));
                    Log.d("XP_LOG", "Weekly: " + (weekSnap.exists() ? weekSnap.getData() : "empty"));
                    Log.d("XP_LOG", "Monthly: " + (monthSnap.exists() ? monthSnap.getData() : "empty"));
                    Log.d("XP_LOG", "=====================");
                })
                .addOnFailureListener(e -> Log.e("XP_LOG", "Failed to fetch quota docs", e));
    }

}
