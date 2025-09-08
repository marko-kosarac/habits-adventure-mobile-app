package com.example.mobilnaaplikacija.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mobilnaaplikacija.adapters.TaskListAdapter;
import com.example.mobilnaaplikacija.databinding.FragmentTaskListBinding;
import com.example.mobilnaaplikacija.model.Task;
import java.util.ArrayList;

public class TaskListFragment extends ListFragment {

    private TaskListAdapter adapter;
    private static final String ARG_PARAM = "param";
    private ArrayList<Task> mTasks;
    private FragmentTaskListBinding binding;

    public static TaskListFragment newInstance(ArrayList<Task> tasks){
        TaskListFragment fragment = new TaskListFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_PARAM, tasks);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.i("MoblinaAplikacija", "onCreate Task List Fragment");
        if(getArguments() != null) {
            mTasks = getArguments().getParcelableArrayList(ARG_PARAM);
            adapter = new TaskListAdapter(getActivity(), mTasks);
            setListAdapter(adapter);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        Log.i("MoblinaAplikacija", "onCreateView Task List Fragment");
        binding = FragmentTaskListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}