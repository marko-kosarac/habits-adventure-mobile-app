package com.example.mobilnaaplikacija.fragments;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.applandeo.materialcalendarview.CalendarDay;
import com.applandeo.materialcalendarview.CalendarView;
import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.RecyclerViewInterface;
import com.example.mobilnaaplikacija.adapters.TaskListAdapter;
import com.example.mobilnaaplikacija.databinding.FragmentTaskListBinding;
import com.example.mobilnaaplikacija.decorators.MultiDotDrawable;
import com.example.mobilnaaplikacija.model.Category;
import com.example.mobilnaaplikacija.model.FrequencyType;
import com.example.mobilnaaplikacija.model.Task;
import com.example.mobilnaaplikacija.model.UnitType;
import com.example.mobilnaaplikacija.services.CategoryService;
import com.example.mobilnaaplikacija.services.TaskService;
import com.example.mobilnaaplikacija.services.UserService;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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

        //Inicijalizacija zadataka
        binding.rvTasks.setLayoutManager(new LinearLayoutManager(getActivity()));
        tasks = applyFilters();
        adapter = new TaskListAdapter(tasks, this);
        adapter.notifyDataSetChanged();
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
                    selectedDate = null;
                    adapter.updateTasks(applyFilters());
                    binding.rvTasks.setVisibility(View.VISIBLE);
                    binding.calendarView.setVisibility(View.GONE);
                    binding.rvDateTasks.setVisibility(View.GONE);
                } else {
                    decorateCalendarWithTasks();
                    binding.rvTasks.setVisibility(View.GONE);
                    binding.calendarView.setVisibility(View.VISIBLE);
                    binding.rvDateTasks.setVisibility(View.VISIBLE);
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
            adapter.updateTasks(applyFilters());
            binding.rvDateTasks.setVisibility(View.VISIBLE);
            selectedDate = null;
        });

        binding.cgFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            selectedFreq = checkedIds.get(0);
            getTasks();
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
        List<Task> filtered = applyFilters();

        List<Task> updated = new ArrayList<>();
        for (Task t : filtered) {
            Task updatedTask = taskService.autoUpdateStatus(t);
            if (!updatedTask.getStatus().equals(t.getStatus())) {
                taskService.update(updatedTask);
            }
            updated.add(updatedTask);
        }

        adapter.updateTasks(filtered);
        Log.d("RecyclerDebug", "Adapter items after setTasks: " + adapter.getItemCount()); // 👈 add here
        decorateCalendarWithTasks();
    }

    public void getCategories(){
        categories = new ArrayList<>(categoryService.getCategories());
        for (Category c : categories) {
            categoryMap.put(c.getId(), c);
        }
    }

    private ArrayList<Task> applyFilters () {
        Log.d("CalendarDebug", "Before filters: " + tasks.size());
        FirebaseUser user = userService.getCurrentUser();
        if(user == null) {
            Log.d("CalendarDebug", "No user logged in");
            return tasks;
        }
        ArrayList<Task> currentTasks = new ArrayList<>(taskService.getTasksByUser(user.getUid()));

        if (listTab != null && listTab.isSelected())
            currentTasks = taskService.filterCurrentFutureTasks(currentTasks);

        Log.d("CalendarDebug", "Tasks fetched from DB: " + currentTasks.size());
        if (selectedDate != null && binding.tabLayout.getSelectedTabPosition() == calendarTab.getPosition()) {
            currentTasks = filterByDate(currentTasks, selectedDate);
            Log.d("CalendarDebug", "After date filter: " + currentTasks.size() + " (selectedDate=" + selectedDate + ")");
        }

        currentTasks = filterByFrequency(Collections.singletonList(selectedFreq), currentTasks);
        Log.d("CalendarDebug", "After freq filter: " + currentTasks.size() + " (chip=" + selectedFreq + ")");
        return currentTasks;
    }

    private ArrayList<Task> filterByDate(ArrayList<Task> tasks, String selectedDate) {
        ArrayList<Task> filtered = new ArrayList<>();
        for (Task task : tasks) {
            List<String> allDates = getTaskOcurringDates(task);
            if (allDates.contains(selectedDate)) {
                filtered.add(task);
            }
        }
        return filtered;
    }

    private ArrayList<Task> filterByFrequency(List<Integer> checkedIds, ArrayList<Task> tasks) {
        ArrayList<Task> filteredTasks = new ArrayList<>();
        if (checkedIds.isEmpty())
            return tasks;
        if (checkedIds.get(0) == R.id.chipAll) {
            filteredTasks = taskService.filterByFrequency(tasks, null);
        } else if (checkedIds.get(0) == R.id.chipOneTime) {
            filteredTasks = taskService.filterByFrequency(tasks, FrequencyType.JEDNOKRATAN);
        } else if (checkedIds.get(0) == R.id.chipRepeat) {
            filteredTasks = taskService.filterByFrequency(tasks, FrequencyType.PONAVLJAJUCI);
        }
        return filteredTasks;
    }

    private List<String> getTaskOcurringDates(Task task) {
        List<String> dates = new ArrayList<>();
        SimpleDateFormat fmt = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
        fmt.setLenient(false);
        Calendar cal = Calendar.getInstance();

        if(task.getFrequency() == FrequencyType.JEDNOKRATAN) {
            try {
                Date date = parseMillisToDate(task.getStartMillis());
                if(date != null) {
                    cal.setTime(date);
                    dates.add(fmt.format(cal.getTime()));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                Date startDate = parseMillisToDate(task.getStartMillis());
                Date endDate = parseMillisToDate(task.getEndMillis());
                if (startDate == null || endDate == null)
                    return dates;

                cal.setTime(startDate);
                while (!cal.getTime().after(endDate)) {
                    dates.add(fmt.format(cal.getTime()));

                    if (task.getUnit() == UnitType.DAN) {
                        cal.add(Calendar.DAY_OF_MONTH, task.getInterval());
                    } else if (task.getUnit() == UnitType.SEDMICA) {
                        cal.add(Calendar.WEEK_OF_YEAR, task.getInterval());
                    } else if (task.getUnit() == UnitType.MJESEC) {
                        cal.add(Calendar.MONTH, task.getInterval());
                    } else {
                        cal.add(Calendar.YEAR, task.getInterval());
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return dates;
    }

    private void decorateCalendarWithTasks() {
        List<CalendarDay> calendarDays = new ArrayList<>();
        ArrayList<Task> tasks = applyFilters();

        //datum -> lista zadataka
        Map<String, List<Task>> tasksPerDate = new HashMap<>();

        for (Task task : tasks) {
            Log.d("CalendarDebug", "Task: " + task.getName() + " | " + parseMillisToDate(task.getStartMillis()) + " -> " + parseMillisToDate(task.getEndMillis()));
            List<String> allDates = getTaskOcurringDates(task);

            for (String date : allDates) {
                if (!tasksPerDate.containsKey(date)) {
                    tasksPerDate.put(date, new ArrayList<>());
                }
                tasksPerDate.get(date).add(task);
            }
        }

        //grupisano po datumu
        SimpleDateFormat fmt = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());

        for (Map.Entry<String, List<Task>> entry : tasksPerDate.entrySet()) {
            try {
                Date parsed = fmt.parse(entry.getKey());
                if (parsed == null) continue;

                Calendar cal = Calendar.getInstance();
                cal.setTime(parsed);

                CalendarDay day = new CalendarDay(cal);

                //boje kategorija zadataka tog dana
                List<Integer> colors = new ArrayList<>();
                for (Task t : entry.getValue()) {
                    colors.add(getCategoryColorInt()); // TODO: povuci boju iz kategorije
                }

                MultiDotDrawable drawable = new MultiDotDrawable(colors);
                day.setImageDrawable(drawable);

                calendarDays.add(day);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        binding.calendarView.setCalendarDays(calendarDays);
    }

    private Date parseMillisToDate(Long millis) {
        if (millis != null) {
            return new Date(millis);
        }
        return null;
    }

    public int getCategoryColorInt() {
        return Color.BLUE; // TODO: fetch category color dynamically
    }

    @Override
    public void onItemClick(int position) {
        Bundle args = new Bundle();
        Task selectedTask = adapter.getTaskAt(position);
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