package com.example.mobilnaaplikacija.services;

import android.content.Context;

import com.example.mobilnaaplikacija.database.SQLiteHelper;
import com.example.mobilnaaplikacija.model.Category;
import com.example.mobilnaaplikacija.repository.CategoryRepository;

import java.util.List;

public class CategoryService {
    private CategoryRepository categoryRepository;

    public CategoryService (Context context) {
        this.categoryRepository = new CategoryRepository(new SQLiteHelper(context));
    }

    public List<Category> getCategories() {
        return categoryRepository.getCategories();
    }

    public Category add(Category category) {
        categoryRepository.add(category);
        return category;
    }


    public String validate(String name, int color) {
        if (name.isEmpty()) return  "Unesi naziv kategorije!";
        if (color == -1) return "Izaberi boju!";
        if (isColorTaken(color)) return "Boja je vec zauzeta!";
        return null;
    }

    public boolean isColorTaken(int pickedColor) {
        ArrayList<Category> categories = new ArrayList<>(getCategories());
        for (Category c: categories) {
            if (c.getColor()!= null && c.getColor() == pickedColor)
                return true;
        }
        return false;
    }
}
