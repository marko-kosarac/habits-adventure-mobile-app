package com.example.mobilnaaplikacija.fragments;

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
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.databinding.FragmentAddTaskBinding;
import com.example.mobilnaaplikacija.model.DifficultyType;
import com.example.mobilnaaplikacija.model.FrequencyType;
import com.example.mobilnaaplikacija.model.ImportanceType;
import com.example.mobilnaaplikacija.model.StatusType;
import com.example.mobilnaaplikacija.model.Task;
import com.example.mobilnaaplikacija.model.UnitType;
import com.example.mobilnaaplikacija.services.TaskService;
import com.example.mobilnaaplikacija.services.UserService;

import java.time.Duration;
import java.util.Calendar;
import java.util.Locale;

public class AddTaskFragment extends DialogFragment {

    private FragmentAddTaskBinding binding;
    private TaskService taskService;
    private UserService userService;
    private boolean isEditing, areDatesValid;
    private Task taskToUpdate, taskToView;

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
        binding = FragmentAddTaskBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        taskService = new TaskService(requireContext());
        userService = new UserService();

        isEditing = false;
        areDatesValid = true;
        taskToUpdate = null;
        taskToView = null;
        setupSpinners();

        if(getArguments() != null && getArguments().containsKey("Task to edit")){
            isEditing = true;
            taskToUpdate = getArguments().getParcelable("Task to edit");
            binding.etTaskName.setText(taskToUpdate.getName());
            binding.etTaskDescription.setText(taskToUpdate.getDescription());
            //binding.spinnerCategory.
            binding.btnSaveCategory.setText("Izmijeni kategoriju");
            binding.rbOneTime.setChecked(taskToUpdate.getFrequency() == FrequencyType.JEDNOKRATAN);
            binding.rbRepeat.setChecked(taskToUpdate.getFrequency() == FrequencyType.PONAVLJAJUCI);
            binding.etStartDate.setText(taskToUpdate.getStartDate());
            binding.etEndDate.setText(taskToUpdate.getEndDate());
            binding.etTime.setText(taskToUpdate.getTime());
            binding.rbWholeDay.setChecked(taskToUpdate.getWholeDay());
            binding.etReccuringNumber.setText(taskToUpdate.getInterval() == null ? "0" : String.valueOf(taskToUpdate.getInterval()));
            binding.spinnerRecurringUnit.setSelection(taskToUpdate.getUnit() == null ? -1 : UnitType.valueOf(taskToUpdate.getUnit().name()).ordinal());
            binding.spinnerDifficulty.setSelection((DifficultyType.valueOf(taskToUpdate.getDifficulty().name()).ordinal()));
            binding.spinnerImportance.setSelection((ImportanceType.valueOf(taskToUpdate.getImportance().name()).ordinal()));
            binding.btnSaveTask.setText("Izmijeni zadatak");
            setupRemoveTaskButton();
            binding.btnRemoveTask.setVisibility(View.VISIBLE);
        } else if(getArguments() != null && getArguments().containsKey("Task to view")){
            taskToView = getArguments().getParcelable("Task to view");
            binding.etTaskName.setText(taskToView.getName());
            binding.etTaskDescription.setText(taskToView.getDescription());
            //binding.spinnerCategory.
            binding.btnSaveCategory.setVisibility(View.GONE);
            binding.rbOneTime.setChecked(taskToView.getFrequency() == FrequencyType.JEDNOKRATAN);
            binding.rbRepeat.setChecked(taskToView.getFrequency() == FrequencyType.PONAVLJAJUCI);
            binding.etStartDate.setText(taskToView.getStartDate());
            binding.etEndDate.setText(taskToView.getEndDate());
            binding.etTime.setText(taskToView.getTime());
            binding.rbWholeDay.setChecked(taskToView.getWholeDay());
            binding.etReccuringNumber.setText(taskToView.getInterval() == null ? "0" : String.valueOf(taskToView.getInterval()));
            binding.spinnerRecurringUnit.setSelection(taskToView.getUnit() == null ? -1 : UnitType.valueOf(taskToView.getUnit().name()).ordinal());
            binding.spinnerDifficulty.setSelection((DifficultyType.valueOf(taskToView.getDifficulty().name()).ordinal()));
            binding.spinnerImportance.setSelection((ImportanceType.valueOf(taskToView.getImportance().name()).ordinal()));
            binding.btnSaveTask.setVisibility(View.GONE);
        } else {
            binding.btnSaveTask.setText("Dodaj zadatak");
        }

        setupDateTimePickers();
        setupFrequency();
        setupSaveTaskButton();
    }

    private void setupSpinners(){
        String[] categoryTypes = {"Zdravlje", "Učenje", "Zabava", "Sređivanje", "Sport", "Posao", "Porodica"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, categoryTypes);
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

    private void setupDateTimePickers(){
        binding.etStartDate.setFocusable(false);
        binding.etStartDate.setClickable(true);

        binding.etEndDate.setFocusable(false);
        binding.etEndDate.setClickable(true);

        binding.etTime.setFocusable(false);
        binding.etTime.setClickable(true);

        binding.etStartDate.setOnClickListener(view -> showDatePicker(binding.etStartDate, true));
        binding.etEndDate.setOnClickListener(view -> showDatePicker(binding.etEndDate, false));
        binding.etTime.setOnClickListener(view -> showTimePicker(binding.etTime));

        binding.rbWholeDay.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                binding.etTime.setText("");
                // If whole-day, start & end equal
                if (!binding.etStartDate.getText().toString().isEmpty()) {
                    binding.etEndDate.setText(binding.etStartDate.getText().toString());
                }
            }
        });
    }

    private void showDatePicker(android.widget.EditText field, boolean isStartDate){
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
            (DatePicker view, int year, int month, int dayOfMonth) ->{
            String date = dayOfMonth + "/" + (month + 1) + "/" + year;
            field.setText(date);

            // If whole-day, start & end equal
            if (binding.rbWholeDay.isChecked()) {
                if (isStartDate) {
                    binding.etEndDate.setText(date);
                } else {
                    binding.etStartDate.setText(date);
                }
            } else if (binding.rbOneTime.isChecked()) {
                // If non repeating and start, then start and end equal
                if (isStartDate) {
                    binding.etEndDate.setText(date);
                    Toast.makeText(requireContext(), "Kraj je postavljen na isti datum.", Toast.LENGTH_LONG).show();
                } else if (!binding.etEndDate.getText().toString().isEmpty()) {
                    binding.etStartDate.setText(date);
                    Toast.makeText(requireContext(), "Početak je postavljen na isti datum.", Toast.LENGTH_LONG).show();
                }
            }

            //Start and end picked, but changed start
            if (isStartDate && !binding.etEndDate.getText().toString().isEmpty()) {
                try {
                    String[] partsStart = binding.etStartDate.getText().toString().split("/");
                    int dayStart = Integer.parseInt(partsStart[0]);
                    int monthStart = Integer.parseInt(partsStart[1]) - 1;
                    int yearStart = Integer.parseInt(partsStart[2]);
                    Calendar calStart = Calendar.getInstance();
                    calStart.set(yearStart, monthStart, dayStart);

                    String[] partsEnd = binding.etEndDate.getText().toString().split("/");
                    int dayEnd = Integer.parseInt(partsEnd[0]);
                    int monthEnd = Integer.parseInt(partsEnd[1]) - 1;
                    int yearEnd = Integer.parseInt(partsEnd[2]);
                    Calendar calEnd = Calendar.getInstance();
                    calEnd.set(yearEnd, monthEnd, dayEnd);

                    if (calEnd.before(calStart)) {
                        areDatesValid = false;
                        showError("Datum završetka je prije početka!");
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

        //Prevent choosing older than start date
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

    private void showTimePicker(android.widget.EditText field){
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (TimePicker view, int hourOfDay, int minute) -> {
                    field.setText(String.format("%02d:%02d", hourOfDay, minute));
                    binding.rbWholeDay.setChecked(false);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);
        timePickerDialog.show();
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
                binding.etReccuringNumber.setText("");
                binding.spinnerRecurringUnit.setSelection(0);
            }
        });
    }

    private void setupRemoveTaskButton(){
        binding.btnRemoveTask.setOnClickListener(view -> {
            boolean removed = taskService.deleteById(taskToUpdate.getId());
            if (removed)
                Toast.makeText(requireContext(), "Zadatak izbrisan!", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(requireContext(), "Greška u brisanju zadatka!", Toast.LENGTH_SHORT).show();
            sendBackToTaskList(taskToUpdate);
            dismiss();
        });
    }
    private void setupSaveTaskButton() {
        binding.btnSaveTask.setOnClickListener(view -> {
            Task task = new Task();

            //Polja novog/izmijenjenog zadatka
            String name = binding.etTaskName.getText().toString();
            String description = binding.etTaskDescription.getText().toString();
            String category = binding.spinnerCategory.getSelectedItem().toString();
            boolean isRepeating = binding.rbRepeat.isChecked();
            boolean isOneTime = binding.rbOneTime.isChecked();
            FrequencyType frequency = isRepeating ? FrequencyType.PONAVLJAJUCI : FrequencyType.JEDNOKRATAN;
            String startDate = binding.etStartDate.getText().toString().trim();
            String endDate = binding.etEndDate.getText().toString().trim();
            String time = binding.etTime.getText().toString().trim();
            boolean isWholeDay = binding.rbWholeDay.isChecked();
            String difficulty = binding.spinnerDifficulty.getSelectedItem().toString();
            String importance = binding.spinnerImportance.getSelectedItem().toString();
            String recurringUnit = binding.spinnerRecurringUnit.getSelectedItem() != null
                    ? binding.spinnerRecurringUnit.getSelectedItem().toString()
                    : "";
            String recurringNumber = binding.etReccuringNumber.getText() != null
                    ? binding.etReccuringNumber.getText().toString()
                    : "0";

            //Validacija
            String error = taskService.validate(name, category, isRepeating, isOneTime, startDate, endDate,
                    time, isWholeDay, difficulty, importance, recurringUnit, recurringNumber);
            if(error != null){
                showError(error);
                return;
            }

            if(isEditing){
                task.setId(taskToUpdate.getId());
                task.setUserId(taskToUpdate.getUserId());
            } else {
                task.setUserId(userService.getCurrentUser().getUid());
            }
            task.setName(name);
            task.setDescription(description);
            task.setCategoryId("-1"); //TODO set categoryId
            task.setFrequency(frequency);
            task.setStartDate(startDate);
            task.setEndDate(endDate);
            task.setTime(time);
            task.setWholeDay(isWholeDay);
            task.setDifficulty((DifficultyType)binding.spinnerDifficulty.getSelectedItem());
            task.setImportance((ImportanceType)binding.spinnerImportance.getSelectedItem());
            task.setUnit((UnitType) binding.spinnerRecurringUnit.getSelectedItem());
            task.setInterval(recurringNumber.trim().isEmpty() ? 0 : Integer.parseInt(recurringNumber));
            task.setStatus(StatusType.AKTIVAN); //TODO set status

            //Čuvanje zadatka
            if (areDatesValid) {
                if(isEditing){
                    task = taskService.update(task);
                    Toast.makeText(requireContext(), "Zadatak izmijenjen!", Toast.LENGTH_SHORT).show();
                } else {
                    task = taskService.add(task);
                    Toast.makeText(requireContext(), "Zadatak dodan!", Toast.LENGTH_SHORT).show();
                }
                sendBackToTaskList(task);
                dismiss();
            } else
                showError("Datum završetka je prije početka!");
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}