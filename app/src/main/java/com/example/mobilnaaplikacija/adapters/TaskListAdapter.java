package com.example.mobilnaaplikacija.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.model.Task;

import java.util.ArrayList;

public class TaskListAdapter extends ArrayAdapter<Task> {
    private ArrayList<Task> aTasks;

    public TaskListAdapter(Context context, ArrayList<Task> tasks){
        super(context, R.layout.task_card, tasks);
        aTasks = tasks;

    }

    @Override
    public int getCount() {
        return aTasks.size();
    }

    @Nullable
    @Override
    public Task getItem(int position) {
        return aTasks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Task task = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.task_card,
                    parent, false);
        }
        CardView taskCard = convertView.findViewById(R.id.task_card_item);
        TextView taskTitle = convertView.findViewById(R.id.tvTaskName);
        TextView taskCategory = convertView.findViewById(R.id.tvTaskCategory);
        //je l ok check box
        CheckBox taskIsDone = convertView.findViewById(R.id.cbIsTaskDone);

        if(task != null){
            taskTitle.setText(task.getName());
            taskCategory.setText(task.getCategory());
            taskIsDone.setChecked(task.getStatus().contentEquals("DONE"));
            taskCard.setOnClickListener(v -> {
                Log.i("MoblilnaAplikacija", "Clicked: " + task.getName() + ", id: "
                        + task.getId().toString());
                Toast.makeText(getContext(), "Clicked: " + task.getName() + ", id: " + task.getId().toString(), Toast.LENGTH_SHORT).show();
            });
        }

        return convertView;
    }
}
