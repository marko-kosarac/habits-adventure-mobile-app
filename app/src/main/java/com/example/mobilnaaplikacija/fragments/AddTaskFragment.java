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

import java.util.Calendar;
import java.util.Locale;

public class AddTaskFragment extends Fragment {

    private FragmentAddTaskBinding binding;

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
            } else {
                binding.layoutRecurringOptions.setVisibility(View.GONE);
            }
        });
    }

    private void setupAddTaskButton(){
        binding.btnAddTask.setOnClickListener(view -> {
            String name = binding.etTaskName.getText().toString().trim();
            String description = binding.etTaskDescription.getText().toString().trim();
            String category = binding.spinnerCategory.getSelectedItem().toString();
            Boolean isRepeating = binding.rbRepeat.isChecked();
            Boolean isOneTime = binding.rbOneTime.isChecked();
            FrequencyType frequency = null;
            if(isRepeating){
                frequency = FrequencyType.PONAVLJAJUCI;
            } else if(isOneTime){
                frequency = FrequencyType.JEDNOKRATAN;
            }
            String startDate = binding.etStartDate.getText().toString().trim();
            String endDate = binding.etEndDate.getText().toString().trim();
            String time = binding.etTime.getText().toString().trim();
            Boolean isWholeDay = binding.rbWholeDay.isChecked();
            Integer interval = null;
            UnitType unit = null;
            DifficultyType difficulty = null;
            String difficultyStr = binding.spinnerDifficulty.getSelectedItem().toString().toUpperCase(Locale.ROOT);
            difficulty = DifficultyType.valueOf(difficultyStr);
            ImportanceType importance = null;
            String importanceStr = binding.spinnerImportance.getSelectedItem().toString().toUpperCase(Locale.ROOT);
            importance = ImportanceType.valueOf(importanceStr);
            StatusType status = StatusType.AKTIVAN;

            //Validation
            if(name.isEmpty()) {
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

            if ((isRepeating == false && isOneTime == false)
                    || (isRepeating == null && isOneTime == null)) {
                Toast.makeText(requireContext(), "Izaberite jednokratan ili ponavljajući zadatak!", Toast.LENGTH_SHORT).show();
                return;
            }

            if(difficultyStr.isEmpty()) {
                Toast.makeText(requireContext(), "Izaberite težinu zadatka!", Toast.LENGTH_SHORT).show();
                return;
            }

            if(importanceStr.isEmpty()) {
                Toast.makeText(requireContext(), "Izaberite bitnost zadatka!", Toast.LENGTH_SHORT).show();
                return;
            }

            if(frequency.equals(FrequencyType.PONAVLJAJUCI)){
                String unitStr = binding.spinnerReccuringUnit.getSelectedItem().toString().toUpperCase(Locale.ROOT);
                unit = UnitType.valueOf(unitStr);
                interval = Integer.valueOf(binding.etReccuringNumber.getText().toString());

                if(unitStr.isEmpty()) {
                    Toast.makeText(requireContext(), "Unesite jedinicu zadatka!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if((interval.toString()).isEmpty()) {
                    Toast.makeText(requireContext(), "Unesite broj ponavljanja zadatka!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            Task task = new Task(0L, name, description, category, frequency, startDate, endDate, time, isWholeDay, interval, unit, difficulty, importance, status);
            Bundle bundle = new Bundle();
            bundle.putParcelable("task", task);

            getParentFragmentManager().setFragmentResult("taskAdded", bundle);

            Toast.makeText(requireContext(), "Zadatak dodan!", Toast.LENGTH_SHORT).show();

            getParentFragmentManager().popBackStack();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}