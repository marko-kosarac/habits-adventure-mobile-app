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

}
