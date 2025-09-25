package com.example.mobilnaaplikacija.fragments.task;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.mobilnaaplikacija.databinding.FragmentDetailTaskBinding;
import com.example.mobilnaaplikacija.model.FrequencyType;
import com.example.mobilnaaplikacija.model.StatusType;
import com.example.mobilnaaplikacija.model.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
            binding.tvTaskCategory.setText(selectedTask.getName()); //TODO category
            binding.tvTaskStatus.setText(selectedTask.getStatus() == null ? "" : selectedTask.getStatus().getDisplayName());
            parseMillisToDateTime(selectedTask);
            binding.recurringFields.setVisibility(selectedTask.getFrequency() == FrequencyType.PONAVLJAJUCI ? View.VISIBLE : View.GONE);
            binding.tvTaskRecurringNumber.setText(String.valueOf(selectedTask.getInterval()));
            binding.tvTaskRecurringUnit.setText(selectedTask.getUnit() == null ? "" : selectedTask.getUnit().getDisplayName());
            binding.tvTaskDifficulty.setText(selectedTask.getDifficulty().getDisplayName());
            binding.tvTaskImportance.setText(selectedTask.getImportance().getDisplayName());

            if (selectedTask.getStatus() == StatusType.OTKAZAN || selectedTask.getStatus() == StatusType.NEURAĐEN)
                binding.btnEditTask.setVisibility(View.GONE);
        }

        binding.btnEditTask.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putParcelable("Task to edit", selectedTask);
            AddEditTaskFragment fragment = new AddEditTaskFragment();
            fragment.setArguments(args);
            fragment.show(getChildFragmentManager(), "Edit task");
        });

        getChildFragmentManager().setFragmentResultListener("Task managed", getViewLifecycleOwner(), (requestKey, result) -> {
            getParentFragmentManager().setFragmentResult("Task managed", result);
            dismiss();
        });
    }

    private void parseMillisToDateTime(Task task) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        if (task.getStartMillis() != null) {
            Date startDateTime = new Date(task.getStartMillis());
            binding.tvTaskStartDate.setText(dateFormat.format(startDateTime));
            binding.tvTaskStartTime.setText(timeFormat.format(startDateTime));
        }

        if (task.getEndMillis() != null) {
            Date endDateTime = new Date(task.getEndMillis());
            binding.tvTaskEndDate.setText(dateFormat.format(endDateTime));
            binding.tvTaskEndTime.setText(timeFormat.format(endDateTime));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
