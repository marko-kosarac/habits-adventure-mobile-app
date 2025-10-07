package com.example.mobilnaaplikacija.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.mobilnaaplikacija.database.SQLiteHelper;
import com.example.mobilnaaplikacija.model.Boss;
import com.example.mobilnaaplikacija.model.Task;

public class BossRepository {
    private SQLiteHelper dbHelper;
    public BossRepository(SQLiteHelper dbHelper) { this.dbHelper = dbHelper;}

    public Boss getBossById(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Boss boss = null;

        Cursor cursor = db.query(
                SQLiteHelper.TABLE_BOSS, new String[]{"id", "current_hp", "max_hp", "level", "defeated"}, "id = ?", new String[]{id}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            boss = new Boss(
                    cursor.getString(cursor.getColumnIndexOrThrow("id")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("current_hp")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("max_hp")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("level")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("defeated")) == 1
            );
            cursor.close();
        }

        db.close();
        return boss;
    }
    
    public Boss add(Boss boss){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.COLUMN_BOSS_ID, boss.getId());
        values.put(SQLiteHelper.COLUMN_BOSS_CURRENT_HP, boss.getCurrentHp());
        values.put(SQLiteHelper.COLUMN_BOSS_MAX_HP, boss.getMaxHp());
        values.put(SQLiteHelper.COLUMN_BOSS_LEVEL, boss.getLevel());
        values.put(SQLiteHelper.COLUMN_BOSS_DEFEATED, boss.isDefeated());

        long rowId = db.insert(SQLiteHelper.TABLE_BOSS, null, values);
        db.close();

        if (rowId == -1)
            return null;

        boss.setId(String.valueOf(rowId));
        return boss;
    }

    public Boss update(Boss boss) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.COLUMN_BOSS_ID, boss.getId());
        values.put(SQLiteHelper.COLUMN_BOSS_CURRENT_HP, boss.getCurrentHp());
        values.put(SQLiteHelper.COLUMN_BOSS_MAX_HP, boss.getMaxHp());
        values.put(SQLiteHelper.COLUMN_BOSS_LEVEL, boss.getLevel());
        values.put(SQLiteHelper.COLUMN_BOSS_DEFEATED, boss.isDefeated());

        db.update(SQLiteHelper.TABLE_BOSS, values,
                SQLiteHelper.COLUMN_BOSS_ID + " = ?",
                new String[]{boss.getId()});
        db.close();

        return boss;
    }
}
