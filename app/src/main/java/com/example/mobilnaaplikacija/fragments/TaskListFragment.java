package com.example.mobilnaaplikacija.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mobilnaaplikacija.adapters.TaskListAdapter;
import com.example.mobilnaaplikacija.databinding.FragmentTaskListBinding;
import com.example.mobilnaaplikacija.model.Task;
import java.util.ArrayList;

public class TaskListFragment extends Fragment {
    private TaskListAdapter adapter;
    private static final String ARG_PARAM = "param";
    private ArrayList<Task> tasks;
    private FragmentTaskListBinding binding;

    public TaskListFragment(){}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.i("MoblinaAplikacija", "onCreate Task List Fragment");
        if(getArguments() != null) {
            tasks = getArguments().getParcelableArrayList(ARG_PARAM);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        Log.i("MoblinaAplikacija", "onCreateView Task List Fragment");
        binding = FragmentTaskListBinding.inflate(inflater, container, false);
        adapter = new TaskListAdapter(tasks);
        binding.rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTasks.setAdapter(adapter);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}