package com.example.mobilnaaplikacija.fragments.task;

import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.mobilnaaplikacija.databinding.DialogDetailTaskBinding;
import com.example.mobilnaaplikacija.model.Category;
import com.example.mobilnaaplikacija.model.FrequencyType;
import com.example.mobilnaaplikacija.model.StatusType;
import com.example.mobilnaaplikacija.model.Task;
import com.example.mobilnaaplikacija.services.CategoryService;
import com.example.mobilnaaplikacija.services.TaskService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DetailTaskFragment extends DialogFragment {
    private DialogDetailTaskBinding binding;
    private Task selectedTask;
    private CategoryService categoryService;
    private TaskService taskService;

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
        binding = DialogDetailTaskBinding.inflate(inflater, container, false);
        categoryService = new CategoryService(getContext());
        taskService = new TaskService(getContext());
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
            Category category = categoryService.getCategoryById(selectedTask.getCategoryId());
            String categoryName = (category == null) ? "Nema kategoriju" : category.getName();
            binding.tvTaskCategory.setText(categoryName);
            binding.tvTaskCategory.setTextColor(category.getColor());
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
            binding.tvTaskOccurringDate.setText(dateFormat.format(startDateTime));
            binding.tvTaskStartTime.setText(timeFormat.format(startDateTime));
        }

        if (task.getEndMillis() != null) {
            Date endDateTime = new Date(task.getEndMillis());
            binding.tvTaskEndTime.setText(timeFormat.format(endDateTime));
        }

        if (task.getFrequency() == FrequencyType.PONAVLJAJUCI) {
            Pair<Long, Long> bounds = taskService.getTaskGroupBounds(task.getTaskId());
            if (bounds.first != null && bounds.second != null) {
                String period = dateFormat.format(new Date(bounds.first))
                        + " - " + dateFormat.format(new Date(bounds.second));
                binding.tvTaskPeriod.setText(period);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
