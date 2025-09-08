package com.example.mobilnaaplikacija.fragments;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;
import androidx.navigation.Navigation;

import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.adapters.TaskListAdapter;
import com.example.mobilnaaplikacija.databinding.FragmentHomePageBinding;
import com.example.mobilnaaplikacija.model.Task;

import java.util.ArrayList;

public class HomePageFragment extends Fragment {

    public static ArrayList<Task> tasks = new ArrayList<Task>();
    private FragmentHomePageBinding binding;

    public static HomePageFragment newInstance(){
        return new HomePageFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        Log.i("MoblinaAplikacija", "onCreateView Home Page Fragment");
        binding = FragmentHomePageBinding.inflate(inflater, container, false);

        prepareTaskList(tasks);

        getParentFragmentManager().setFragmentResultListener("taskAdded", this, (request, bundle) -> {
            Task newTask = bundle.getParcelable("task");
            if(newTask != null) {
                tasks.add(newTask);
            }
        });

        TaskListAdapter adapter = new TaskListAdapter(requireContext(), tasks);
        binding.lvTasks.setAdapter(adapter);

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
        tasks.add(new Task(1L, "Anin rođendan", "Kućna žurka", "Zabava", "Jednokratno", "26/09/2025", "26/09/2025", "20:00", false, 1, "Dan", "Lak", "Ekstremno važan", "Neurađen"));
        tasks.add(new Task(2L, "Stomatolog", "Popravka zuba", "Zdravlje", "Jednokratno", "01/10/2025", "01/10/2025", "13:00", false, 1, "Dan", "Težak", "Važan", "Neurađen"));
        tasks.add(new Task(3L, "Poslovni sastanak", "Weekly", "Posao", "Ponavljajuće", "26/09/2025", "26/09/2026", "15:00", false, 1, "Sedmica", "Težak", "Ekstremno važan", "Neurađen"));
    }

}