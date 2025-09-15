package com.example.mobilnaaplikacija.fragments;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.adapters.TaskListAdapter;
import com.example.mobilnaaplikacija.databinding.FragmentHomePageBinding;
import com.example.mobilnaaplikacija.model.DifficultyType;
import com.example.mobilnaaplikacija.model.FrequencyType;
import com.example.mobilnaaplikacija.model.ImportanceType;
import com.example.mobilnaaplikacija.model.StatusType;
import com.example.mobilnaaplikacija.model.Task;
import com.example.mobilnaaplikacija.model.UnitType;
import com.example.mobilnaaplikacija.services.TaskService;

import java.util.ArrayList;

public class HomePageFragment extends Fragment {

    private FragmentHomePageBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        Log.i("MoblinaAplikacija", "onCreateView Home Page Fragment");
        binding = FragmentHomePageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        binding.btnAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AddTaskFragment().show(
                        getChildFragmentManager(), "New task");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}