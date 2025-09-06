package com.example.mobilnaaplikacija.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.model.Equipment;

import java.util.ArrayList;

public class EquipmentListAdapter extends RecyclerView.Adapter<EquipmentListAdapter.EquipmentViewHolder> {

    private ArrayList<Equipment> equipmentList;
    private Context context;

    public EquipmentListAdapter(Context context, ArrayList<Equipment> equipmentList) {
        this.context = context;
        this.equipmentList = equipmentList;
    }

    @NonNull
    @Override
    public EquipmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_equipment, parent, false);
        return new EquipmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EquipmentViewHolder holder, int position) {
        Equipment equipment = equipmentList.get(position);

        holder.textName.setText(equipment.getName());
        holder.textType.setText("Tip: " + equipment.getType());
        holder.textBonus.setText("Bonus: " + equipment.getBonus());
        holder.textDescription.setText(equipment.getDescription());

        holder.buttonActivate.setOnClickListener(v -> {
            // ovde dodaj logiku za aktivaciju opreme
            Toast.makeText(context, equipment.getName() + " aktivirana!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return equipmentList.size();
    }

    public static class EquipmentViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textType, textBonus, textDescription;
        Button buttonActivate;

        public EquipmentViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.eq_name);
            textType = itemView.findViewById(R.id.eq_type);
            textBonus = itemView.findViewById(R.id.eq_bonus);
            textDescription = itemView.findViewById(R.id.eq_description);
            buttonActivate = itemView.findViewById(R.id.btn_activate);
        }
    }
}
