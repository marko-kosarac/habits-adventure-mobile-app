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

        db.delete(SQLiteHelper.TABLE_EQUIPMENT, null, null);

        // Napitci
        insertEquipment(db, new Equipment(0, "Snaga u flašici", "Jednokratni napitak koji povećava snagu", Equipment.Type.NAPITAK, "+20%", 0, 50));
        insertEquipment(db, new Equipment(0, "Turbo eliksir", "Jednokratni napitak koji povećava snagu", Equipment.Type.NAPITAK, "+40%", 0, 70));
        insertEquipment(db, new Equipment(0, "Merlinov napitak", "Napitak za trajno povećavanje snage", Equipment.Type.NAPITAK, "+5%", -1, 200));
        insertEquipment(db, new Equipment(0, "Gargamelov zub", "Napitak za trajno povećavanje snage", Equipment.Type.NAPITAK, "+10%", -1, 1000));
        insertEquipment(db, new Equipment(0, "Moćni miks", "Jednokratni napitak koji povećava snagu", Equipment.Type.NAPITAK, "+15%", 0, 40));
        insertEquipment(db, new Equipment(0, "Čarobni nektar", "Napitak za trajno povećavanje snage", Equipment.Type.NAPITAK, "+8%", -1, 300));

// Odeća
        insertEquipment(db, new Equipment(0, "Rukavice", "Povećavaju snagu", Equipment.Type.ODECA, "+10%", -1, 60));
        insertEquipment(db, new Equipment(0, "Štit", "Povećava šansu uspešnog napada", Equipment.Type.ODECA, "+10%", -1, 60));
        insertEquipment(db, new Equipment(0, "Čizme", "Povećavaju broj napada", Equipment.Type.ODECA, "+40%", -1, 80));


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
