package com.example.mobilnaaplikacija.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.mobilnaaplikacija.databinding.FragmentDetailTaskBinding;
import com.example.mobilnaaplikacija.model.FrequencyType;
import com.example.mobilnaaplikacija.model.Task;

public class DetailTaskFragment extends DialogFragment {
    private FragmentDetailTaskBinding binding;
    private Task selectedTask;

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
        selectedTask = null;

        if (getArguments() != null && getArguments().containsKey("Task to view")) {
            selectedTask = getArguments().getParcelable("Task to view");
            binding.tvTaskName.setText(selectedTask.getName());
            binding.tvTaskDescription.setText(selectedTask.getDescription());
            binding.tvTaskCategory.setText(selectedTask.getStartDate()); //TODO category
            binding.tvTaskStartDate.setText(selectedTask.getStartDate());
            binding.tvTaskEndDate.setText(selectedTask.getEndDate());
            binding.recurringFields.setVisibility(selectedTask.frequency == FrequencyType.PONAVLJAJUCI ? View.VISIBLE : View.GONE);
            binding.tvTaskRecurringNumber.setText(String.valueOf(selectedTask.getInterval()));
            binding.tvTaskRecurringUnit.setText(selectedTask.getUnit() == null ? "" : selectedTask.getUnit().getDisplayName());
            binding.tvTaskDifficulty.setText(selectedTask.getDifficulty().getDisplayName());
            binding.tvTaskImportance.setText(selectedTask.getImportance().getDisplayName());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
