package com.example.mobilnaaplikacija.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.model.StatusType;
import com.example.mobilnaaplikacija.model.Task;

import java.util.ArrayList;
import java.util.Locale;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TaskViewHolder> {
    private ArrayList<Task> tasks;

    public TaskListAdapter(ArrayList<Task> tasks) {
        this.tasks = tasks;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public static class TaskViewHolder extends  RecyclerView.ViewHolder{
        CardView taskCard;
        TextView taskName, taskCategory;
        CheckBox taskIsDone;
        public TaskViewHolder(@NonNull View view){
            super(view);
            taskCard = view.findViewById(R.id.task_card_item);
            taskName = view.findViewById(R.id.tvTaskName);
            taskCategory = view.findViewById(R.id.tvTaskCategory);
            taskIsDone = view.findViewById(R.id.cbIsTaskDone);
        }
    }
}
