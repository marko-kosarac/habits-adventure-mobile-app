package com.example.mobilnaaplikacija.fragments.task;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.util.Log;
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
import com.example.mobilnaaplikacija.model.enums.DifficultyType;
import com.example.mobilnaaplikacija.model.enums.FrequencyType;
import com.example.mobilnaaplikacija.model.enums.ImportanceType;
import com.example.mobilnaaplikacija.model.enums.StatusType;
import com.example.mobilnaaplikacija.model.Task;
import com.example.mobilnaaplikacija.model.enums.UnitType;
import com.example.mobilnaaplikacija.services.task.CategoryService;
import com.example.mobilnaaplikacija.services.task.TaskService;
import com.example.mobilnaaplikacija.services.UserService;
import com.google.firebase.auth.FirebaseUser;

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
    private FirebaseUser user;

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

        user = userService.getCurrentUser();
        if (user == null) return;

        isEditing = false;
        areDatesValid = true;
        taskToUpdate = null;
        newCategory = null;

        setupSpinners();
        setupNewCategoryButton();
        setupDateTimePickers();
        setupFrequency();

        if (getArguments() != null && getArguments().containsKey("Task to edit")) {
            loadTaskForEditing();
        } else {
            binding.spinnerStatus.setVisibility(View.GONE);
        }

        setupCategoryListener();
        setupSaveTaskButton();
    }

    private void loadTaskForEditing() {
        isEditing = true;
        taskToUpdate = getArguments().getParcelable("Task to edit");
        if (taskToUpdate == null) return;

        binding.tvAddTaskTitle.setText(R.string.task_edit_title);
        binding.etTaskName.setText(taskToUpdate.getName());
        binding.etTaskDescription.setText(taskToUpdate.getDescription());

        int categoryPos = categoryIds.indexOf(taskToUpdate.getCategoryId());
        if (categoryPos != -1) binding.spinnerCategory.setSelection(categoryPos);

        binding.categoryFields.setVisibility(View.GONE);
        binding.rbOneTime.setEnabled(false);
        binding.rbOneTime.setChecked(taskToUpdate.getFrequency() == FrequencyType.JEDNOKRATAN);
        binding.rbRepeat.setEnabled(false);
        binding.rbRepeat.setChecked(taskToUpdate.getFrequency() == FrequencyType.PONAVLJAJUCI);

        binding.spinnerStatus.setSelection(StatusType.valueOf(taskToUpdate.getStatus().name()).ordinal());
        binding.spinnerStatus.setEnabled(false);

        parseMillisToDateTime(taskToUpdate);
        binding.etRecurringNumber.setText(taskToUpdate.getInterval() == null ? "0" : String.valueOf(taskToUpdate.getInterval()));
        binding.spinnerRecurringUnit.setSelection(taskToUpdate.getUnit() == null ? -1 : UnitType.valueOf(taskToUpdate.getUnit().name()).ordinal());
        binding.spinnerDifficulty.setSelection(DifficultyType.valueOf(taskToUpdate.getDifficulty().name()).ordinal());
        binding.spinnerImportance.setSelection(ImportanceType.valueOf(taskToUpdate.getImportance().name()).ordinal());
        setupStatusSpinner(taskToUpdate);

        if (taskToUpdate.getStatus() == StatusType.OTKAZAN || taskToUpdate.getStatus() == StatusType.NEURAĐEN) {
            binding.btnSaveTask.setVisibility(View.GONE);
        }
    }

    private void setupCategoryListener() {
        requireActivity().getSupportFragmentManager().setFragmentResultListener("Category managed", getViewLifecycleOwner(),
                (requestKey, result) -> {
                    Category newCategory = result.getParcelable("category");
                    if (newCategory != null) {
                        categoryIds.add(newCategory.getId());
                        categoryNames.add(newCategory.getName());
                        categoryAdapter.notifyDataSetChanged();

                        int position = categoryIds.indexOf(newCategory.getId());
                        if (position >= 0) binding.spinnerCategory.setSelection(position);

                        getChildFragmentManager().setFragmentResult("Category managed", result); // forward up
                    }
                });
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
            binding.etStartDate.setText(dateFormat.format(task.getGroupStartMillis()));
            binding.etEndDate.setText(dateFormat.format(task.getGroupEndMillis()));
        }
    }

    private void setupSpinners(){
        ArrayList<Category> categories = new ArrayList<>(categoryService.getCategoriesByUser(user.getUid()));
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

        categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, categoryNames);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(categoryAdapter);

        binding.spinnerRecurringUnit.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, UnitType.values()));
        binding.spinnerDifficulty.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, DifficultyType.values()));
        binding.spinnerImportance.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, ImportanceType.values()));
    }

    private void setupStatusSpinner(Task task) {
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
        if (index >= 0) binding.spinnerStatus.setSelection(index);
    }

    private void setupDateTimePickers() {
        setupDateTimeField(binding.etStartDate, true);
        setupDateTimeField(binding.etStartTime, true);
        setupDateTimeField(binding.etEndDate, false);
        setupDateTimeField(binding.etEndTime, false);
    }

    private void setupDateTimeField(EditText field, boolean isStart) {
        field.setFocusable(false);
        field.setClickable(true);
        if (field == binding.etStartDate || field == binding.etEndDate) {
            field.setOnClickListener(v -> showDatePicker(field, isStart));
        } else {
            field.setOnClickListener(v -> showTimePicker(field, isStart));
        }
    }

    private void showDatePicker(EditText field, boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    field.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    Calendar chosen = Calendar.getInstance();
                    chosen.set(year, month, dayOfMonth, 0, 0, 0);
                    chosen.set(Calendar.MILLISECOND, 0);

                    if (isStartDate) startMillis = chosen.getTimeInMillis();
                    else endMillis = chosen.getTimeInMillis();

                    validateDates();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        long today = System.currentTimeMillis() - 1000;
        datePickerDialog.getDatePicker().setMinDate(today);

        if (!isStartDate && !binding.etStartDate.getText().toString().isEmpty()) {
            try {
                String[] parts = binding.etStartDate.getText().toString().split("/");
                int day = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]) - 1;
                int year = Integer.parseInt(parts[2]);
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

            if (isStart) startMillis = combineTimeToMillis(startMillis, hour, minute);
            else endMillis = combineTimeToMillis(endMillis, hour, minute);

        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    private long combineTimeToMillis(long millis, int hour, int minute) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis != -1 ? millis : System.currentTimeMillis());
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private void setupFrequency() {
        binding.layoutRecurringOptions.setVisibility(binding.rbRepeat.isChecked() ? View.VISIBLE : View.GONE);

        binding.rgFrequency.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbRepeat) binding.layoutRecurringOptions.setVisibility(View.VISIBLE);
            else binding.layoutRecurringOptions.setVisibility(View.GONE);
        });
    }

    private void setupSaveTaskButton() {
        binding.btnSaveTask.setOnClickListener(view -> {
            Task task = new Task();

            String name = binding.etTaskName.getText().toString().trim();
            String description = binding.etTaskDescription.getText().toString().trim();
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

            String startDate = binding.etStartDate.getText().toString().trim();
            String endDate = binding.etEndDate.getText().toString().trim();
            String startTime = binding.etStartTime.getText().toString().trim();
            String endTime = binding.etEndTime.getText().toString().trim();

            String recurringUnit = binding.spinnerRecurringUnit.getSelectedItem() != null
                    ? binding.spinnerRecurringUnit.getSelectedItem().toString()
                    : null;
            String recurringNumber = binding.etRecurringNumber.getText() != null
                    ? binding.etRecurringNumber.getText().toString()
                    : "0";

            String difficulty = binding.spinnerDifficulty.getSelectedItem().toString();
            String importance = binding.spinnerImportance.getSelectedItem().toString();

            // Validacija
            String error = taskService.validate(name, categoryId, isRepeating, isOneTime,
                    startDate, endDate, startTime, endTime, startMillis, endMillis, difficulty, importance, recurringUnit, recurringNumber);
            if (error != null) { showError(error); return; }

            if (isEditing) {
                String statusError = taskService.isStatusValid(binding.spinnerStatus.getSelectedItem().toString(), startMillis, endMillis);
                if (statusError != null) { showError(statusError); return; }
            }

            String timeError = taskService.isTimeValid(startMillis, endMillis, isRepeating);
            if (timeError != null) { showError(timeError); return; }

            if (isEditing) {
                task.setId(taskToUpdate.getId());
                task.setUserId(taskToUpdate.getUserId());
                task.setTaskId(taskToUpdate.getTaskId());
                task.setQuotaReached(taskToUpdate.isQuotaReached());
                task.setStatusTimestamp(taskToUpdate.getStatusTimestamp());
            } else {
                task.setUserId(userService.getCurrentUser().getUid());
                task.setQuotaReached(false);
                task.setStatusTimestamp(System.currentTimeMillis());
            }

            if (isOneTime && !isEditing) task.setTaskId(UUID.randomUUID().toString());

            task.setName(name);
            task.setDescription(description);
            task.setCategoryId(categoryId);
            task.setFrequency(frequency);
            task.setStartMillis(startMillis);
            task.setEndMillis(endMillis);
            task.setGroupStartMillis(startMillis);
            task.setGroupEndMillis(endMillis);
            task.setDifficulty((DifficultyType) binding.spinnerDifficulty.getSelectedItem());
            task.setImportance((ImportanceType) binding.spinnerImportance.getSelectedItem());

            if (isEditing) task.setStatus((StatusType) binding.spinnerStatus.getSelectedItem());
            else task.setStatus(StatusType.AKTIVAN);

            if (isOneTime) {
                task.setUnit(null);
                task.setInterval(0);
            } else {
                task.setUnit((UnitType) binding.spinnerRecurringUnit.getSelectedItem());
                task.setInterval(Integer.parseInt(recurringNumber));
            }
            save(task);
        });
    }

    private void save(Task task) {
        if (!areDatesValid) { showError("Datum završetka je pre početka!"); return; }

        if (isEditing) {
            if (taskToUpdate.getFrequency() == FrequencyType.PONAVLJAJUCI) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Izmena zadatka")
                        .setMessage("Promene će važiti samo za buduće pojavljivanja ovog zadatka počevši od danas. Završeni i odrađeni zadaci se neće menjati.\n\nDa li želite da nastavite?")
                        .setPositiveButton("Da", (dialog, which) -> {
                            updateStartEndMillis(task);
                            List<Task> updatedList = taskService.updateFutureOccurrences(task);
                            if (!updatedList.isEmpty()) {
                                showToast("Zadatak izmenjen!");
                                sendBackToTaskList(task);
                                updateStreak();
                                dismiss();
                            } else showToast("Greška pri izmeni zadatka!");
                        })
                        .setNegativeButton("Ne", (dialog, which) -> dialog.dismiss())
                        .show();
            } else {
                Task updated = taskService.update(task);
                if (updated != null) {
                    showToast("Zadatak izmenjen!");
                    sendBackToTaskList(task);
                    updateStreak();
                    dismiss();
                } else showToast("Greška pri izmeni zadatka!");
            }
        } else {
            if (task.getFrequency() == FrequencyType.PONAVLJAJUCI)
                taskService.addRepeatingTask(task);
            else
                taskService.add(task);

            showToast("Zadatak dodan!");
            sendBackToTaskList(task);
            updateStreak();
            dismiss();
        }
    }

    private void sendBackToTaskList(Task task) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("task", task);
        getParentFragmentManager().setFragmentResult("Task managed", bundle);
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private long combineDateAndTimeToMillis(String dateStr, String timeStr) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy HH:mm", Locale.getDefault());
        Date dateTime;
        try { dateTime = dateFormat.parse(dateStr + " " + timeStr); }
        catch (Exception e) { throw new RuntimeException(e); }
        return dateTime != null ? dateTime.getTime() : 0;
    }

    private void updateStartEndMillis(Task task) {
        task.setStartMillis(combineDateAndTimeToMillis(binding.etStartDate.getText().toString(), binding.etStartTime.getText().toString()));
        task.setEndMillis(combineDateAndTimeToMillis(binding.etEndDate.getText().toString(), binding.etEndTime.getText().toString()));
        task.setGroupStartMillis(task.getStartMillis());
        task.setGroupEndMillis(task.getEndMillis());
    }

    private void validateDates() {
        if (binding.etStartDate.getText().toString().isEmpty() || binding.etEndDate.getText().toString().isEmpty()) return;
        SimpleDateFormat fmt = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
        fmt.setLenient(false);
        try {
            Date startDateObj = fmt.parse(binding.etStartDate.getText().toString());
            Date endDateObj = fmt.parse(binding.etEndDate.getText().toString());
            if (startDateObj != null && endDateObj != null) {
                areDatesValid = !endDateObj.before(startDateObj);
                if (!areDatesValid) showError("Datum završetka je pre početka!");
            }
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private void updateStreak() {
        String currentUserId = userService.getCurrentUser().getUid();
        userService.updateActiveDaysOnTaskAction(currentUserId);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
