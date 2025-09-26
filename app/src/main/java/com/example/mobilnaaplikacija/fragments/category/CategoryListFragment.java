package com.example.mobilnaaplikacija.fragments.category;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mobilnaaplikacija.RecyclerViewInterface;
import com.example.mobilnaaplikacija.adapters.CategoryListAdapter;
import com.example.mobilnaaplikacija.databinding.FragmentCategoryListBinding;
import com.example.mobilnaaplikacija.model.Category;
import com.example.mobilnaaplikacija.services.CategoryService;

import java.util.ArrayList;
import java.util.List;

public class CategoryListFragment extends Fragment implements RecyclerViewInterface {
    private FragmentCategoryListBinding binding;
    private CategoryService categoryService;
    private CategoryListAdapter adapter;
    private ArrayList<Category> categories;

    public CategoryListFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCategoryListBinding.inflate(inflater, container,false);
        this.categoryService = new CategoryService(getContext());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding.rvCategories.setLayoutManager(new LinearLayoutManager(getActivity()));
        categories = new ArrayList<>(categoryService.getCategories());
        adapter = new CategoryListAdapter(categories, this);
        adapter.notifyDataSetChanged();
        binding.rvCategories.setAdapter(adapter);

        binding.btnAddCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AddCategoryFragment().show(getChildFragmentManager(), "New category");
            }
        });

        getChildFragmentManager().setFragmentResultListener("Category managed", getViewLifecycleOwner(), (requestKey, result) -> {
            getCategories();
        });
    }

    private void getCategories() {
        List<Category> categories = categoryService.getCategories();
        adapter.updateCategories(categories);
    }

    @Override
    public void onItemClick(int position) {}

    @Override
    public void onEditClick(int position) {
        Bundle args = new Bundle();
        Category selectedCategory = categories.get(position);
        args.putParcelable("Category to edit", selectedCategory);
        AddCategoryFragment fragment = new AddCategoryFragment();
        fragment.setArguments(args);
        fragment.show(getChildFragmentManager(), "Edit category");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}