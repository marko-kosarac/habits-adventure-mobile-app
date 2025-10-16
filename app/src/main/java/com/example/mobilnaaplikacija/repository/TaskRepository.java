package com.example.mobilnaaplikacija.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.mobilnaaplikacija.database.SQLiteHelper;
import com.example.mobilnaaplikacija.model.enums.DifficultyType;
import com.example.mobilnaaplikacija.model.enums.FrequencyType;
import com.example.mobilnaaplikacija.model.enums.ImportanceType;
import com.example.mobilnaaplikacija.model.enums.StatusType;
import com.example.mobilnaaplikacija.model.Task;
import com.example.mobilnaaplikacija.model.enums.UnitType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TaskRepository {
    private final SQLiteHelper dbHelper;

    public TaskRepository(SQLiteHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public Task add(Task task){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.COLUMN_USER_ID, task.getUserId());
        values.put(SQLiteHelper.COLUMN_TASK_ID, task.getTaskId());
        values.put(SQLiteHelper.COLUMN_TASK_NAME, task.getName());
        values.put(SQLiteHelper.COLUMN_TASK_DESCRIPTION, task.getDescription());
        values.put(SQLiteHelper.COLUMN_TASK_CATEGORY_ID, task.getCategoryId());
        values.put(SQLiteHelper.COLUMN_FREQUENCY, task.getFrequency().name());
        values.put(SQLiteHelper.COLUMN_START_MILLIS, task.getStartMillis());
        values.put(SQLiteHelper.COLUMN_END_MILLIS, task.getEndMillis());
        values.put(SQLiteHelper.COLUMN_GROUP_START_MILLIS, task.getGroupStartMillis());
        values.put(SQLiteHelper.COLUMN_GROUP_END_MILLIS, task.getGroupEndMillis());
        values.put(SQLiteHelper.COLUMN_INTERVAL, task.getInterval());
        values.put(SQLiteHelper.COLUMN_UNIT, task.getUnit() != null ? task.getUnit().name() : null);
        values.put(SQLiteHelper.COLUMN_DIFFICULTY, task.getDifficulty().name());
        values.put(SQLiteHelper.COLUMN_IMPORTANCE, task.getImportance().name());
        values.put(SQLiteHelper.COLUMN_STATUS, task.getStatus().name());
        values.put(SQLiteHelper.COLUMN_STATUS_TIMESTAMP, task.getStatusTimestamp());
        values.put(SQLiteHelper.COLUMN_QUOTA_REACHED, task.isQuotaReached());

        long rowId = db.insert(SQLiteHelper.TABLE_TASKS, null, values);
        db.close();

        if (rowId == -1)
            return null;

        task.setId(String.valueOf(rowId));
        return task;
    }

    public Task update(Task task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.COLUMN_USER_ID, task.getUserId());
        values.put(SQLiteHelper.COLUMN_TASK_ID, task.getTaskId());
        values.put(SQLiteHelper.COLUMN_TASK_NAME, task.getName());
        values.put(SQLiteHelper.COLUMN_TASK_DESCRIPTION, task.getDescription());
        values.put(SQLiteHelper.COLUMN_TASK_CATEGORY_ID, task.getCategoryId());
        values.put(SQLiteHelper.COLUMN_FREQUENCY, task.getFrequency().name());
        values.put(SQLiteHelper.COLUMN_START_MILLIS, task.getStartMillis());
        values.put(SQLiteHelper.COLUMN_END_MILLIS, task.getEndMillis());
        values.put(SQLiteHelper.COLUMN_GROUP_START_MILLIS, task.getGroupStartMillis());
        values.put(SQLiteHelper.COLUMN_GROUP_END_MILLIS, task.getGroupEndMillis());
        values.put(SQLiteHelper.COLUMN_INTERVAL, task.getInterval());
        values.put(SQLiteHelper.COLUMN_UNIT, task.getUnit() != null ? task.getUnit().name() : null);
        values.put(SQLiteHelper.COLUMN_DIFFICULTY, task.getDifficulty().name());
        values.put(SQLiteHelper.COLUMN_IMPORTANCE, task.getImportance().name());
        values.put(SQLiteHelper.COLUMN_STATUS, task.getStatus().name());
        values.put(SQLiteHelper.COLUMN_STATUS_TIMESTAMP, task.getStatusTimestamp());
        values.put(SQLiteHelper.COLUMN_QUOTA_REACHED, task.isQuotaReached());

        db.update(SQLiteHelper.TABLE_TASKS, values,
                SQLiteHelper.COLUMN_TASK_OCCURRENCE_ID + " = ?",
                new String[]{task.getId()});
        db.close();

        return task;
    }

    public List<Task> getTasksByUser(String userId){
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(SQLiteHelper.TABLE_TASKS, null,
                SQLiteHelper.COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)},
                null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Task task = new Task();
                task.setId(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_TASK_OCCURRENCE_ID)));
                task.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_USER_ID)));
                task.setTaskId(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_TASK_ID)));
                task.setName(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_TASK_NAME)));
                task.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_TASK_DESCRIPTION)));
                task.setCategoryId(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_TASK_CATEGORY_ID)));
                task.setFrequency(FrequencyType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_FREQUENCY))));
                task.setStartMillis(cursor.getLong(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_START_MILLIS)));
                task.setEndMillis(cursor.getLong(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_END_MILLIS)));
                task.setGroupStartMillis(cursor.getLong(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_GROUP_START_MILLIS)));
                task.setGroupEndMillis(cursor.getLong(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_GROUP_END_MILLIS)));
                task.setInterval(cursor.getInt(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_INTERVAL)));
                task.setStatusTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_STATUS_TIMESTAMP)));
                task.setQuotaReached(cursor.getInt(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_QUOTA_REACHED)) == 1);
                int unitIndex = cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_UNIT);
                String unitStr = cursor.getString(unitIndex);

                if (unitStr != null) {
                    task.setUnit(UnitType.valueOf(unitStr));
                } else {
                    task.setUnit(null);
                }
                task.setDifficulty(DifficultyType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_DIFFICULTY))));
                task.setImportance(ImportanceType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_IMPORTANCE))));
                task.setStatus(StatusType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_STATUS))));
                tasks.add(task);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return tasks;
    }

    public int delete(String id){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(SQLiteHelper.TABLE_TASKS, SQLiteHelper.COLUMN_TASK_OCCURRENCE_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    public Map<String, Integer> getTaskCountsByStatus(String userId) {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("AKTIVNI", 0);
        counts.put("URAĐENI", 0);
        counts.put("NEURAĐENI", 0);
        counts.put("OTKAZANI", 0);

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT status, COUNT(*) as count FROM " + SQLiteHelper.TABLE_TASKS +
                        " WHERE " + SQLiteHelper.COLUMN_USER_ID + " = ? GROUP BY status",
                new String[]{userId}
        );

        if (cursor.moveToFirst()) {
            do {
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                int count = cursor.getInt(cursor.getColumnIndexOrThrow("count"));

                switch (status) {
                    case "URAĐEN":
                        counts.put("URAĐENI", count);
                        break;
                    case "NEURAĐEN":
                        counts.put("NEURAĐENI", count);
                        break;
                    case "OTKAZAN":
                        counts.put("OTKAZANI", count);
                        break;
                    case "AKTIVAN":
                        counts.put("AKTIVNI", count);
                        break;
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return counts;
    }

    public void updateStatus(String taskId, StatusType newStatus) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.COLUMN_STATUS, newStatus.name());

        db.update(SQLiteHelper.TABLE_TASKS,
                values,
                SQLiteHelper.COLUMN_TASK_ID + " = ?",
                new String[]{taskId});

        db.close();
    }

    public int getLongestStreak() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT start_millis FROM "
                + SQLiteHelper.TABLE_TASKS +
                " WHERE " + SQLiteHelper.COLUMN_STATUS + " = 'URAĐEN' " +
                "ORDER BY " + SQLiteHelper.COLUMN_START_MILLIS + " ASC", null);

        int longestStreak = 0;
        int currentStreak = 0;
        long lastDay = -1;

        if (cursor.moveToFirst()) {
            do {
                long millis = cursor.getLong(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_START_MILLIS));
                long fakeDay = millis / 30000; // 30 sekundi = jedan dan

                if (lastDay == -1) {
                    currentStreak = 1;
                    longestStreak = 1;
                } else if (fakeDay == lastDay) {
                    // isti "dan" → ne povećava streak
                } else if (fakeDay == lastDay + 1) {
                    // sledeći dan
                    currentStreak++;
                    if (currentStreak > longestStreak) {
                        longestStreak = currentStreak;
                    }
                } else {
                    // preskočen dan
                    currentStreak = 1;
                }

                lastDay = fakeDay;
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return longestStreak;
    }

    public Map<String, Integer> getCompletedTasksWithColors(String userId) {
        Map<String, Integer> result = new LinkedHashMap<>();
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String query = "SELECT c." + SQLiteHelper.COLUMN_CATEGORY_NAME + " as category_name, " +
                "c." + SQLiteHelper.COLUMN_CATEGORY_COLOR + " as color, COUNT(*) as count " +
                "FROM " + SQLiteHelper.TABLE_TASKS + " t " +
                "JOIN " + SQLiteHelper.TABLE_CATEGORIES + " c " +
                "ON t." + SQLiteHelper.COLUMN_TASK_CATEGORY_ID + " = c." + SQLiteHelper.COLUMN_CATEGORY_ID + " " +
                "WHERE t." + SQLiteHelper.COLUMN_USER_ID + " = ? AND t." + SQLiteHelper.COLUMN_STATUS + " = ? " +
                "GROUP BY c." + SQLiteHelper.COLUMN_CATEGORY_NAME;

        Cursor cursor = db.rawQuery(query, new String[]{userId, StatusType.URAĐEN.name()});

        if (cursor.moveToFirst()) {
            do {
                String categoryName = cursor.getString(cursor.getColumnIndexOrThrow("category_name"));
                int color = cursor.getInt(cursor.getColumnIndexOrThrow("color"));
                int count = cursor.getInt(cursor.getColumnIndexOrThrow("count"));
                result.put(categoryName + ":" + color, count);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return result;
    }

    public Map<String, Integer> getCompletedTasksByCategory(String userId) {
        Map<String, Integer> result = new HashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT c." + SQLiteHelper.COLUMN_CATEGORY_NAME + " as category_name, COUNT(*) as count " +
                "FROM " + SQLiteHelper.TABLE_TASKS + " t " +
                "JOIN " + SQLiteHelper.TABLE_CATEGORIES + " c " +
                "ON t." + SQLiteHelper.COLUMN_TASK_CATEGORY_ID + " = c." + SQLiteHelper.COLUMN_CATEGORY_ID + " " +
                "WHERE t." + SQLiteHelper.COLUMN_USER_ID + " = ? AND t." + SQLiteHelper.COLUMN_STATUS + " = ? " +
                "GROUP BY c." + SQLiteHelper.COLUMN_CATEGORY_NAME;

        Cursor cursor = db.rawQuery(query, new String[]{userId, StatusType.URAĐEN.name()});

        if (cursor.moveToFirst()) {
            do {
                String categoryName = cursor.getString(cursor.getColumnIndexOrThrow("category_name"));
                int count = cursor.getInt(cursor.getColumnIndexOrThrow("count"));
                result.put(categoryName, count);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return result;
    }

    // Prosečan XP završenih zadataka
//    public float getAverageXPOfCompletedTasks(String userId) {
//        SQLiteDatabase db = dbHelper.getReadableDatabase();
//
//        Cursor cursor = db.rawQuery(
//                "SELECT difficulty FROM " + SQLiteHelper.TABLE_TASKS +
//                        " WHERE user_id = ? AND status = ?",
//                new String[]{userId, StatusType.URAĐEN.name()}
//        );
//
//        int totalXP = 0;
//        int count = 0;
//
//        if (cursor.moveToFirst()) {
//            do {
//                String difficultyStr = cursor.getString(cursor.getColumnIndexOrThrow("difficulty"));
//                DifficultyType difficulty = DifficultyType.valueOf(difficultyStr);
//                totalXP += getXPFromDifficulty(difficulty);
//                count++;
//            } while (cursor.moveToNext());
//        }
//
//        cursor.close();
//        db.close();
//
//        return count > 0 ? (float) totalXP / count : 0f;
//    }
//
//    private int getXPFromDifficulty(DifficultyType difficulty) {
//        switch (difficulty) {
//            case VEOMA_LAK: return 1;
//            case LAK: return 3;
//            case TEŽAK: return 7;
//            case EKSTREMNO_TEŽAK: return 20;
//            default: return 0;
//        }
//    }

    public List<Task> getCompletedTasks(String userId) {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(SQLiteHelper.TABLE_TASKS, null,
                SQLiteHelper.COLUMN_USER_ID + " = ? AND " + SQLiteHelper.COLUMN_STATUS + " = ?",
                new String[]{userId, StatusType.URAĐEN.name()},
                null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Task task = new Task();
                task.setId(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_TASK_ID)));
                task.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_USER_ID)));
                task.setName(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_TASK_NAME)));
                task.setDifficulty(DifficultyType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_DIFFICULTY))));
                tasks.add(task);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return tasks;
    }

    public float getAverageXPOfCompletedTasks(String userId) {
        List<Task> tasks = getCompletedTasks(userId);
        int totalXP = 0;

        for (Task t : tasks) {
            totalXP += getXPFromDifficulty(t.getDifficulty());
        }

        return tasks.size() > 0 ? (float) totalXP / tasks.size() : 0f;
    }

    public int getXPFromDifficulty(DifficultyType difficulty) {
        switch (difficulty) {
            case VEOMA_LAK: return 1;
            case LAK: return 3;
            case TEŽAK: return 7;
            case EKSTREMNO_TEŽAK: return 20;
            default: return 0;
        }
    }

    public DifficultyType getDifficultyFromXP(float xp) {
        if (xp <= 2) return DifficultyType.VEOMA_LAK;
        if (xp <= 5) return DifficultyType.LAK;
        if (xp <= 13) return DifficultyType.TEŽAK;
        return DifficultyType.EKSTREMNO_TEŽAK;
    }

    public boolean deleteFutureOccurrences(String taskId, long now) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //start >= now
        int deleted = db.delete(
                SQLiteHelper.TABLE_TASKS,
                "task_id = ? AND (start_millis >= ? OR (start_millis <= ? AND end_millis >= ?))",
                new String[]{taskId, String.valueOf(now), String.valueOf(now), String.valueOf(now)}
        );

        db.close();
        return deleted > 0;
    }

    public void updateRepeatingTaskStatus(String taskId, long now, StatusType oldStatus, StatusType newStatus) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.COLUMN_STATUS, newStatus.name());

        db.update(
                SQLiteHelper.TABLE_TASKS,
                values,
                SQLiteHelper.COLUMN_TASK_ID + " = ? AND " +
                        SQLiteHelper.COLUMN_START_MILLIS + " > ? AND " +
                        SQLiteHelper.COLUMN_STATUS + " = ?",
                new String[]{taskId, String.valueOf(now), oldStatus.name()}
        );

        db.close();
    }

    public List<Task> getTasksForEtapa(String userId, Long start, Long end) {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                SQLiteHelper.TABLE_TASKS,
                null,
                SQLiteHelper.COLUMN_USER_ID + " = ? AND " +
                        SQLiteHelper.COLUMN_STATUS_TIMESTAMP + " >= ? AND " +
                        SQLiteHelper.COLUMN_STATUS_TIMESTAMP + " <= ? AND " +
                        SQLiteHelper.COLUMN_STATUS + " IN (?, ?, ?) AND " +
                        SQLiteHelper.COLUMN_QUOTA_REACHED + " = 0",
                new String[]{
                        userId,
                        String.valueOf(start),
                        String.valueOf(end),
                        StatusType.AKTIVAN.name(),
                        StatusType.URAĐEN.name(),
                        StatusType.NEURAĐEN.name()
                },
                null, null, null
        )) {
            if (cursor.moveToFirst()) {
                do {
                    Task task = new Task();
                    task.setId(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_TASK_OCCURRENCE_ID)));
                    task.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_USER_ID)));
                    task.setTaskId(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_TASK_ID)));
                    task.setName(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_TASK_NAME)));
                    task.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_TASK_DESCRIPTION)));
                    task.setCategoryId(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_TASK_CATEGORY_ID)));
                    task.setFrequency(FrequencyType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_FREQUENCY))));
                    task.setStartMillis(cursor.getLong(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_START_MILLIS)));
                    task.setEndMillis(cursor.getLong(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_END_MILLIS)));
                    task.setGroupStartMillis(cursor.getLong(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_GROUP_START_MILLIS)));
                    task.setGroupEndMillis(cursor.getLong(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_GROUP_END_MILLIS)));
                    task.setInterval(cursor.getInt(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_INTERVAL)));
                    task.setStatusTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_STATUS_TIMESTAMP)));
                    task.setQuotaReached(cursor.getInt(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_QUOTA_REACHED)) == 1);

                    String unitStr = cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_UNIT));
                    task.setUnit(unitStr != null ? UnitType.valueOf(unitStr) : null);

                    task.setDifficulty(DifficultyType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_DIFFICULTY))));
                    task.setImportance(ImportanceType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_IMPORTANCE))));
                    task.setStatus(StatusType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_STATUS))));
                    tasks.add(task);
                } while (cursor.moveToNext());
            }
        } finally {
            db.close();
        }

        return tasks;
    }

    public int getDifficultyTaskCountSince(String userId, DifficultyType difficulty, long startOfPeriod, String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        int count = 0;

        try {
            String query = "SELECT COUNT(*) FROM tasks WHERE " +
                    SQLiteHelper.COLUMN_USER_ID + "=? AND " +
                    SQLiteHelper.COLUMN_TASK_OCCURRENCE_ID + "!=? AND " +
                    SQLiteHelper.COLUMN_DIFFICULTY + "=? AND " +
                    SQLiteHelper.COLUMN_STATUS + "=? AND " +
                    SQLiteHelper.COLUMN_STATUS_TIMESTAMP + ">=?";

            cursor = db.rawQuery(query, new String[]{
                    userId,
                    id,
                    difficulty.name(),
                    StatusType.URAĐEN.name(),
                    String.valueOf(startOfPeriod)
            });

            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }

            Log.d("TaskRepository2", "Cursor count: " + cursor.getCount());

        } catch (Exception e) {
            Log.e("TaskRepository", "Error counting difficulty tasks: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }

        return count;
    }

    public int getImportanceTaskCountSince(String userId, ImportanceType importance, long startOfPeriod, String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        int count = 0;

        try {
            String query = "SELECT COUNT(*) FROM tasks WHERE " +
                    SQLiteHelper.COLUMN_USER_ID + "=? AND " +
                    SQLiteHelper.COLUMN_TASK_OCCURRENCE_ID + "!=? AND " +
                    SQLiteHelper.COLUMN_IMPORTANCE + "=? AND " +
                    SQLiteHelper.COLUMN_STATUS + "=? AND " +
                    SQLiteHelper.COLUMN_STATUS_TIMESTAMP + ">=?";

            cursor = db.rawQuery(query, new String[]{
                    userId,
                    id,
                    importance.name(),
                    StatusType.URAĐEN.name(),
                    String.valueOf(startOfPeriod)
            });

            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e("TaskRepository", "Error counting importance tasks: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }

        return count;
    }

}
