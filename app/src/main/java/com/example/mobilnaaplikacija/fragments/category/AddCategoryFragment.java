package com.example.mobilnaaplikacija.fragments.category;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.mobilnaaplikacija.databinding.FragmentAddCategoryBinding;
import com.example.mobilnaaplikacija.model.Category;
import com.example.mobilnaaplikacija.services.CategoryService;

public class AddCategoryFragment extends DialogFragment {

    private FragmentAddCategoryBinding binding;
    private CategoryService categoryService;
    private int pickedColor = Color.WHITE;

    public AddCategoryFragment() {}

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        categoryService = new CategoryService(getContext());
        binding = FragmentAddCategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showColorPicker();
        setupAddCategoryButton();
    }

    private void showColorPicker() {
        binding.btnPickColor.setOnClickListener(view -> {
            //ColorPicker
        });
    }

    private void setupAddCategoryButton(){
        binding.btnAddCategory.setOnClickListener(view -> {
            String name = binding.etCategoryName.getText().toString();

            String error = categoryService.validate(name, pickedColor);
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                return;
            }

            Category category = new Category();
            category.setColor(pickedColor);
            category.setName(name);
            categoryService.add(category);
            Toast.makeText(getContext(), "Kategorija dodana!", Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }
}