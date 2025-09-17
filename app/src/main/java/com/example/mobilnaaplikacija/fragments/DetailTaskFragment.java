package com.example.mobilnaaplikacija.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.databinding.FragmentDetailTaskBinding;
import com.example.mobilnaaplikacija.model.FrequencyType;
import com.example.mobilnaaplikacija.model.Task;
import com.example.mobilnaaplikacija.model.UnitType;
import com.example.mobilnaaplikacija.services.TaskService;

public class DetailTaskFragment extends DialogFragment {
    private FragmentDetailTaskBinding binding;
    private Task taskToView;

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDetailTaskBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        taskToView = null;

        if (getArguments() != null && getArguments().containsKey("Task to view")) {
            taskToView = getArguments().getParcelable("Task to view");
            binding.tvTaskName.setText(taskToView.getName());
            binding.tvTaskDescription.setText(taskToView.getDescription());
            binding.tvTaskCategory.setText(taskToView.getStartDate()); //TODO category
            binding.tvTaskStartDate.setText(taskToView.getStartDate());
            binding.tvTaskEndDate.setText(taskToView.getEndDate());
            binding.recurringFields.setVisibility(taskToView.frequency == FrequencyType.PONAVLJAJUCI ? View.VISIBLE : View.GONE);
            binding.tvTaskRecurringNumber.setText(String.valueOf(taskToView.getInterval()));
            binding.tvTaskRecurringUnit.setText(taskToView.getUnit() == null ? "" : taskToView.getUnit().getDisplayName());
            binding.tvTaskDifficulty.setText(taskToView.getDifficulty().getDisplayName());
            binding.tvTaskImportance.setText(taskToView.getImportance().getDisplayName());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
