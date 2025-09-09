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

import java.util.ArrayList;

public class HomePageFragment extends Fragment {

    public static ArrayList<Task> tasks = new ArrayList<Task>();
    private FragmentHomePageBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        Log.i("MoblinaAplikacija", "onCreateView Home Page Fragment");
        binding = FragmentHomePageBinding.inflate(inflater, container, false);

        prepareTaskList(tasks);

        TaskListAdapter adapter = new TaskListAdapter(tasks);
        binding.rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTasks.setAdapter(adapter);

        getParentFragmentManager().setFragmentResultListener("taskAdded", this, (request, bundle) -> {
            Task newTask = bundle.getParcelable("task");
            if(newTask != null) {
                tasks.add(newTask);
                adapter.notifyItemInserted(tasks.size() - 1);
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        binding.btnAddTask.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_homePageFragment_to_addTaskFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void prepareTaskList(ArrayList<Task> tasks){
        tasks.clear();
        tasks.add(new Task(0L, "Anin rođendan", "Kućna žurka", 0L, FrequencyType.JEDNOKRATAN, "26/09/2025", "26/09/2025", "20:00", false, 1, UnitType.DAN, DifficultyType.LAK, ImportanceType.VAŽAN, StatusType.AKTIVAN));
        tasks.add(new Task(1L, "Stomatolog", "Popravka zuba", 0L, FrequencyType.JEDNOKRATAN, "01/10/2025", "01/10/2025", "13:00", false, 1, UnitType.DAN, DifficultyType.TEŽAK, ImportanceType.VAŽAN, StatusType.AKTIVAN));
        tasks.add(new Task(2L, "Poslovni sastanak", "Weekly", 0L, FrequencyType.PONAVLJAJUCI, "26/09/2025", "26/09/2026", "15:00", false, 1, UnitType.SEDMICA, DifficultyType.TEŽAK, ImportanceType.EKSTREMNO_VAŽAN, StatusType.AKTIVAN));
    }

}