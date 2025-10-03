package com.example.mobilnaaplikacija;

import android.view.View;

public interface RecyclerViewInterface {
    void onItemClick(int position);
    void onEditClick(int position);
    void onStatusClick(int position, View anchor);
}
