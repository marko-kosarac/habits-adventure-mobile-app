package com.example.mobilnaaplikacija.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.mobilnaaplikacija.model.Equipment;

public class EquipmentSeeder {

    private SQLiteHelper dbHelper;

    public EquipmentSeeder(Context context, SQLiteDatabase db) {
        dbHelper = new SQLiteHelper(context);
    }

    public void seedData() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Napitci
        insertEquipment(db, new Equipment(0, "Napitak PP +20%", "Jednokratni napitak koji povećava PP za 20%", Equipment.Type.NAPITAK, "+20% PP", 0, 50));
        insertEquipment(db, new Equipment(0, "Napitak PP +40%", "Jednokratni napitak koji povećava PP za 40%", Equipment.Type.NAPITAK, "+40% PP", 0, 70));
        insertEquipment(db, new Equipment(0, "Napitak Snage +5%", "Trajno povećava snagu za 5%", Equipment.Type.NAPITAK, "+5% Snage", -1, 200));
        insertEquipment(db, new Equipment(0, "Napitak Snage +10%", "Trajno povećava snagu za 10%", Equipment.Type.NAPITAK, "+10% Snage", -1, 1000));
        insertEquipment(db, new Equipment(0, "Napitak PP +15%", "Jednokratni napitak koji povećava PP za 15%", Equipment.Type.NAPITAK, "+15% PP", 0, 40));
        insertEquipment(db, new Equipment(0, "Napitak Snage +8%", "Trajno povećava snagu za 8%", Equipment.Type.NAPITAK, "+8% Snage", -1, 300));

// Odeća
        insertEquipment(db, new Equipment(0, "Rukavice Snage +10%", "Povećavaju snagu za 10%", Equipment.Type.ODECA, "+10% Snage", -1, 60));
        insertEquipment(db, new Equipment(0, "Štit Uspeh +10%", "Povećava šansu uspešnog napada za 10%", Equipment.Type.ODECA, "+10% Uspeh", -1, 60));
        insertEquipment(db, new Equipment(0, "Čizme Napadi +40%", "Povećavaju broj napada za 40%", Equipment.Type.ODECA, "+40% Napadi", -1, 80));


        db.close();
    }

    private void insertEquipment(SQLiteDatabase db, Equipment equipment) {
        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.COLUMN_NAME, equipment.getName());
        values.put(SQLiteHelper.COLUMN_DESCRIPTION, equipment.getDescription());
        values.put(SQLiteHelper.COLUMN_TYPE, equipment.getType().name()); // ENUM kao string
        values.put(SQLiteHelper.COLUMN_BONUS, equipment.getBonus());
        values.put(SQLiteHelper.COLUMN_DURATION, equipment.getDuration());
        values.put(SQLiteHelper.COLUMN_PRICE, equipment.getPrice());

        long id = db.insert(SQLiteHelper.TABLE_EQUIPMENT, null, values);
        Log.i("SEED_SQLITE", "Inserted equipment with ID: " + id);
    }
}
