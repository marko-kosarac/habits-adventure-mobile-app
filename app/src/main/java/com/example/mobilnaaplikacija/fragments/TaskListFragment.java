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
import com.example.mobilnaaplikacija.services.UserService;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class TaskListFragment extends Fragment implements RecyclerViewInterface {
    private TaskListAdapter adapter;
    private ArrayList<Task> tasks;
    private FragmentTaskListBinding binding;
    private TaskService taskService;
    private UserService userService;

    public TaskListFragment(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        this.taskService = new TaskService(getContext());
        this.userService = new UserService();
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

        binding.btnAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AddTaskFragment().show(
                        getChildFragmentManager(), "New task");
            }
        });

        getChildFragmentManager().setFragmentResultListener("Task managed", getViewLifecycleOwner(), (requestKey, result) -> {
            getTasks();
        });
    }

    public void getTasks(){
        String userId = "";
        FirebaseUser user = userService.getCurrentUser();
        if(user != null){
            userId = user.getUid();
        }

        tasks.clear();
        tasks.addAll(taskService.getTasksByUser(userId));
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(int position) {
        Bundle args = new Bundle();
        Task selectedTask = tasks.get(position);
        args.putParcelable("Task to view", selectedTask);
        DetailTaskFragment fragment = new DetailTaskFragment();
        fragment.setArguments(args);
        fragment.show(getChildFragmentManager(), "View task");
    }

    @Override
    public void onEditClick(int position) {
        Bundle args = new Bundle();
        Task selectedTask = tasks.get(position);
        args.putParcelable("Task to edit", selectedTask);
        AddTaskFragment fragment = new AddTaskFragment();
        fragment.setArguments(args);
        fragment.show(getChildFragmentManager(), "Edit task");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}