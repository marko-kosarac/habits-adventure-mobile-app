package com.example.mobilnaaplikacija.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.mobilnaaplikacija.database.SQLiteHelper;
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
        values.put(SQLiteHelper.COLUMN_BATTLE_EQUIPMENT_IDS, battle.getEquipmentIdsAsString());
        values.put(SQLiteHelper.COLUMN_BATTLE_USER_WON, battle.hasUserWon());
        values.put(SQLiteHelper.COLUMN_BATTLE_COINS_EARNED, battle.getCoinsEarned());

        long rowId = db.insert(SQLiteHelper.TABLE_BATTLES, null, values);
        db.close();

        if (rowId == -1) return null;
        battle.setId(String.valueOf(rowId));
        return battle;
    }


    public Battle update(Battle battle) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.COLUMN_BATTLE_USER_ID, battle.getUserId());
        values.put(SQLiteHelper.COLUMN_BATTLE_BOSS_ID, battle.getBossId());
        values.put(SQLiteHelper.COLUMN_BATTLE_EQUIPMENT_IDS, battle.getEquipmentIdsAsString());
        values.put(SQLiteHelper.COLUMN_BATTLE_USER_WON, battle.hasUserWon());
        values.put(SQLiteHelper.COLUMN_BATTLE_COINS_EARNED, battle.getCoinsEarned());

        db.update(SQLiteHelper.TABLE_BATTLES, values,
                SQLiteHelper.COLUMN_BATTLE_BOSS_ID + " = ? AND " + SQLiteHelper.COLUMN_BATTLE_USER_ID + " = ?",
                new String[]{battle.getBossId(), battle.getUserId()});
        db.close();

        return battle;
    }

    public List<Battle> getBattlesByUser(String userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Battle> battles = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor = db.query(SQLiteHelper.TABLE_BATTLES,null, SQLiteHelper.COLUMN_BATTLE_USER_ID + " = ?", new String[]{ userId }, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Battle battle = new Battle();
                    battle.setId(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_BATTLE_ID)));
                    battle.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_BATTLE_USER_ID)));
                    battle.setBossId(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_BATTLE_BOSS_ID)));
                    battle.setEquipmentIdsFromString(cursor.getString(cursor.getColumnIndex(SQLiteHelper.COLUMN_BATTLE_EQUIPMENT_IDS)));
                    battle.setCoinsEarned(cursor.getInt(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_BATTLE_COINS_EARNED)));

                    if (!cursor.isNull(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_BATTLE_USER_WON))) {
                        battle.setUserWon(cursor.getInt(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_BATTLE_USER_WON)) == 1);
                    } else {
                        battle.setUserWon(null);
                    }

                    battles.add(battle);
                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            Log.e("BattleRepository", "Error fetching battles", e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return battles;
    }

}
