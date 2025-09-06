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

        // Prva oprema
        insertEquipment(db, new Equipment(
                0,
                "Napitak Snage 20%",
                "Jednokratni napitak koji povećava PP za 20%",
                Equipment.Type.NAPITAK,
                "+20% PP",
                0
        ));

        // Druga oprema
        insertEquipment(db, new Equipment(
                0,
                "Rukavice Snage 10%",
                "Odeća koja povećava snagu za 10%",
                Equipment.Type.ODECA,
                "+10% Snage",
                2
        ));

        // Treća oprema
        insertEquipment(db, new Equipment(
                0,
                "Napitak snage 5%",
                "Oružje koje trajno povećava snagu za 5%",
                Equipment.Type.ORUZJE,
                "+5% Snage",
                0
        ));

        db.close();
    }

    private void insertEquipment(SQLiteDatabase db, Equipment equipment) {
        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.COLUMN_NAME, equipment.getName());
        values.put(SQLiteHelper.COLUMN_DESCRIPTION, equipment.getDescription());
        values.put(SQLiteHelper.COLUMN_TYPE, equipment.getType().name()); // ENUM kao string
        values.put(SQLiteHelper.COLUMN_BONUS, equipment.getBonus());
        values.put(SQLiteHelper.COLUMN_DURATION, equipment.getDuration());

        long id = db.insert(SQLiteHelper.TABLE_EQUIPMENT, null, values);
        Log.i("SEED_SQLITE", "Inserted equipment with ID: " + id);
    }
}
