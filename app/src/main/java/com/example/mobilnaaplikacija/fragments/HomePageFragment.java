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

    public static ArrayList<Task> tasks = new ArrayList<Task>();
    private FragmentHomePageBinding binding;
    private TaskListAdapter adapter;
    private TaskService taskService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        Log.i("MoblinaAplikacija", "onCreateView Home Page Fragment");
        binding = FragmentHomePageBinding.inflate(inflater, container, false);

        adapter = new TaskListAdapter(tasks);
        binding.rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTasks.setAdapter(adapter);

        taskService = new TaskService(requireContext());
        loadTasksFromDb();

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

    private void loadTasksFromDb(){
        tasks.clear();
        tasks.addAll(taskService.getTasksById(1L));
        adapter.notifyDataSetChanged();
    }

}