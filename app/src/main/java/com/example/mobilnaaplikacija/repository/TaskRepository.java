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

    public long insertTask(Task task, String userId){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.COLUMN_USER_ID, userId);
        values.put(SQLiteHelper.COLUMN_TASK_NAME, task.getName());
        values.put(SQLiteHelper.COLUMN_TASK_DESCRIPTION, task.getDescription());
        values.put(SQLiteHelper.COLUMN_CATEGORY_ID, task.getCategoryId());
        values.put(SQLiteHelper.COLUMN_FREQUENCY, task.getFrequency().name());
        values.put(SQLiteHelper.COLUMN_START_DATE, task.getStartDate());
        values.put(SQLiteHelper.COLUMN_END_DATE, task.getEndDate());
        values.put(SQLiteHelper.COLUMN_TIME, task.getTime());
        values.put(SQLiteHelper.COLUMN_IS_WHOLE_DAY, task.getWholeDay() ? 1 : 0);
        values.put(SQLiteHelper.COLUMN_INTERVAL, task.getInterval());
        values.put(SQLiteHelper.COLUMN_UNIT, task.getUnit() != null ? task.getUnit().name() : null);
        values.put(SQLiteHelper.COLUMN_DIFFICULTY, task.getDifficulty().name());
        values.put(SQLiteHelper.COLUMN_IMPORTANCE, task.getImportance().name());
        values.put(SQLiteHelper.COLUMN_STATUS, task.getStatus().name());

        long id = db.insert(SQLiteHelper.TABLE_TASKS, null, values);
        db.close();
        return id;
    }

    public List<Task> getTasksById(Long userId){
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(SQLiteHelper.TABLE_TASKS, null,
                SQLiteHelper.COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)},
                null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Task task = new Task();
                task.setId(cursor.getLong(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_TASK_ID)));
                task.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_USER_ID)));
                task.setName(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_TASK_NAME)));
                task.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_TASK_DESCRIPTION)));
                task.setCategoryId(cursor.getLong(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_CATEGORY_ID)));
                task.setFrequency(FrequencyType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_FREQUENCY))));
                task.setStartDate(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_START_DATE)));
                task.setEndDate(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_END_DATE)));
                task.setTime(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_TIME)));
                task.setWholeDay(cursor.getInt(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_IS_WHOLE_DAY)) == 1);
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

    public int deleteTask(long taskId){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(SQLiteHelper.TABLE_TASKS, SQLiteHelper.COLUMN_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
        db.close();
        return rows;
    }
}
