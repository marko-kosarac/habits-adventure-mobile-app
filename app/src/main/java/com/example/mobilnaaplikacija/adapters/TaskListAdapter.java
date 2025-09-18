package com.example.mobilnaaplikacija.adapters;

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
import com.example.mobilnaaplikacija.model.StatusType;
import com.example.mobilnaaplikacija.model.Task;

import java.util.ArrayList;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TasksViewHolder> {
    private ArrayList<Task> tasks;
    private final RecyclerViewInterface recyclerViewInterface;

    public TaskListAdapter(ArrayList<Task> tasks, RecyclerViewInterface recyclerViewInterface) {
        this.tasks = tasks;
        this.recyclerViewInterface = recyclerViewInterface;
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
        holder.category.setText(task.getName()); // TODO : retrieve category by id
        holder.isDone.setChecked(task.getStatus() == StatusType.URAĐEN);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void setTasks(ArrayList<Task> newTasks) {
        this.tasks = new ArrayList<>(newTasks);
        notifyDataSetChanged();
    }

    public static class TasksViewHolder extends  RecyclerView.ViewHolder{
        TextView name, category;
        CheckBox isDone;
        ImageButton editButton;

        public TasksViewHolder(@NonNull View view, RecyclerViewInterface recyclerViewInterface){
            super(view);
            name = view.findViewById(R.id.tvTaskName);
            category = view.findViewById(R.id.tvTaskCategory);
            isDone = view.findViewById(R.id.cbIsTaskDone);
            editButton = view.findViewById(R.id.btnEditTask);

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
