package com.example.mobilnaaplikacija.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.SpinnerAdapter;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.databinding.FragmentAddTaskBinding;
import com.google.android.material.textfield.TextInputEditText;

import java.sql.Time;
import java.util.Calendar;

public class AddTaskFragment extends Fragment {

    private FragmentAddTaskBinding binding;
    private boolean RECURRING_TASK = false;

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
        setupAddTaskButton();
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
        binding.spinnerReccuringUnit.setAdapter(unitAdapter);

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
                binding.etTime.setEnabled(false);
                binding.etTime.setText("");
                if(!binding.etStartDate.getText().toString().isEmpty()){
                    binding.etEndDate.setText(binding.etStartDate.getText().toString());
                } else if(!binding.etEndDate.getText().toString().isEmpty()){
                    binding.etStartDate.setText(binding.etEndDate.getText().toString());
                }
            } else {
                binding.etTime.setEnabled(true);
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
                (TimePicker view, int hourOfDay, int minute) ->
                field.setText(String.format("%02d:%02d", hourOfDay, minute)),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);
        timePickerDialog.show();
    }

    private void setupFrequency(){
        binding.rgFrequency.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            if(checkedId == R.id.rbRepeat) {
                binding.layoutRecurringOptions.setVisibility(View.VISIBLE);
                RECURRING_TASK = true;
            } else {
                binding.layoutRecurringOptions.setVisibility(View.GONE);
            }
        });
    }

    private void setupAddTaskButton(){
        binding.btnAddTask.setOnClickListener(view -> {
            String taskName = binding.etTaskName.getText().toString().trim();
            String category = binding.spinnerCategory.getSelectedItem().toString();
            String startDate = binding.etStartDate.getText().toString().trim();
            String endDate = binding.etEndDate.getText().toString().trim();
            String time = binding.etTime.getText().toString().trim();
            Boolean isWholeDay = binding.rbWholeDay.isChecked();
            Boolean isRepeating = binding.rbRepeat.isChecked();
            Boolean isOneTime = binding.rbOneTime.isChecked();
            String difficulty = binding.spinnerDifficulty.getSelectedItem().toString();
            String importance = binding.spinnerImportance.getSelectedItem().toString();

            //Validation
            if(taskName.isEmpty()) {
                Toast.makeText(requireContext(), "Unesite naziv zadatka!", Toast.LENGTH_SHORT).show();
                return;
            }

            if(category.isEmpty()) {
                Toast.makeText(requireContext(), "Izaberite kategoriju zadatka!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (startDate.isEmpty()) {
                Toast.makeText(requireContext(), "Izaberite datum početka!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (endDate.isEmpty()) {
                Toast.makeText(requireContext(), "Izaberite datum završetka!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (time.isEmpty() && isWholeDay == false) {
                Toast.makeText(requireContext(), "Unesite vrijeme zadatka!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isRepeating == false && isOneTime == false) {
                Toast.makeText(requireContext(), "Izaberite jednokratan ili ponavljajući zadatak!", Toast.LENGTH_SHORT).show();
                return;
            }

            if(difficulty.isEmpty()) {
                Toast.makeText(requireContext(), "Izaberite težinu zadatka!", Toast.LENGTH_SHORT).show();
                return;
            }

            if(importance.isEmpty()) {
                Toast.makeText(requireContext(), "Izaberite bitnost zadatka!", Toast.LENGTH_SHORT).show();
                return;
            }

            if(RECURRING_TASK){
                String unit = binding.spinnerReccuringUnit.getSelectedItem().toString();
                String interval = binding.etReccuringNumber.getText().toString();

                if(unit.isEmpty()) {
                    Toast.makeText(requireContext(), "Unesite jedinicu zadatka!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(interval.isEmpty()) {
                    Toast.makeText(requireContext(), "Unesite broj ponavljanja zadatka!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            Toast.makeText(requireContext(), "Zadatak dodan!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}