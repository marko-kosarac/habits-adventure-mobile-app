package com.example.mobilnaaplikacija.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.RecyclerViewInterface;
import com.example.mobilnaaplikacija.adapters.TaskListAdapter;
import com.example.mobilnaaplikacija.databinding.FragmentTaskListBinding;
import com.example.mobilnaaplikacija.model.FrequencyType;
import com.example.mobilnaaplikacija.model.Task;
import com.example.mobilnaaplikacija.services.TaskService;
import com.example.mobilnaaplikacija.services.UserService;
import com.google.android.material.tabs.TabLayout;
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

        TabLayout tabLayout = binding.tabLayout;

        //Tab lista
        TabLayout.Tab listTab = tabLayout.newTab();
        View listView = LayoutInflater.from(getContext()).inflate(R.layout.tab, null);
        ImageView tabListImage = listView.findViewById(R.id.ivTabIcon);
        tabListImage.setImageResource(R.drawable.ic_list);
        TextView tabListText = listView.findViewById(R.id.tvTabTitle);
        tabListText.setText(R.string.list_view);
        listTab.setCustomView(listView);
        tabLayout.addTab(listTab);

        //Tab kalendar
        TabLayout.Tab calendarTab = tabLayout.newTab();
        View calendarView = LayoutInflater.from(getContext()).inflate(R.layout.tab, null);
        ImageView tabCalendarImage = calendarView.findViewById(R.id.ivTabIcon);
        tabCalendarImage.setImageResource(R.drawable.ic_calendar);
        TextView tabCalendarText = calendarView.findViewById(R.id.tvTabTitle);
        tabCalendarText.setText(R.string.calendar_view);
        calendarTab.setCustomView(calendarView);
        tabLayout.addTab(calendarTab);

        binding.btnAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AddEditTaskFragment().show(
                        getChildFragmentManager(), "New task");
            }
        });

        getChildFragmentManager().setFragmentResultListener("Task managed", getViewLifecycleOwner(), (requestKey, result) -> {
            getTasks();
        });

        binding.cgFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            ArrayList<Task> filteredTasks = new ArrayList<>();

            if (checkedIds.get(0) == R.id.chipAll) {
               filteredTasks = taskService.filterByFrequency(tasks, null);
           } else if (checkedIds.get(0) == R.id.chipOneTime) {
                filteredTasks = taskService.filterByFrequency(tasks, FrequencyType.JEDNOKRATAN);
            } else if (checkedIds.get(0) == R.id.chipRepeat) {
                filteredTasks = taskService.filterByFrequency(tasks, FrequencyType.PONAVLJAJUCI);
            }
            adapter.setTasks(filteredTasks);
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
        adapter.setTasks(tasks);
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
        AddEditTaskFragment fragment = new AddEditTaskFragment();
        fragment.setArguments(args);
        fragment.show(getChildFragmentManager(), "Edit task");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}