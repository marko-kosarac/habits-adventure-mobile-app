package com.example.mobilnaaplikacija.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.mobilnaaplikacija.database.SQLiteHelper;
import com.example.mobilnaaplikacija.model.Attack;

import java.util.ArrayList;
import java.util.List;

public class AttackRepository {
    private final SQLiteHelper dbHelper;

    public AttackRepository(SQLiteHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public List<Attack> getAttacksByUserAndBoss(String userId, String bossId){
        List<Attack> attacks = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(SQLiteHelper.TABLE_ATTACKS, null,
                SQLiteHelper.COLUMN_ATTACK_USER_ID + " = ? AND " +
                        SQLiteHelper.COLUMN_ATTACK_BOSS_ID + " = ?", new String[]{String.valueOf(userId), String.valueOf(bossId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Attack attack = new Attack();
                attack.setId(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_ATTACK_ID)));
                attack.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_ATTACK_USER_ID)));
                attack.setBossId(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_ATTACK_BOSS_ID)));
                attack.setHit(cursor.getInt(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_ATTACK_HIT)) == 1);
                attack.setDamageDealt(cursor.getInt(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_ATTACK_DAMAGE_DEALT)));
                attacks.add(attack);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return attacks;
    }
    public List<Attack> getAttacksByUser(String userId){
        List<Attack> attacks = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(SQLiteHelper.TABLE_ATTACKS, null,
                SQLiteHelper.COLUMN_ATTACK_USER_ID + " = ?", new String[]{String.valueOf(userId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Attack attack = new Attack();
                attack.setId(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_ATTACK_ID)));
                attack.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_USER_ID)));
                attack.setBossId(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_ATTACK_BOSS_ID)));
                attack.setHit(cursor.getInt(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_ATTACK_HIT)) == 1);
                attack.setDamageDealt(cursor.getInt(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_ATTACK_DAMAGE_DEALT)));
                attacks.add(attack);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return attacks;
    }
    
    public Attack add(Attack attack) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.COLUMN_ATTACK_USER_ID, attack.getUserId());
        values.put(SQLiteHelper.COLUMN_ATTACK_BOSS_ID, attack.getBossId());
        values.put(SQLiteHelper.COLUMN_ATTACK_ATTEMPTS_NUMBER, attack.getAttemptNumber());
        values.put(SQLiteHelper.COLUMN_ATTACK_HIT, attack.isHit() ? 1 : 0);
        values.put(SQLiteHelper.COLUMN_ATTACK_DAMAGE_DEALT, attack.getDamageDealt());
        long rowId = db.insert(SQLiteHelper.TABLE_ATTACKS, null, values);
        db.close();

        if (rowId == -1) return null;
        attack.setId(String.valueOf(rowId));
        return attack;
    }

    public int delete(String id){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(SQLiteHelper.TABLE_ATTACKS, SQLiteHelper.COLUMN_ATTACK_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }
}
