package com.example.mobilnaaplikacija.fragments.category;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.mobilnaaplikacija.databinding.DialogAddCategoryBinding;
import com.example.mobilnaaplikacija.model.Category;
import com.example.mobilnaaplikacija.services.CategoryService;
import com.github.dhaval2404.colorpicker.ColorPickerDialog;
import com.github.dhaval2404.colorpicker.listener.ColorListener;
import com.github.dhaval2404.colorpicker.model.ColorShape;

import org.jetbrains.annotations.NotNull;

public class AddCategoryFragment extends DialogFragment {

    private DialogAddCategoryBinding binding;
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
        binding = DialogAddCategoryBinding.inflate(inflater, container, false);
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
            new ColorPickerDialog
                    .Builder(getContext())
                    .setTitle("Izaberi boju")
                    .setColorShape(ColorShape.SQAURE)
                    .setPositiveButton("OK")
                    .setNegativeButton("Otkaži")
                    .setColorListener(new ColorListener() {
                        @Override
                        public void onColorSelected(int color, @NotNull String colorHex) {
                            pickedColor = color;
                            binding.btnPickedColor.setBackgroundTintList(ColorStateList.valueOf(pickedColor));
                        }
                    })
                    .show();
        });
    }

    private void setupAddCategoryButton(){
        binding.btnAddCategory.setOnClickListener(view -> {
            String name = binding.etCategoryName.getText().toString();

            //Validacija
            String error = categoryService.validate(name, pickedColor);
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                return;
            }

            //Čuvanje kategorije
            Category category = new Category();
            category.setColor(pickedColor);
            category.setName(name);
            categoryService.add(category);
            Toast.makeText(getContext(), "Kategorija dodana!", Toast.LENGTH_SHORT).show();
            sendBackToCategoryList(category);
            dismiss();
        });
    }

    private void sendBackToCategoryList(Category category) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("category", category);
        getParentFragmentManager().setFragmentResult("Category managed", bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}