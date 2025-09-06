package com.example.mobilnaaplikacija.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.mobilnaaplikacija.database.SQLiteHelper;
import com.example.mobilnaaplikacija.model.Equipment;

import java.util.ArrayList;

public class EquipmentRepository {

    private SQLiteHelper dbHelper;

    public EquipmentRepository(Context context) {
        dbHelper = new SQLiteHelper(context);
    }

    // Ubacivanje nove opreme u bazu
    public long insertEquipment(Equipment equipment) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.COLUMN_NAME, equipment.getName());
        values.put(SQLiteHelper.COLUMN_DESCRIPTION, equipment.getDescription());
        values.put(SQLiteHelper.COLUMN_TYPE, equipment.getType().name());
        values.put(SQLiteHelper.COLUMN_BONUS, equipment.getBonus());
        values.put(SQLiteHelper.COLUMN_DURATION, equipment.getDuration());
        values.put(SQLiteHelper.COLUMN_PRICE, equipment.getDuration()); // ili druga logika za cenu

        long id = db.insert(SQLiteHelper.TABLE_EQUIPMENT, null, values);
        db.close();
        return id;
    }

    // Update postojeće opreme
    public int updateEquipment(Equipment equipment) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.COLUMN_NAME, equipment.getName());
        values.put(SQLiteHelper.COLUMN_DESCRIPTION, equipment.getDescription());
        values.put(SQLiteHelper.COLUMN_TYPE, equipment.getType().name());
        values.put(SQLiteHelper.COLUMN_BONUS, equipment.getBonus());
        values.put(SQLiteHelper.COLUMN_DURATION, equipment.getDuration());
        values.put(SQLiteHelper.COLUMN_PRICE, equipment.getDuration()); // ili druga logika za cenu

        int rows = db.update(SQLiteHelper.TABLE_EQUIPMENT, values,
                SQLiteHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(equipment.getId())});
        db.close();
        return rows;
    }

    // Brisanje opreme
    public int deleteEquipment(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(SQLiteHelper.TABLE_EQUIPMENT,
                SQLiteHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    // Dohvatanje svih oprema iz baze
    public ArrayList<Equipment> getAllEquipment() {
        ArrayList<Equipment> equipmentList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(SQLiteHelper.TABLE_EQUIPMENT, null,
                null, null, null, null, SQLiteHelper.COLUMN_ID + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_NAME));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_DESCRIPTION));
                String typeStr = cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_TYPE));
                Equipment.Type type = Equipment.Type.valueOf(typeStr);
                String bonus = cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_BONUS));
                int duration = cursor.getInt(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_DURATION));

                Equipment equipment = new Equipment(id, name, description, type, bonus, duration);
                equipmentList.add(equipment);
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return equipmentList;
    }

    // Dohvatanje opreme po ID-u
    public Equipment getEquipmentById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(SQLiteHelper.TABLE_EQUIPMENT, null,
                SQLiteHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(id)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_NAME));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_DESCRIPTION));
            String typeStr = cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_TYPE));
            Equipment.Type type = Equipment.Type.valueOf(typeStr);
            String bonus = cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_BONUS));
            int duration = cursor.getInt(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_DURATION));

            cursor.close();
            db.close();
            return new Equipment(id, name, description, type, bonus, duration);
        }

        if (cursor != null) cursor.close();
        db.close();
        return null;
    }
}
