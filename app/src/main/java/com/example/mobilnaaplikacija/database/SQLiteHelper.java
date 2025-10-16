package com.example.mobilnaaplikacija.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.compose.ui.node.HitTestResult;

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
    public static final String COLUMN_COUNT = "count";

    // Tasks table
    public static final String TABLE_TASKS = "TASKS";
    public static final String COLUMN_TASK_OCCURRENCE_ID = "id";
    public static final String COLUMN_TASK_ID = "task_id";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_TASK_NAME = "task_name";
    public static final String COLUMN_TASK_DESCRIPTION = "task_description";
    public static final String COLUMN_TASK_CATEGORY_ID = "category_id";
    public static final String COLUMN_FREQUENCY = "frequency";
    public static final String COLUMN_START_MILLIS = "start_millis";
    public static final String COLUMN_END_MILLIS = "end_millis";
    public static final String COLUMN_GROUP_START_MILLIS = "group_start_millis";
    public static final String COLUMN_GROUP_END_MILLIS = "group_end_millis";
    public static final String COLUMN_INTERVAL = "interval";
    public static final String COLUMN_UNIT = "unit";
    public static final String COLUMN_DIFFICULTY = "difficulty";
    public static final String COLUMN_IMPORTANCE = "importance";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_STATUS_TIMESTAMP = "status_timestamp";
    public static final String COLUMN_QUOTA_REACHED = "quota_reached";

    // Categories table
    public static final String TABLE_CATEGORIES = "CATEGORIES";
    public static final String COLUMN_CATEGORY_ID = "id";
    public static final String COLUMN_CATEGORY_NAME = "name";
    public static final String COLUMN_CATEGORY_COLOR = "color";

    // Boss table
    public static final String TABLE_BOSS = "BOSS";
    public static final String COLUMN_BOSS_ID = "id";
    public static final String COLUMN_BOSS_CURRENT_HP = "current_hp";
    public static final String COLUMN_BOSS_MAX_HP = "max_hp";
    public static final String COLUMN_BOSS_LEVEL = "level";
    public static final String COLUMN_BOSS_DEFEATED = "defeated";

    // Battle table
    public static final String TABLE_BATTLES = "BATTLES";
    public static final String COLUMN_BATTLE_ID = "id";
    public static final String COLUMN_BATTLE_USER_ID = "user_id";
    public static final String COLUMN_BATTLE_BOSS_ID = "boss_id";
    public static final String COLUMN_BATTLE_EQUIPMENT_IDS = "equipment";

    public static final String COLUMN_BATTLE_USER_WON = "user_won";
    public static final String COLUMN_BATTLE_COINS_EARNED = "coins_earned";

    // Attack table
    public static final String TABLE_ATTACKS = "ATTACKS";
    public static final String COLUMN_ATTACK_ID = "id";
    public static final String COLUMN_ATTACK_USER_ID = "user_id";
    public static final String COLUMN_ATTACK_BOSS_ID = "boss_id";
    public static final String COLUMN_ATTACK_ATTEMPTS_NUMBER = "attempts";
    public static final String COLUMN_ATTACK_HIT = "hit";
    public static final String COLUMN_ATTACK_DAMAGE_DEALT = "damage_dealt";

    private static final int DATABASE_VERSION = 12;
    private static final String DATABASE_NAME = "appdata.db";

    private static final String DB_CREATE_EQUIPMENT = "CREATE TABLE " + TABLE_EQUIPMENT + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_NAME + " TEXT, "
            + COLUMN_DESCRIPTION + " TEXT, "
            + COLUMN_TYPE + " TEXT, "           // enum stored as TEXT
            + COLUMN_BONUS + " TEXT, "
            + COLUMN_DURATION + " INTEGER, "
            + COLUMN_PRICE + " INTEGER, "
            + COLUMN_COUNT + " INTEGER"
            + ");";

    private static final String DB_CREATE_TASKS =
            "CREATE TABLE " + TABLE_TASKS + " (" +
                    COLUMN_TASK_OCCURRENCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TASK_ID + " TEXT, " +
                    COLUMN_USER_ID + " TEXT, " +
                    COLUMN_TASK_NAME + " TEXT, " +
                    COLUMN_TASK_DESCRIPTION + " TEXT, " +
                    COLUMN_TASK_CATEGORY_ID + " TEXT, " +
                    COLUMN_FREQUENCY + " TEXT, " +
                    COLUMN_START_MILLIS + " INTEGER, " +
                    COLUMN_END_MILLIS + " INTEGER, " +
                    COLUMN_GROUP_START_MILLIS + " INTEGER, " +
                    COLUMN_GROUP_END_MILLIS + " INTEGER, " +
                    COLUMN_INTERVAL + " INTEGER, " +
                    COLUMN_UNIT + " TEXT, " +
                    COLUMN_DIFFICULTY + " TEXT, " +
                    COLUMN_IMPORTANCE + " TEXT, " +
                    COLUMN_STATUS + " TEXT, " +
                    COLUMN_STATUS_TIMESTAMP + " INTEGER, " +
                    COLUMN_QUOTA_REACHED + " TEXT" +
        ");";

    private static final String DB_CREATE_CATEGORIES = "CREATE TABLE " + TABLE_CATEGORIES + " ("
            + COLUMN_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_CATEGORY_NAME + " TEXT, "
            + COLUMN_CATEGORY_COLOR + " INTEGER"
            + ");";

    private static final String DB_CREATE_BOSS = "CREATE TABLE " + TABLE_BOSS + " ("
            + COLUMN_BOSS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_BOSS_CURRENT_HP + " INTEGER, "
            + COLUMN_BOSS_MAX_HP + " INTEGER, "
            + COLUMN_BOSS_LEVEL + " INTEGER, "
            + COLUMN_BOSS_DEFEATED + " INTEGER"
            + ");";

    private static final String DB_CREATE_BATTLES = "CREATE TABLE " + TABLE_BATTLES + " ("
            + COLUMN_BATTLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_BATTLE_USER_ID + " TEXT, "
            + COLUMN_BATTLE_BOSS_ID + " TEXT, "
            + COLUMN_BATTLE_EQUIPMENT_IDS + " TEXT, "
            + COLUMN_BATTLE_USER_WON + " INTEGER,"
            + COLUMN_BATTLE_COINS_EARNED + " INTEGER"
            + ");";

    private static final String DB_CREATE_ATTACKS = "CREATE TABLE " + TABLE_ATTACKS + " ("
            + COLUMN_ATTACK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_ATTACK_USER_ID + " TEXT, "
            + COLUMN_ATTACK_BOSS_ID + " TEXT, "
            + COLUMN_ATTACK_ATTEMPTS_NUMBER + " INTEGER, "
            + COLUMN_ATTACK_HIT + " INTEGER, "
            + COLUMN_ATTACK_DAMAGE_DEALT + " INTEGER"
            + ");";

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i("DB", "Creating Equipment, Tasks, Categories, Boss, Battles and Attacks table");
        db.execSQL(DB_CREATE_EQUIPMENT);
        db.execSQL(DB_CREATE_TASKS);
        db.execSQL(DB_CREATE_CATEGORIES);
        db.execSQL(DB_CREATE_BOSS);
        db.execSQL(DB_CREATE_BATTLES);
        db.execSQL(DB_CREATE_ATTACKS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("DB", "Upgrading tables from " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EQUIPMENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOSS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BATTLES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ATTACKS);
        onCreate(db);
    }
}
