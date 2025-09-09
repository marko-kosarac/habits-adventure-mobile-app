package com.example.mobilnaaplikacija.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.databinding.FragmentAddTaskBinding;
import com.example.mobilnaaplikacija.model.Task;
import com.example.mobilnaaplikacija.services.TaskService;

import java.util.Calendar;

public class AddTaskFragment extends Fragment {

    private FragmentAddTaskBinding binding;
    private TaskService taskService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddTaskBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupSpinners();
        setupDateTimePickers();
        setupFrequency();
        setupSaveTaskButton();
    }

    private void setupSpinners(){
        String[] categoryTypes = {"Zdravlje", "Učenje", "Zabava", "Sređivanje",
                "Sport", "Posao", "Porodica", "Putovanje", "Lični razvoj", "Finansije"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, categoryTypes);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(categoryAdapter);

        String[] unitTypes = {"Dan", "Sedmica", "Mjesec", "Godina"};
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, unitTypes);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerRecurringUnit.setAdapter(unitAdapter);

        String[] difficultyTypes = {"Veoma lak", "Lak", "Težak", "Ekstremno težak"};
        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, difficultyTypes);
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerDifficulty.setAdapter(difficultyAdapter);

        String[] importanceTypes = {"Normalan", "Važan", "Ekstremno važan", "Specijalan"};
        ArrayAdapter<String> importanceAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, importanceTypes);
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
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
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
        binding.layoutRecurringOptions.setVisibility(View.GONE);

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

    private void setupSaveTaskButton() {
        binding.btnSaveTask.setOnClickListener(view -> {
            taskService = new TaskService(requireContext());

            Task task = taskService.validateAndCreateTask(
                    binding.etTaskName.getText().toString(),
                    binding.etTaskDescription.getText().toString(),
                    binding.spinnerCategory.getSelectedItem().toString(),
                    binding.rbRepeat.isChecked(),
                    binding.rbOneTime.isChecked(),
                    binding.etStartDate.getText().toString().trim(),
                    binding.etEndDate.getText().toString().trim(),
                    binding.etTime.getText().toString().trim(),
                    binding.rbWholeDay.isChecked(),
                    binding.spinnerDifficulty.getSelectedItem().toString(),
                    binding.spinnerImportance.getSelectedItem().toString(),
                    binding.spinnerRecurringUnit.getSelectedItem().toString(),
                    binding.etReccuringNumber.getText().toString()
            );

            if (task == null) return;

            long userId = 1L; // TODO: fetch from logged-in user
            long id = taskService.saveTask(task, userId);

            if (id != -1) {
                task.setId(id);
                sendTaskBackToHomePage(task);
                Toast.makeText(requireContext(), "Zadatak dodan!", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(view).popBackStack();
            } else {
                Toast.makeText(requireContext(), "Greška pri dodavanju zadatka!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendTaskBackToHomePage(Task task) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("task", task);
        getParentFragmentManager().setFragmentResult("taskAdded", bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}