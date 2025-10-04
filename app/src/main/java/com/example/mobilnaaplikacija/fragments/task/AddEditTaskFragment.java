package com.example.mobilnaaplikacija.fragments.task;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.databinding.DialogAddEditTaskBinding;
import com.example.mobilnaaplikacija.fragments.category.AddEditCategoryFragment;
import com.example.mobilnaaplikacija.model.Category;
import com.example.mobilnaaplikacija.model.DifficultyType;
import com.example.mobilnaaplikacija.model.FrequencyType;
import com.example.mobilnaaplikacija.model.ImportanceType;
import com.example.mobilnaaplikacija.model.StatusType;
import com.example.mobilnaaplikacija.model.Task;
import com.example.mobilnaaplikacija.model.UnitType;
import com.example.mobilnaaplikacija.services.CategoryService;
import com.example.mobilnaaplikacija.services.TaskService;
import com.example.mobilnaaplikacija.services.UserService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AddEditTaskFragment extends DialogFragment {

    private DialogAddEditTaskBinding binding;
    private TaskService taskService;
    private UserService userService;
    private CategoryService categoryService;
    private boolean isEditing, areDatesValid;
    private Task taskToUpdate;
    private Long startMillis = -1L, endMillis = -1L;
    private ArrayList<String> categoryNames;
    private ArrayList<String> categoryIds;
    private ArrayAdapter<String> categoryAdapter;
    private Category newCategory;

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
        binding = DialogAddEditTaskBinding.inflate(inflater, container, false);
        taskService = new TaskService(requireContext());
        userService = new UserService();
        categoryService = new CategoryService(getContext());
        categoryNames = new ArrayList<>();
        categoryIds = new ArrayList<>();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        isEditing = false;
        areDatesValid = true;
        taskToUpdate = null;
        newCategory = null;
        setupSpinners();

        if(getArguments() != null && getArguments().containsKey("Task to edit")){
            isEditing = true;
            taskToUpdate = getArguments().getParcelable("Task to edit");
            binding.tvAddTaskTitle.setText(R.string.task_edit_title);
            binding.etTaskName.setText(taskToUpdate.getName());
            binding.etTaskDescription.setText(taskToUpdate.getDescription());
            int categoryPos = categoryIds.indexOf(taskToUpdate.getCategoryId());
            if (categoryPos != -1)
                binding.spinnerCategory.setSelection(categoryPos);
            binding.categoryFields.setVisibility(View.GONE);
            binding.rbOneTime.setEnabled(false);
            binding.rbOneTime.setChecked(taskToUpdate.getFrequency() == FrequencyType.JEDNOKRATAN);
            binding.rbRepeat.setEnabled(false);
            binding.rbRepeat.setChecked(taskToUpdate.getFrequency() == FrequencyType.PONAVLJAJUCI);
            binding.spinnerStatus.setSelection((StatusType.valueOf(taskToUpdate.getStatus().name()).ordinal()));
            binding.spinnerStatus.setEnabled(false);
            parseMillisToDateTime(taskToUpdate);
            binding.etRecurringNumber.setText(taskToUpdate.getInterval() == null ? "0" : String.valueOf(taskToUpdate.getInterval()));
            binding.spinnerRecurringUnit.setSelection(taskToUpdate.getUnit() == null ? -1 : UnitType.valueOf(taskToUpdate.getUnit().name()).ordinal());
            binding.spinnerDifficulty.setSelection((DifficultyType.valueOf(taskToUpdate.getDifficulty().name()).ordinal()));
            binding.spinnerImportance.setSelection((ImportanceType.valueOf(taskToUpdate.getImportance().name()).ordinal()));
            setupStatusSpinner(taskToUpdate);
        }
        else {
            binding.spinnerStatus.setVisibility(View.GONE);
        }

        requireActivity().getSupportFragmentManager().setFragmentResultListener("Category managed", getViewLifecycleOwner(),
                (requestKey, result) -> {
                    Category newCategory = result.getParcelable("category");

                    if (newCategory != null) {
                        categoryIds.add(newCategory.getId());
                        categoryNames.add(newCategory.getName());
                        categoryAdapter.notifyDataSetChanged();

                        int position = categoryIds.indexOf(newCategory.getId());
                        if (position >= 0) {
                            binding.spinnerCategory.setSelection(position);
                        }
                        getChildFragmentManager().setFragmentResult("Category managed", result); // forward up to TaskList
        }});

        setupNewCategoryButton();
        setupDateTimePickers();
        setupFrequency();
        if (taskToUpdate != null  && (taskToUpdate.getStatus() == StatusType.OTKAZAN || taskToUpdate.getStatus() == StatusType.NEURAĐEN)) {
            binding.btnSaveTask.setVisibility(View.GONE);
        } else{
            binding.btnSaveTask.setVisibility(View.VISIBLE);
            setupSaveTaskButton();
        }
    }

    private void setupNewCategoryButton() {
        binding.btnSaveCategory.setOnClickListener(view -> {
            AddEditCategoryFragment fragment = new AddEditCategoryFragment();
            fragment.show(getChildFragmentManager(), "New category");
        });
    }

    private void parseMillisToDateTime(Task task) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        if (task.getStartMillis() != null) {
            Date dateTime = new Date(task.getStartMillis());
            binding.etStartDate.setText(dateFormat.format(dateTime));
            binding.etStartTime.setText(timeFormat.format(dateTime));
            binding.etTaskOccurringDate.setText(dateFormat.format(dateTime));
            startMillis = task.getStartMillis();
        }

        if (task.getEndMillis() != null) {
            Date endDate = new Date(task.getEndMillis());
            binding.etEndDate.setText(dateFormat.format(endDate));
            binding.etEndTime.setText(timeFormat.format(endDate));
            endMillis = task.getEndMillis();
        }

        if (task.getFrequency() == FrequencyType.PONAVLJAJUCI) {
            Pair<Long, Long> bounds = taskService.getTaskGroupBounds(task.getTaskId());
            if (bounds.first != null && bounds.second != null) {
                binding.etStartDate.setText(dateFormat.format(new Date(bounds.first)));
                binding.etEndDate.setText(dateFormat.format(new Date(bounds.second)));
            }
        }
    }

    private void setupSpinners(){
        ArrayList<Category> categories = new ArrayList<>(categoryService.getCategories());

        if (categories.isEmpty()) {
            categoryNames.add("Kategorija");
            categoryIds.add("-1");
            binding.spinnerCategory.setEnabled(false);
        } else {
            binding.spinnerCategory.setEnabled(true);
            for (Category c : categories) {
                categoryNames.add(c.getName());
                categoryIds.add(c.getId());
            }
        }

        categoryAdapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_dropdown_item, categoryNames);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(categoryAdapter);

        ArrayAdapter<UnitType> unitAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, UnitType.values());
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerRecurringUnit.setAdapter(unitAdapter);

        ArrayAdapter<DifficultyType> difficultyAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, DifficultyType.values());
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerDifficulty.setAdapter(difficultyAdapter);

        ArrayAdapter<ImportanceType> importanceAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, ImportanceType.values());
        importanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerImportance.setAdapter(importanceAdapter);
    }

    private void setupStatusSpinner (Task task) {
        task = taskService.autoUpdateStatus(task);
        ArrayList<StatusType> possibleStatuses = new ArrayList<>();
        StatusType current = task.getStatus();

        for (StatusType status : StatusType.values()) {
            if (taskService.canChangeStatus(task, status) || status == current)
                possibleStatuses.add(status);
        }

        ArrayAdapter<StatusType> statusAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, possibleStatuses);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerStatus.setAdapter(statusAdapter);

        if (current == StatusType.URAĐEN || current == StatusType.NEURAĐEN || current == StatusType.OTKAZAN)
            binding.spinnerStatus.setEnabled(false);

        int index = possibleStatuses.indexOf(task.getStatus());
        if (index >= 0) {
            binding.spinnerStatus.setSelection(index);
        }
    }

    private void setupDateTimePickers(){
        binding.etStartDate.setFocusable(false);
        binding.etStartDate.setClickable(true);
        binding.etStartTime.setFocusable(false);
        binding.etStartTime.setClickable(true);

        binding.etEndDate.setFocusable(false);
        binding.etEndDate.setClickable(true);
        binding.etEndTime.setFocusable(false);
        binding.etEndTime.setClickable(true);

        binding.etStartDate.setOnClickListener(view -> showDatePicker(binding.etStartDate, true));
        binding.etEndDate.setOnClickListener(view -> showDatePicker(binding.etEndDate, false));
        binding.etStartTime.setOnClickListener(view -> showTimePicker(binding.etStartTime, true));
        binding.etEndTime.setOnClickListener(view -> showTimePicker(binding.etEndTime, false));

    }

    private void showDatePicker(EditText field, boolean isStartDate){
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (DatePicker view, int year, int month, int dayOfMonth) ->{
                    String date = dayOfMonth + "/" + (month + 1) + "/" + year;
                    field.setText(date);

                    Calendar chosen = Calendar.getInstance();
                    chosen.set(Calendar.YEAR, year);
                    chosen.set(Calendar.MONTH, month);
                    chosen.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    chosen.set(Calendar.HOUR_OF_DAY, 0);
                    chosen.set(Calendar.MINUTE, 0);
                    chosen.set(Calendar.SECOND, 0);
                    chosen.set(Calendar.MILLISECOND, 0);

                    if (isStartDate) {
                        if (startMillis == -1) {
                            startMillis = chosen.getTimeInMillis();
                        } else {
                            Calendar tmp = Calendar.getInstance();
                            tmp.setTimeInMillis(startMillis);
                            tmp.set(Calendar.YEAR, year);
                            tmp.set(Calendar.MONTH, month);
                            tmp.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            startMillis = tmp.getTimeInMillis();
                        }
                    } else {
                        if (endMillis == -1) {
                            endMillis = chosen.getTimeInMillis();
                        } else {
                            Calendar tmp = Calendar.getInstance();
                            tmp.setTimeInMillis(endMillis);
                            tmp.set(Calendar.YEAR, year);
                            tmp.set(Calendar.MONTH, month);
                            tmp.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            endMillis = tmp.getTimeInMillis();
                        }
                    }

                    //Start i end izabrani, a ponovo se promijeni izbor
                    if (!binding.etStartDate.getText().toString().isEmpty()
                            && !binding.etEndDate.getText().toString().isEmpty()) {
                        String startStr = binding.etStartDate.getText().toString();
                        String endStr = binding.etEndDate.getText().toString();

                        SimpleDateFormat fmt = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
                        fmt.setLenient(false);

                        try {
                            Date startDate = fmt.parse(startStr);
                            Date endDate = fmt.parse(endStr);

                            if(startDate == null || endDate == null)
                                return;

                            if(endDate.before(startDate)) {
                                showError("Datum završetka je pre početka!");
                                areDatesValid = false;
                            } else
                                areDatesValid = true;
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        long today = System.currentTimeMillis() - 1000;
        datePickerDialog.getDatePicker().setMinDate(today);

        //Ne moze datum prije startnog da bira
        if (!isStartDate && !binding.etStartDate.getText().toString().isEmpty()) {
            try {
                String[] partsOfStart = binding.etStartDate.getText().toString().split("/");
                int day = Integer.parseInt(partsOfStart[0]);
                int month = Integer.parseInt(partsOfStart[1]) - 1;
                int year = Integer.parseInt(partsOfStart[2]);

                Calendar calendarStart = Calendar.getInstance();
                calendarStart.set(year, month, day);
                datePickerDialog.getDatePicker().setMinDate(calendarStart.getTimeInMillis());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        datePickerDialog.show();
    }

    private void showTimePicker(EditText field, boolean isStart) {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(requireContext(), (view, hour, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            field.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.getTime()));

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            field.setText(sdf.format(calendar.getTime()));

            if (isStart) {
                if (startMillis == -1) startMillis = calendar.getTimeInMillis();
                else {
                    Calendar tmp = Calendar.getInstance();
                    tmp.setTimeInMillis(startMillis);
                    tmp.set(Calendar.HOUR_OF_DAY, hour);
                    tmp.set(Calendar.MINUTE, minute);
                    tmp.set(Calendar.SECOND, 0);
                    startMillis = tmp.getTimeInMillis();
                }
            } else {
                if (endMillis == -1) endMillis = calendar.getTimeInMillis();
                else {
                    Calendar tmp = Calendar.getInstance();
                    tmp.setTimeInMillis(endMillis);
                    tmp.set(Calendar.HOUR_OF_DAY, hour);
                    tmp.set(Calendar.MINUTE, minute);
                    tmp.set(Calendar.SECOND, 0);
                    endMillis = tmp.getTimeInMillis();
                }
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    private void setupFrequency(){
        if(binding.rbRepeat.isChecked()){
            binding.layoutRecurringOptions.setVisibility(View.VISIBLE);
        } else {
            binding.layoutRecurringOptions.setVisibility(View.GONE);
        }

        binding.rgFrequency.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            if(checkedId == R.id.rbRepeat) {
                binding.layoutRecurringOptions.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.rbOneTime){
                binding.layoutRecurringOptions.setVisibility(View.GONE);
            }
        });
    }

    private void setupSaveTaskButton() {
        binding.btnSaveTask.setOnClickListener(view -> {
            Task task = new Task();

            //Polja novog/izmijenjenog zadatka
            String name = binding.etTaskName.getText().toString();
            String description = binding.etTaskDescription.getText().toString();
            int categoryPos = binding.spinnerCategory.getSelectedItemPosition();
            String categoryId = categoryIds.get(categoryPos);
            if (newCategory != null) {
                categoryId = newCategory.getId();
                categoryService.add(newCategory);
                Bundle result = new Bundle();
                result.putParcelable("category", newCategory);
                getParentFragmentManager().setFragmentResult("Category managed", result);
            }
            boolean isRepeating = binding.rbRepeat.isChecked();
            boolean isOneTime = binding.rbOneTime.isChecked();
            FrequencyType frequency = isRepeating ? FrequencyType.PONAVLJAJUCI : FrequencyType.JEDNOKRATAN;
            String startTime = binding.etStartTime.getText().toString().trim();
            String startDate = binding.etStartDate.getText().toString().trim();
            String endDate = binding.etEndDate.getText().toString().trim();
            String endTime = binding.etEndTime.getText().toString().trim();
            String difficulty = binding.spinnerDifficulty.getSelectedItem().toString();
            String importance = binding.spinnerImportance.getSelectedItem().toString();
            String recurringUnit = binding.spinnerRecurringUnit.getSelectedItem() != null
                    ? binding.spinnerRecurringUnit.getSelectedItem().toString()
                    : null;
            String recurringNumber = binding.etRecurringNumber.getText() != null
                    ? binding.etRecurringNumber.getText().toString()
                    : "0";

            //Validacija
            String error = taskService.validate(name, categoryId, isRepeating, isOneTime, startDate, endDate, startTime, endTime, startMillis, endMillis, difficulty, importance, recurringUnit, recurringNumber);
            if(error != null){
                showError(error);
                return;
            }
            if (isEditing) {
                String statusError = taskService.isStatusValid(binding.spinnerStatus.getSelectedItem().toString(), startMillis, endMillis);
                if (statusError != null) {
                    showError(statusError);
                    return;
                }
            }

            String isTimeValid = taskService.isTimeValid(startMillis, endMillis, isRepeating);
            if (isTimeValid != null) {
                showError(isTimeValid);
                return;
            }

            if(isEditing){
                task.setId(taskToUpdate.getId());
                task.setUserId(taskToUpdate.getUserId());
                task.setTaskId(taskToUpdate.getTaskId());
            } else {
                task.setUserId(userService.getCurrentUser().getUid());
            }
            if(isOneTime && !isEditing){
                task.setTaskId(UUID.randomUUID().toString());
            }
            task.setName(name);
            task.setDescription(description);
            task.setCategoryId(categoryId);
            task.setFrequency(frequency);
            if (isEditing)
                task.setStatus((StatusType)binding.spinnerStatus.getSelectedItem());
            else
                task.setStatus(StatusType.AKTIVAN);
            task.setStartMillis(startMillis);
            task.setEndMillis(endMillis);
            task.setDifficulty((DifficultyType)binding.spinnerDifficulty.getSelectedItem());
            task.setImportance((ImportanceType)binding.spinnerImportance.getSelectedItem());
            if (isOneTime) {
                task.setUnit(null);
                task.setInterval(0);
            } else {
                task.setUnit((UnitType) binding.spinnerRecurringUnit.getSelectedItem());
                task.setInterval(Integer.parseInt(recurringNumber));
            }

            // Čuvanje zadatka
            if (areDatesValid) {
                if (isEditing) {
                    if (taskToUpdate.getFrequency() == FrequencyType.PONAVLJAJUCI) {
                        new AlertDialog.Builder(requireContext())
                                .setTitle("Izmena zadatka")
                                .setMessage("Promene će važiti samo za buduće pojavljivanja ovog zadatka počevši od danas. " +
                                        "Završeni i odrađeni zadaci se neće menjati.\n\nDa li želite da nastavite?")
                                .setPositiveButton("Da", (dialog, which) -> {
                                    updateStartEndMillis(task);
                                    List<Task> list = taskService.updateFutureOccurrences(task);
                                    if (!list.isEmpty()) {
                                        Toast.makeText(requireContext(),
                                                "Zadatak izmenjen!",
                                                Toast.LENGTH_SHORT).show();
                                        sendBackToTaskList(task);
                                        dismiss();
                                    } else {
                                        Toast.makeText(requireContext(),
                                                "Greška pri izmeni zadatka!",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton("Ne", (dialog, which) -> dialog.dismiss())
                                .show();
                    } else {
                        Task updated = taskService.update(task);
                        if (updated != null) {
                            Toast.makeText(requireContext(), "Zadatak izmenjen!", Toast.LENGTH_SHORT).show();
                            sendBackToTaskList(task);
                            dismiss();
                        } else {
                            Toast.makeText(requireContext(), "Greška pri izmeni zadatka!", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    if (isRepeating)
                        taskService.addRepeatingTask(task);
                    else
                        taskService.add(task);

                    Toast.makeText(requireContext(), "Zadatak dodan!", Toast.LENGTH_SHORT).show();
                    sendBackToTaskList(task);
                    dismiss();
                }
            } else {
                showError("Datum završetka je pre početka!");
            }
        });
    }

    private void sendBackToTaskList(Task task) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("task", task);
        getParentFragmentManager().setFragmentResult("Task managed", bundle);
    }

    private void showError(String message){
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private long combineDateAndTimeToMillis(String dateStr, String timeStr) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy HH:mm", Locale.getDefault());
        Date dateTime;
        try {
            dateTime = dateFormat.parse(dateStr + " " + timeStr);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return dateTime != null ? dateTime.getTime() : 0;
    }

    private void updateStartEndMillis (Task task) {
        long startMillis = combineDateAndTimeToMillis(
                binding.etStartDate.getText().toString(),
                binding.etStartTime.getText().toString()
        );

        long endMillis = combineDateAndTimeToMillis(
                binding.etEndDate.getText().toString(),
                binding.etEndTime.getText().toString()
        );

        task.setStartMillis(startMillis);
        task.setEndMillis(endMillis);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}