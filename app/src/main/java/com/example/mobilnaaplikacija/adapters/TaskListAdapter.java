package com.example.mobilnaaplikacija.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.RecyclerViewInterface;
import com.example.mobilnaaplikacija.model.Category;
import com.example.mobilnaaplikacija.model.enums.FrequencyType;
import com.example.mobilnaaplikacija.model.enums.StatusType;
import com.example.mobilnaaplikacija.model.Task;
import com.example.mobilnaaplikacija.services.TaskService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TasksViewHolder> {

    private ArrayList<Task> tasks;
    private HashMap<String, Category> categoryMap;
    private final RecyclerViewInterface recyclerViewInterface;
    private TaskService taskService;
    private boolean calendarMode;

    public TaskListAdapter(ArrayList<Task> tasks, RecyclerViewInterface recyclerViewInterface, HashMap<String, Category> categoryMap, TaskService taskService, boolean calendarMode) {
        this.tasks = tasks;
        this.recyclerViewInterface = recyclerViewInterface;
        this.categoryMap = categoryMap;
        this.taskService = taskService;
        this.calendarMode = calendarMode;
    }

    public void setCalendarMode(boolean calendarMode) {
        this.calendarMode = calendarMode;
    }

    @NonNull
    @Override
    public TasksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = calendarMode ? R.layout.card_task_calendar : R.layout.card_task;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new TasksViewHolder(view, recyclerViewInterface, this.taskService);
    }

    @Override
    public void onBindViewHolder(@NonNull TasksViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.bind(task, categoryMap);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void updateTasks(List<Task> newTasks) {
        this.tasks.clear();
        this.tasks.addAll(newTasks);
        notifyDataSetChanged();
    }

    public Task getTaskAt(int position) {
        return tasks.get(position);
    }

    public static class TasksViewHolder extends RecyclerView.ViewHolder {
        TextView name, category, status, experiencePoints;
        ImageButton editButton;
        View cardColor;
        private final TaskService taskService;

        public TasksViewHolder(@NonNull View view, RecyclerViewInterface recyclerViewInterface, TaskService taskService) {
            super(view);
            this.taskService = taskService;

            name = view.findViewById(R.id.tvTaskName);
            category = view.findViewById(R.id.tvTaskCategory);
            status = view.findViewById(R.id.tvTaskStatus);
            editButton = view.findViewById(R.id.btnEditTask);
            cardColor = view.findViewById(R.id.vCardColor);
            experiencePoints = view.findViewById(R.id.tvTaskXP);

            if (editButton != null) {
                editButton.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && recyclerViewInterface != null) {
                        recyclerViewInterface.onEditClick(position);
                    }
                });
            }

            if (status != null) {
                status.setOnClickListener(v -> {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION && recyclerViewInterface != null) {
                        recyclerViewInterface.onStatusClick(pos, v);
                    }
                });
            }

            view.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && recyclerViewInterface != null) {
                    recyclerViewInterface.onItemClick(pos);
                }
            });
        }

        public void bind(Task task, HashMap<String, Category> categoryMap) {
            if (name != null) name.setText(task.getName());

            if (category != null) {
                Category cat = categoryMap.get(task.getCategoryId());
                category.setText(cat == null ? "Nema kategoriju" : cat.getName());
            }

            if (cardColor != null) {
                Category cat = categoryMap.get(task.getCategoryId());
                cardColor.setBackgroundColor(cat == null ? Color.WHITE : cat.getColor());
            }

            if (status != null) {
                status.setText(task.getStatus().getDisplayName());

                switch (task.getStatus()) {
                    case AKTIVAN:
                        status.setBackgroundResource(R.drawable.status_pill_blue);
                        status.setTextColor(Color.WHITE);
                        break;
                    case URAĐEN:
                        status.setBackgroundResource(R.drawable.status_pill_green);
                        status.setTextColor(Color.WHITE);
                        break;
                    case NEURAĐEN:
                        status.setBackgroundResource(R.drawable.status_pill_red);
                        status.setTextColor(Color.WHITE);
                        break;
                    case PAUZIRAN:
                        status.setBackgroundResource(R.drawable.status_pill_yellow);
                        status.setTextColor(Color.WHITE);
                        break;
                    case OTKAZAN:
                        status.setBackgroundResource(R.drawable.status_pill_gray);
                        status.setTextColor(Color.WHITE);
                        break;
                }

                if (task.getStatus() == StatusType.URAĐEN || task.getStatus() == StatusType.NEURAĐEN || task.getStatus() == StatusType.OTKAZAN) {
                    status.setClickable(false);
                } else {
                    status.setClickable(true);
                }
            }

            if (experiencePoints != null) {
                int xp = this.taskService.getXP(task);
                experiencePoints.setText(xp + " XP");
            }
            if (editButton != null) {
                editButton.setVisibility(
                        (task.getStatus() == StatusType.NEURAĐEN || task.getStatus() == StatusType.OTKAZAN || task.getStatus() == StatusType.URAĐEN || (task.getFrequency() == FrequencyType.PONAVLJAJUCI && task.getEndMillis() < System.currentTimeMillis()))
                                ? View.INVISIBLE : View.VISIBLE
                );
            }
        }
    }
}
