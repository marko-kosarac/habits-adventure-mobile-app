package com.example.mobilnaaplikacija.repository;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.example.mobilnaaplikacija.database.SQLiteHelper;
import com.example.mobilnaaplikacija.model.Boss;

public class BossRepository {
    private SQLiteHelper dbHelper;
    public BossRepository(SQLiteHelper dbHelper) { this.dbHelper = dbHelper;}

    public Boss add(Boss boss){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.COLUMN_BOSS_ID, boss.getId());
        values.put(SQLiteHelper.COLUMN_BOSS_CURRENT_HP, boss.getCurrentHp());
        values.put(SQLiteHelper.COLUMN_BOSS_MAX_HP, boss.getMaxHp());
        values.put(SQLiteHelper.COLUMN_BOSS_LEVEL, boss.getLevel());
        values.put(SQLiteHelper.COLUMN_BOSS_DEFEATED, boss.getDefeated());

        long rowId = db.insert(SQLiteHelper.TABLE_BOSS, null, values);
        db.close();

        if (rowId == -1)
            return null;

        boss.setId(String.valueOf(rowId));
        return boss;
    }
}
