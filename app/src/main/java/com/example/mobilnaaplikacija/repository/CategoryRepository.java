package com.example.mobilnaaplikacija.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.mobilnaaplikacija.database.SQLiteHelper;
import com.example.mobilnaaplikacija.model.Category;
import com.example.mobilnaaplikacija.model.Task;

import org.checkerframework.checker.units.qual.C;

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

    public Category add(Category category){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.COLUMN_CATEGORY_ID, category.getId());
        values.put(SQLiteHelper.COLUMN_CATEGORY_NAME, category.getName());
        values.put(SQLiteHelper.COLUMN_CATEGORY_COLOR, category.getColor());

        long rowId = db.insert(SQLiteHelper.TABLE_CATEGORIES, null, values);
        db.close();

        if (rowId == -1)
            return null;

        category.setId(String.valueOf(rowId));
        return category;
    }

    public Category update(Category category) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.COLUMN_CATEGORY_NAME, category.getName());
        values.put(SQLiteHelper.COLUMN_CATEGORY_COLOR, category.getColor());

        db.update(SQLiteHelper.TABLE_CATEGORIES, values,
                SQLiteHelper.COLUMN_CATEGORY_ID + " = ?",
                new String[]{category.getId()});
        db.close();

        return category;
    }

    public int delete(String id){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(SQLiteHelper.TABLE_CATEGORIES, SQLiteHelper.COLUMN_CATEGORY_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    public Category getCategoryById(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Category category = null;

        Cursor cursor = db.query(
                SQLiteHelper.TABLE_CATEGORIES, new String[]{"id", "name", "color"}, "id = ?", new String[]{id}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            category = new Category(
                    cursor.getString(cursor.getColumnIndexOrThrow("id")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("color")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name"))
            );
            cursor.close();
        }

        db.close();
        return category;
    }

    public Integer getColorById(String categoryId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Integer color = null;
        Cursor cursor = db.query(SQLiteHelper.TABLE_CATEGORIES,
                new String[]{SQLiteHelper.COLUMN_CATEGORY_COLOR},
                SQLiteHelper.COLUMN_CATEGORY_ID + " = ?",
                new String[]{categoryId}, null, null, null);
        if (cursor.moveToFirst()) {
            color = cursor.getInt(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_CATEGORY_COLOR));
        }
        cursor.close();
        db.close();
        return color;
    }

    public String getCategoryNameById(String categoryId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String name = null;

        Cursor cursor = db.query(SQLiteHelper.TABLE_CATEGORIES,
                new String[]{SQLiteHelper.COLUMN_CATEGORY_NAME},
                SQLiteHelper.COLUMN_CATEGORY_ID + " = ?",
                new String[]{categoryId},
                null, null, null);

        if (cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_CATEGORY_NAME));
        }

        cursor.close();
        db.close();
        return name;
    }


}
