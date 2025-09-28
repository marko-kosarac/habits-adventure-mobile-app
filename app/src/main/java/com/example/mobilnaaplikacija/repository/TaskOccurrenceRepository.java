package com.example.mobilnaaplikacija.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.mobilnaaplikacija.database.SQLiteHelper;
import com.example.mobilnaaplikacija.model.StatusType;
import com.example.mobilnaaplikacija.model.TaskOccurrence;

import java.util.ArrayList;
import java.util.List;

public class TaskOccurrenceRepository {
    private final SQLiteHelper dbHelper;

    public TaskOccurrenceRepository(SQLiteHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public TaskOccurrence add(TaskOccurrence taskOccurrence){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.COLUMN_TASK_OCCURRENCE_TASK_ID, taskOccurrence.getTaskId());
        values.put(SQLiteHelper.COLUMN_TASK_OCCURRENCE_NAME, taskOccurrence.getName());
        values.put(SQLiteHelper.COLUMN_TASK_OCCURRENCE_DESCRIPTION, taskOccurrence.getDescription());
        values.put(SQLiteHelper.COLUMN_TASK_OCCURRENCE_START_MILLIS, taskOccurrence.getStartMillis());
        values.put(SQLiteHelper.COLUMN_TASK_OCCURRENCE_END_MILLIS, taskOccurrence.getEndMillis());
        values.put(SQLiteHelper.COLUMN_TASK_OCCURRENCE_STATUS, taskOccurrence.getStatus().name());

        long rowId = db.insert(SQLiteHelper.TABLE_TASK_OCCURRENCES, null, values);
        db.close();

        if (rowId == -1)
            return null;

        taskOccurrence.setId(String.valueOf(rowId));
        return taskOccurrence;
    }

    public TaskOccurrence update(TaskOccurrence taskOccurrence) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.COLUMN_TASK_OCCURRENCE_TASK_ID, taskOccurrence.getTaskId());
        values.put(SQLiteHelper.COLUMN_TASK_OCCURRENCE_NAME, taskOccurrence.getName());
        values.put(SQLiteHelper.COLUMN_TASK_OCCURRENCE_DESCRIPTION, taskOccurrence.getDescription());
        values.put(SQLiteHelper.COLUMN_TASK_OCCURRENCE_START_MILLIS, taskOccurrence.getStartMillis());
        values.put(SQLiteHelper.COLUMN_TASK_OCCURRENCE_END_MILLIS, taskOccurrence.getEndMillis());
        values.put(SQLiteHelper.COLUMN_TASK_OCCURRENCE_STATUS, taskOccurrence.getStatus().name());

        db.update(SQLiteHelper.TABLE_TASK_OCCURRENCES, values,
                SQLiteHelper.COLUMN_TASK_OCCURRENCE_ID + " = ?",
                new String[]{taskOccurrence.getId()});
        db.close();

        return taskOccurrence;
    }

    public List<TaskOccurrence> getTaskOccurrencesByTask(String taskId){
        List<TaskOccurrence> taskOccurrences = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(SQLiteHelper.TABLE_TASK_OCCURRENCES, null,
                SQLiteHelper.COLUMN_TASK_OCCURRENCE_TASK_ID + " = ?", new String[]{String.valueOf(taskId)},
                null, null, null);

        if (cursor.moveToFirst()) {
            do {
                TaskOccurrence taskOccurrence = new TaskOccurrence();
                taskOccurrence.setId(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_TASK_OCCURRENCE_ID)));
                taskOccurrence.setTaskId(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_TASK_OCCURRENCE_TASK_ID)));
                taskOccurrence.setName(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_TASK_OCCURRENCE_NAME)));
                taskOccurrence.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_TASK_OCCURRENCE_DESCRIPTION)));
                taskOccurrence.setStartMillis(cursor.getLong(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_TASK_OCCURRENCE_START_MILLIS)));
                taskOccurrence.setEndMillis(cursor.getLong(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_TASK_OCCURRENCE_END_MILLIS)));
                taskOccurrence.setStatus(StatusType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_TASK_OCCURRENCE_STATUS))));
                taskOccurrences.add(taskOccurrence);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return taskOccurrences;
    }

    public int delete(String id){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(SQLiteHelper.TABLE_TASK_OCCURRENCES, SQLiteHelper.COLUMN_TASK_OCCURRENCE_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }
}
