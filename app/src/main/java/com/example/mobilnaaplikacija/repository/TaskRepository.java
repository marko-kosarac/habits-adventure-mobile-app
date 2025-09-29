package com.example.mobilnaaplikacija.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.mobilnaaplikacija.database.SQLiteHelper;
import com.example.mobilnaaplikacija.model.DifficultyType;
import com.example.mobilnaaplikacija.model.FrequencyType;
import com.example.mobilnaaplikacija.model.ImportanceType;
import com.example.mobilnaaplikacija.model.StatusType;
import com.example.mobilnaaplikacija.model.Task;
import com.example.mobilnaaplikacija.model.UnitType;

import java.util.ArrayList;
import java.util.List;

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
        values.put(SQLiteHelper.COLUMN_INTERVAL, task.getInterval());
        values.put(SQLiteHelper.COLUMN_UNIT, task.getUnit() != null ? task.getUnit().name() : null);
        values.put(SQLiteHelper.COLUMN_DIFFICULTY, task.getDifficulty().name());
        values.put(SQLiteHelper.COLUMN_IMPORTANCE, task.getImportance().name());
        values.put(SQLiteHelper.COLUMN_STATUS, task.getStatus().name());

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
        values.put(SQLiteHelper.COLUMN_INTERVAL, task.getInterval());
        values.put(SQLiteHelper.COLUMN_UNIT, task.getUnit() != null ? task.getUnit().name() : null);
        values.put(SQLiteHelper.COLUMN_DIFFICULTY, task.getDifficulty().name());
        values.put(SQLiteHelper.COLUMN_IMPORTANCE, task.getImportance().name());
        values.put(SQLiteHelper.COLUMN_STATUS, task.getStatus().name());

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
                task.setInterval(cursor.getInt(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_INTERVAL)));
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

    public boolean deleteFutureOccurrences(String taskId) {
        long now = System.currentTimeMillis();
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //start > now
        int deleted = db.delete(
                SQLiteHelper.TABLE_TASKS,
                "task_id = ? AND start_millis > ?",
                new String[]{taskId, String.valueOf(now)}
        );

        db.close();
        return deleted > 0;
    }

}
