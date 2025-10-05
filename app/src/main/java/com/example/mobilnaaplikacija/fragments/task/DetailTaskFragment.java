package com.example.mobilnaaplikacija.fragments.task;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.mobilnaaplikacija.databinding.DialogDetailTaskBinding;
import com.example.mobilnaaplikacija.model.Category;
import com.example.mobilnaaplikacija.model.enums.FrequencyType;
import com.example.mobilnaaplikacija.model.enums.StatusType;
import com.example.mobilnaaplikacija.model.Task;
import com.example.mobilnaaplikacija.services.CategoryService;
import com.example.mobilnaaplikacija.services.TaskService;
import com.google.firebase.firestore.FirebaseFirestore;

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
            setupRemoveTaskButton();
        }

        binding.btnEditTask.setOnClickListener(v -> {
            if (selectedTask.getStatus() == StatusType.NEURAĐEN) {
                Toast.makeText(requireContext(), "Ne mogu se menjati neurađeni zadaci.", Toast.LENGTH_SHORT).show();
                return;
            } else if (selectedTask.getStatus() == StatusType.OTKAZAN) {
                Toast.makeText(requireContext(), "Ne mogu se menjati otkazani zadaci.", Toast.LENGTH_SHORT).show();
                return;
            } else if (selectedTask.getStatus() == StatusType.URAĐEN) {
                Toast.makeText(requireContext(), "Ne mogu se menjati urađeni zadaci.", Toast.LENGTH_SHORT).show();
                return;
            } else if (taskService.isInPast(selectedTask)) {
                Toast.makeText(requireContext(), "Ne mogu se menjati završeni zadaci.", Toast.LENGTH_SHORT).show();
                return;
            }

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
            String period = dateFormat.format(task.getGroupStartMillis()) + " - " + dateFormat.format(task.getGroupEndMillis());
            binding.tvTaskPeriod.setText(period);
        }
    }


    private void setupRemoveTaskButton(){
        binding.btnRemoveTask.setOnClickListener(view -> {
            boolean removed = false;
            if (selectedTask.getStatus() == StatusType.URAĐEN) {
                Toast.makeText(requireContext(), "Nije moguće obrisati urađene zadatke.", Toast.LENGTH_SHORT).show();
                return;
            } else if (selectedTask.getStatus() == StatusType.OTKAZAN) {
                Toast.makeText(requireContext(), "Nije moguće obrisati otkazane zadatke.", Toast.LENGTH_SHORT).show();
                return;
            } else if (selectedTask.getStatus() == StatusType.NEURAĐEN) {
                Toast.makeText(requireContext(), "Nije moguće obrisati neurađene zadatke.", Toast.LENGTH_SHORT).show();
                return;
            } else if (taskService.isInPast(selectedTask)) {
                Toast.makeText(requireContext(), "Nije moguće obrisati zadatke koji su završeni.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedTask.getFrequency() == FrequencyType.JEDNOKRATAN){
                removed = taskService.deleteById(selectedTask.getId());
            } else {
                SimpleDateFormat fmt = new SimpleDateFormat("d. MMM yyyy, HH:mm", Locale.getDefault());
                String clickedDateStr = fmt.format(new Date());

                new AlertDialog.Builder(requireContext())
                        .setTitle("Brisanje ponavljajućeg zadatka")
                        .setMessage("Obrisaćeš sve ponavljajuće zadatke nakon ovog trenutka "
                                + clickedDateStr
                                + ". Prethodni ostaju sačuvani u kalendaru. Da li se slažeš?")
                        .setPositiveButton("Da", (dialog, which) -> {
                            boolean deleted = taskService.deleteFutureOccurrences(selectedTask.getTaskId());
                            showDeleteResult(deleted);
                        })
                        .setNegativeButton("Ne", null)
                        .show();
                return;
            }
            showDeleteResult(removed);
        });
    }

    private void showDeleteResult(boolean removed) {
        if (removed)
            Toast.makeText(requireContext(), "Zadatak izbrisan!", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(requireContext(), "Greška u brisanju zadatka!", Toast.LENGTH_SHORT).show();

        sendBackToTaskList(selectedTask);
        dismiss();
    }

    private void sendBackToTaskList(Task task) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("task", task);
        getParentFragmentManager().setFragmentResult("Task managed", bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
