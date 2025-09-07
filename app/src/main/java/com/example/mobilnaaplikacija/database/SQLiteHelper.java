package com.example.mobilnaaplikacija.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteHelper extends SQLiteOpenHelper {

    public static final String TABLE_EQUIPMENT = "EQUIPMENT";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_TYPE = "type";       // enum se čuva kao string
    public static final String COLUMN_BONUS = "bonus";
    public static final String COLUMN_DURATION = "duration";
    public static final String COLUMN_PRICE = "price";

    private static final String DATABASE_NAME = "equipment.db";
    private static final int DATABASE_VERSION = 4;

    private static final String DB_CREATE = "CREATE TABLE " + TABLE_EQUIPMENT + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_NAME + " TEXT, "
            + COLUMN_DESCRIPTION + " TEXT, "
            + COLUMN_TYPE + " TEXT, "           // enum stored as TEXT
            + COLUMN_BONUS + " TEXT, "
            + COLUMN_DURATION + " INTEGER, "
            + COLUMN_PRICE + " INTEGER"
            + ");";

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i("EQ_DB", "Creating Equipment table");
        db.execSQL(DB_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("EQ_DB", "Upgrading Equipment table from " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EQUIPMENT);
        onCreate(db);
    }
}
