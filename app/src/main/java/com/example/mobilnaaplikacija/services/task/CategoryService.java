package com.example.mobilnaaplikacija.services.task;

import android.content.Context;

import com.example.mobilnaaplikacija.database.SQLiteHelper;
import com.example.mobilnaaplikacija.model.Category;
import com.example.mobilnaaplikacija.model.enums.StatusType;
import com.example.mobilnaaplikacija.model.Task;
import com.example.mobilnaaplikacija.repository.CategoryRepository;

import java.util.ArrayList;
import java.util.List;

public class CategoryService {
    private CategoryRepository categoryRepository;
    private TaskService taskService;

    public CategoryService (Context context) {
        this.categoryRepository = new CategoryRepository(new SQLiteHelper(context));
        this.taskService = new TaskService(context);
    }

    public List<Category> getCategoriesByUser(String userId) {
        return categoryRepository.getCategories(userId);
    }

    public Category add(Category category) {
        categoryRepository.add(category);
        return category;
    }

    public Category update(Category category) {
        categoryRepository.update(category);
        return category;
    }
    public String validate(String name, int color, String userId) {
        if (name.isEmpty()) return  "Unesi naziv kategorije!";
        if (color == -1) return "Izaberi boju!";
        if (isColorTaken(color, userId)) return "Boja je vec zauzeta!";
        return null;
    }

    public boolean isColorTaken(int pickedColor, String userId) {
        ArrayList<Category> categories = new ArrayList<>(getCategoriesByUser(userId));
        for (Category c: categories) {
            if (c.getColor()!= null && c.getColor() == pickedColor)
                return true;
        }
        return false;
    }

    public String canBeRemoved(Category category, String userId) {
        ArrayList<Task> tasks = new ArrayList<>(taskService.getTasksByUser(userId));
        for (Task task : tasks) {
            if(task.getCategoryId().equals(category.getId()) && task.getStatus() == StatusType.AKTIVAN){
                return "Postoje aktivni zadaci ove kategorije.";
            }
        }
        return null;
    }

    public Boolean deleteById(String categoryId, String userId) {
        int rows = categoryRepository.delete(categoryId);
        taskService.deleteByCategory(categoryId, userId);
        return rows > 0;
    }

    public Category getCategoryById(String id) {
        return categoryRepository.getCategoryById(id);
    }
}
