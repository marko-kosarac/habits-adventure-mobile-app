package com.example.mobilnaaplikacija.adapters;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.RecyclerViewInterface;
import com.example.mobilnaaplikacija.model.Category;
import com.example.mobilnaaplikacija.model.StatusType;
import com.example.mobilnaaplikacija.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TasksViewHolder> {
    private ArrayList<Task> tasks;
    private HashMap<String, Category> categoryMap;
    private final RecyclerViewInterface recyclerViewInterface;

    public TaskListAdapter(ArrayList<Task> tasks, RecyclerViewInterface recyclerViewInterface, HashMap<String, Category> categoryMap) {
        this.tasks = tasks;
        this.recyclerViewInterface = recyclerViewInterface;
        this.categoryMap = categoryMap;
    }

    @NonNull
    @Override
    public TasksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_task, parent, false);
        return new TasksViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull TasksViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.name.setText(task.getName());
        Category category = categoryMap.get(task.getCategoryId());
        String categoryName = (category == null) ? "Nema kategoriju" : category.getName();
        holder.category.setText(categoryName);
        holder.cardColor.setBackgroundColor(category.getColor());
        holder.isDone.setChecked(task.getStatus() == StatusType.URAĐEN);

        if (task.getStatus() == StatusType.OTKAZAN || task.getStatus() == StatusType.NEURAĐEN) {
            holder.editButton.setVisibility(View.INVISIBLE);
        } else {
            holder.editButton.setVisibility(View.VISIBLE);
        }
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

    public static class TasksViewHolder extends  RecyclerView.ViewHolder{
        TextView name, category;
        CheckBox isDone;
        ImageButton editButton;
        View cardColor;

        public TasksViewHolder(@NonNull View view, RecyclerViewInterface recyclerViewInterface){
            super(view);
            name = view.findViewById(R.id.tvTaskName);
            category = view.findViewById(R.id.tvTaskCategory);
            isDone = view.findViewById(R.id.cbIsTaskDone);
            editButton = view.findViewById(R.id.btnEditTask);
            cardColor = view.findViewById(R.id.vCardColor);

            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && recyclerViewInterface != null) {
                        recyclerViewInterface.onEditClick(position);
                    }
                }
            });

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(recyclerViewInterface != null){
                        int pos = getAdapterPosition();
                        if(pos != RecyclerView.NO_POSITION){
                            recyclerViewInterface.onItemClick(pos);
                        }
                    }
                }
            });
        }
    }
}
