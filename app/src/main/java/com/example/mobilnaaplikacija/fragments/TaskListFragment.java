package com.example.mobilnaaplikacija.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mobilnaaplikacija.RecyclerViewInterface;
import com.example.mobilnaaplikacija.adapters.TaskListAdapter;
import com.example.mobilnaaplikacija.databinding.FragmentTaskListBinding;
import com.example.mobilnaaplikacija.model.Task;
import com.example.mobilnaaplikacija.services.TaskService;

import java.util.ArrayList;

public class TaskListFragment extends Fragment implements RecyclerViewInterface {
    private TaskListAdapter adapter;
    private ArrayList<Task> tasks;
    private FragmentTaskListBinding binding;
    private TaskService taskService;

    public TaskListFragment(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        Log.i("MoblinaAplikacija", "onCreateView Task List Fragment");
        this.taskService = new TaskService(getContext());
        this.tasks = new ArrayList<>();
        binding = FragmentTaskListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.rvTasks.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new TaskListAdapter(tasks, this);
        binding.rvTasks.setAdapter(adapter);
        getTasks();

        getParentFragmentManager().setFragmentResultListener("Task added", this, ((requestKey, result) -> {
            getTasks();
        }));
    }

    public void getTasks(){
        tasks.clear();
        tasks.addAll(taskService.getTasksById(1L)); // TODO : get user id
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(int position) {}

    @Override
    public void onEditClick(int position) {
        Task selectedTask = tasks.get(position);
        new AddTaskFragment().show(getChildFragmentManager(), "Edit task");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}