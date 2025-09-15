package com.example.mobilnaaplikacija.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteHelper extends SQLiteOpenHelper {
    //Equipment table
    public static final String TABLE_EQUIPMENT = "EQUIPMENT";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_TYPE = "type";       // enum se čuva kao string
    public static final String COLUMN_BONUS = "bonus";
    public static final String COLUMN_DURATION = "duration";
    public static final String COLUMN_PRICE = "price";

    // Tasks table
    public static final String TABLE_TASKS = "TASKS";
    public static final String COLUMN_TASK_ID = "task_id";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_TASK_NAME = "task_name";
    public static final String COLUMN_TASK_DESCRIPTION = "task_description";
    public static final String COLUMN_CATEGORY_ID = "category_id";
    public static final String COLUMN_FREQUENCY = "frequency";
    public static final String COLUMN_START_DATE = "start_date";
    public static final String COLUMN_END_DATE = "end_date";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_IS_WHOLE_DAY = "is_whole_day";
    public static final String COLUMN_INTERVAL = "interval";
    public static final String COLUMN_UNIT = "unit";
    public static final String COLUMN_DIFFICULTY = "difficulty";
    public static final String COLUMN_IMPORTANCE = "importance";
    public static final String COLUMN_STATUS = "status";

    private static final String DATABASE_NAME = "appdata.db";
    private static final int DATABASE_VERSION = 5;

    private static final String DB_CREATE_EQUIPMENT = "CREATE TABLE " + TABLE_EQUIPMENT + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_NAME + " TEXT, "
            + COLUMN_DESCRIPTION + " TEXT, "
            + COLUMN_TYPE + " TEXT, "           // enum stored as TEXT
            + COLUMN_BONUS + " TEXT, "
            + COLUMN_DURATION + " INTEGER, "
            + COLUMN_PRICE + " INTEGER"
            + ");";

    private static final String DB_CREATE_TASKS =
            "CREATE TABLE " + TABLE_TASKS + " (" +
                    COLUMN_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_ID + " TEXT, " +
                    COLUMN_TASK_NAME + " TEXT, " +
                    COLUMN_TASK_DESCRIPTION + " TEXT, " +
                    COLUMN_CATEGORY_ID + " TEXT, " +
                    COLUMN_FREQUENCY + " TEXT, " +
                    COLUMN_START_DATE + " TEXT, " +
                    COLUMN_END_DATE + " TEXT, " +
                    COLUMN_TIME + " TEXT, " +
                    COLUMN_IS_WHOLE_DAY + " INTEGER, " +
                    COLUMN_INTERVAL + " INTEGER, " +
                    COLUMN_UNIT + " TEXT, " +
                    COLUMN_DIFFICULTY + " TEXT, " +
                    COLUMN_IMPORTANCE + " TEXT, " +
                    COLUMN_STATUS + " TEXT" +
                    ");";

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i("DB", "Creating Equipment and Tasks table");
        db.execSQL(DB_CREATE_EQUIPMENT);
        db.execSQL(DB_CREATE_TASKS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("DB", "Upgrading tables from " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EQUIPMENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        onCreate(db);
    }
}
