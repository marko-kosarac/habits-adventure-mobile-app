package com.example.mobilnaaplikacija.repository;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.mobilnaaplikacija.database.SQLiteHelper;
import com.example.mobilnaaplikacija.model.Category;
import com.example.mobilnaaplikacija.model.DifficultyType;
import com.example.mobilnaaplikacija.model.FrequencyType;
import com.example.mobilnaaplikacija.model.ImportanceType;
import com.example.mobilnaaplikacija.model.StatusType;
import com.example.mobilnaaplikacija.model.Task;
import com.example.mobilnaaplikacija.model.UnitType;

import java.util.ArrayList;
import java.util.List;

public class CategoryRepository {
    private SQLiteHelper dbHelper;
    public CategoryRepository(SQLiteHelper dbHelper){ this.dbHelper = dbHelper;}

    public List<Category> getCategories(){
        List<Category> categories = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(SQLiteHelper.TABLE_CATEGORIES, new String[]{"id", "name", "color"},null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                int color = cursor.getInt(cursor.getColumnIndexOrThrow("color"));
                categories.add(new Category(id, color, name));
            }
            while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return categories;
    }

}
