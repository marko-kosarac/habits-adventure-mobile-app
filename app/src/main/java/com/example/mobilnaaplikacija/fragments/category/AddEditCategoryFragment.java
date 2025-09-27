package com.example.mobilnaaplikacija.fragments.category;

import android.app.AlertDialog;
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

import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.databinding.DialogAddEditCategoryBinding;
import com.example.mobilnaaplikacija.model.Category;
import com.example.mobilnaaplikacija.services.CategoryService;
import com.example.mobilnaaplikacija.services.UserService;
import com.github.dhaval2404.colorpicker.ColorPickerDialog;
import com.github.dhaval2404.colorpicker.listener.ColorListener;
import com.github.dhaval2404.colorpicker.model.ColorShape;

import org.jetbrains.annotations.NotNull;

public class AddEditCategoryFragment extends DialogFragment {

    private DialogAddEditCategoryBinding binding;
    private CategoryService categoryService;
    private int pickedColor = Color.WHITE;
    private boolean isEditing;
    private Category categoryToUpdate;
    private UserService userService;

    public AddEditCategoryFragment() {}

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        categoryService = new CategoryService(getContext());
        userService = new UserService();
        binding = DialogAddEditCategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isEditing = false;
        categoryToUpdate = null;

        if(getArguments() != null && getArguments().containsKey("Category to edit")) {
            isEditing = true;
            categoryToUpdate = getArguments().getParcelable("Category to edit");
            binding.tvAddCategoryTitle.setText(getString(R.string.category_edit));
            binding.etCategoryName.setText(categoryToUpdate.getName());
            binding.etCategoryName.setEnabled(false);
            binding.btnPickedColor.setBackgroundTintList(ColorStateList.valueOf(categoryToUpdate.getColor()));
            binding.btnPickColor.setText(getString(R.string.category_edit_color));
            setupRemoveCategoryButton();
        }
        showColorPicker();
        setupSaveCategoryButton();
    }

    private void showColorPicker() {
        binding.btnPickColor.setOnClickListener(view -> {
            new ColorPickerDialog
                    .Builder(requireContext())
                    .setTitle(R.string.category_choose_color)
                    .setColorShape(ColorShape.SQAURE)
                    .setPositiveButton(getString(R.string.color_picker_ok))
                    .setNegativeButton(R.string.color_picker_decline)
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

    private void setupSaveCategoryButton(){
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
            if(isEditing)
                category.setId(categoryToUpdate.getId());
            category.setColor(pickedColor);
            category.setName(name);

            if(isEditing){
                category = categoryService.update(category);
                Toast.makeText(requireContext(), "Kategorija izmenjena!", Toast.LENGTH_SHORT).show();
            } else {
                category = categoryService.add(category);
                Toast.makeText(getContext(), "Kategorija dodana!", Toast.LENGTH_SHORT).show();
            }
            sendBackToCategoryList(category);
            dismiss();
        });
    }

    private void sendBackToCategoryList(Category category) {
        if (category == null)
            return;
        Bundle bundle = new Bundle();
        bundle.putParcelable("category", category);
        requireActivity().getSupportFragmentManager()
                .setFragmentResult("Category managed", bundle);
    }

    private void setupRemoveCategoryButton(){
        binding.btnRemoveCategory.setVisibility(View.VISIBLE);
        binding.btnRemoveCategory.setOnClickListener(view -> {
            String error = categoryService.canBeRemoved(categoryToUpdate, userService.getCurrentUser().getUid());
            if (error == null) {
                confirmDeleteCategory(categoryToUpdate);
            } else {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDeleteCategory(Category category) {
        new AlertDialog.Builder(requireActivity())
                .setTitle("Brisanje kategorije")
                .setMessage("Brisanjem kategorije obrisaćete i sve zadatke u njoj. Da li ste sigurni?")
                .setPositiveButton("Obriši", (dialog, which) -> {
                    boolean isRemoved = categoryService.deleteById(categoryToUpdate.getId(), userService.getCurrentUser().getUid());
                    if (isRemoved) {
                        Toast.makeText(getActivity(), "Kategorija izbrisana!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Greška u brisanju kategorije!", Toast.LENGTH_SHORT).show();
                    }
                    sendBackToCategoryList(category);
                    dismiss();
                })
                .setNegativeButton("Otkaži", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}