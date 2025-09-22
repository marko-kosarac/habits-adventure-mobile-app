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
import android.widget.Toast;

import com.applandeo.materialcalendarview.CalendarView;
import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.RecyclerViewInterface;
import com.example.mobilnaaplikacija.adapters.TaskListAdapter;
import com.example.mobilnaaplikacija.databinding.FragmentTaskListBinding;
import com.example.mobilnaaplikacija.model.Category;
import com.example.mobilnaaplikacija.model.FrequencyType;
import com.example.mobilnaaplikacija.model.Task;
import com.example.mobilnaaplikacija.services.CategoryService;
import com.example.mobilnaaplikacija.services.TaskService;
import com.example.mobilnaaplikacija.services.UserService;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseUser;

import org.checkerframework.checker.units.qual.C;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class TaskListFragment extends Fragment implements RecyclerViewInterface {
    private TaskListAdapter adapter;
    private ArrayList<Task> tasks;
    private ArrayList<Category> categories;
    private HashMap<String, Category> categoryMap;
    private String selectedDate = null;
    private Integer selectedFreq = R.id.chipAll;
    private FragmentTaskListBinding binding;
    private TabLayout.Tab listTab, calendarTab;
    private TaskService taskService;
    private UserService userService;
    private CategoryService categoryService;

    public TaskListFragment(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        this.taskService = new TaskService(getContext());
        this.userService = new UserService();
        this.categoryService = new CategoryService(getContext());
        this.tasks = new ArrayList<>();
        this.categories = new ArrayList<>();
        this.categoryMap = new HashMap<>();
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

        //Kategorije
        getCategories();

        //Tabovi lista i kalendar
        TabLayout tabLayout = binding.tabLayout;

        listTab = tabLayout.newTab();
        View listView = LayoutInflater.from(getContext()).inflate(R.layout.tab, null);
        ImageView tabListImage = listView.findViewById(R.id.ivTabIcon);
        tabListImage.setImageResource(R.drawable.ic_list);
        TextView tabListText = listView.findViewById(R.id.tvTabTitle);
        tabListText.setText(R.string.list_view);
        listTab.setCustomView(listView);
        tabLayout.addTab(listTab);

        calendarTab = tabLayout.newTab();
        View calendarView = LayoutInflater.from(getContext()).inflate(R.layout.tab, null);
        ImageView tabCalendarImage = calendarView.findViewById(R.id.ivTabIcon);
        tabCalendarImage.setImageResource(R.drawable.ic_calendar);
        TextView tabCalendarText = calendarView.findViewById(R.id.tvTabTitle);
        tabCalendarText.setText(R.string.calendar_view);
        calendarTab.setCustomView(calendarView);
        tabLayout.addTab(calendarTab);

        //Mijenjanje lista-kalendar
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab == listTab) {
                    adapter.setTasks(applyFilters());
                    binding.rvTasks.setVisibility(View.VISIBLE);
                    binding.calendarView.setVisibility(View.GONE);
                    binding.rvDateTasks.setVisibility(View.GONE);
                } else {
                    binding.rvTasks.setVisibility(View.GONE);
                    binding.calendarView.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        CalendarView calendar = view.findViewById(R.id.calendarView);
        calendar.setOnCalendarDayClickListener(day -> {
            Calendar clickedDay = day.getCalendar();
            SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
            selectedDate = dateFormat.format(clickedDay.getTime());

            binding.rvDateTasks.setLayoutManager(new LinearLayoutManager(getActivity()));
            binding.rvDateTasks.setAdapter(adapter);
            adapter.setTasks(applyFilters());
            binding.rvDateTasks.setVisibility(View.VISIBLE);
            Toast.makeText(getContext(), "Tasks for " + selectedDate, Toast.LENGTH_SHORT).show();
        });

        binding.cgFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            selectedFreq = checkedIds.get(0);
            adapter.setTasks(applyFilters());
        });

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

    public void getCategories(){
        categories = new ArrayList<>(categoryService.getCategories());
        for (Category c : categories) {
            categoryMap.put(c.getId(), c);
        }
    }

    private ArrayList<Task> applyFilters () {
        ArrayList<Task> currentTasks = new ArrayList<>(taskService.getTasksByUser(userService.getCurrentUser().getUid()));
        if (selectedDate != null && binding.tabLayout.getSelectedTabPosition() == calendarTab.getPosition()) {
            currentTasks = taskService.filterByDate(currentTasks, selectedDate);
        }
        currentTasks = filterByFrequency(Collections.singletonList(selectedFreq), currentTasks);
        return currentTasks;
    }

    private ArrayList<Task> filterByFrequency(List<Integer> checkedIds, ArrayList<Task> tasks) {
        ArrayList<Task> filteredTasks = new ArrayList<>();
        if (checkedIds.get(0) == R.id.chipAll) {
            filteredTasks = taskService.filterByFrequency(tasks, null);
        } else if (checkedIds.get(0) == R.id.chipOneTime) {
            filteredTasks = taskService.filterByFrequency(tasks, FrequencyType.JEDNOKRATAN);
        } else if (checkedIds.get(0) == R.id.chipRepeat) {
            filteredTasks = taskService.filterByFrequency(tasks, FrequencyType.PONAVLJAJUCI);
        }
        return filteredTasks;
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