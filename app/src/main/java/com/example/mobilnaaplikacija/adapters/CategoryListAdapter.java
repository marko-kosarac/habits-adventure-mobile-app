package com.example.mobilnaaplikacija.adapters;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.RecyclerViewInterface;
import com.example.mobilnaaplikacija.model.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryListAdapter extends RecyclerView.Adapter<CategoryListAdapter.CategoriesViewHolder> {
    private ArrayList<Category> categories;
    private final RecyclerViewInterface recyclerViewInterface;

    public CategoryListAdapter(ArrayList<Category> categories, RecyclerViewInterface recyclerViewInterface) {
        this.categories = categories;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @NonNull
    @Override
    public CategoriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_category, parent, false);
        return new CategoriesViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoriesViewHolder holder, int position) {
        Category category = categories.get(position);
        int color = category.getColor();
        holder.name.setText(category.getName());
        holder.pickedColorButton.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void updateCategories(List<Category> newCategories) {
        this.categories.clear();
        this.categories.addAll(newCategories);
        notifyDataSetChanged();
    }

    public Category getCategoryAt(int position) {
        return categories.get(position);
    }
    public static class CategoriesViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        Button pickedColorButton;
        ImageButton editButton;
        public CategoriesViewHolder(@NonNull View view, RecyclerViewInterface recyclerViewInterface){
            super(view);
            name = view.findViewById(R.id.tvCategoryName);
            pickedColorButton = view.findViewById(R.id.btnPickedColor);
            editButton = view.findViewById(R.id.btnEditCategory);

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
