package com.example.mobilnaaplikacija.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.mobilnaaplikacija.database.SQLiteHelper;
import com.example.mobilnaaplikacija.model.Battle;
import com.example.mobilnaaplikacija.model.Battle;
import com.example.mobilnaaplikacija.model.Boss;

import java.util.ArrayList;
import java.util.List;

public class BattleRepository {
    private final SQLiteHelper dbHelper;

    public BattleRepository(SQLiteHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public Battle add(Battle battle) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.COLUMN_BATTLE_ID, battle.getId());
        values.put(SQLiteHelper.COLUMN_BATTLE_USER_ID, battle.getUserId());
        values.put(SQLiteHelper.COLUMN_BATTLE_BOSS_ID, battle.getBossId());
        values.put(SQLiteHelper.COLUMN_BATTLE_COINS_EARNED, battle.hasUserWon());
        values.put(SQLiteHelper.COLUMN_BATTLE_COINS_EARNED, battle.getCoinsEarned());

        long rowId = db.insert(SQLiteHelper.TABLE_BATTLES, null, values);
        db.close();

        if (rowId == -1) return null;
        battle.setId(String.valueOf(rowId));
        return battle;
    }

    public Battle getBattleByUserAndBoss(String userId, String bossId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Battle battle = null;

        Cursor cursor = db.query(
                SQLiteHelper.TABLE_BATTLES,
                new String[]{"id", "user_id", "boss_id", "user_won", "coins_earned"}, "user_id = ? AND boss_id = ?",
                new String[]{userId, bossId},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            battle = new Battle(
                    cursor.getString(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("user_id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("boss_id")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("user_won")) == 1,
                    cursor.getInt(cursor.getColumnIndexOrThrow("coins_earned"))
            );
            cursor.close();
        }

        db.close();
        return battle;
    }
}
