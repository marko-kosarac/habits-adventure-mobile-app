package com.example.mobilnaaplikacija.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.model.Equipment;

import java.util.ArrayList;
import java.util.List;

public class EquipmentSelectionAdapter extends RecyclerView.Adapter<EquipmentSelectionAdapter.ViewHolder> {

    private Context context;
    private List<Equipment> equipmentList;
    private List<Equipment> selectedEquipment = new ArrayList<>();
    private OnActivateListener onActivateListener;

    public interface OnActivateListener {
        void onActivate(Equipment equipment);
    }

    public void setOnActivateListener(OnActivateListener listener) {
        this.onActivateListener = listener;
    }

    public EquipmentSelectionAdapter(Context context, List<Equipment> equipmentList) {
        this.context = context;
        this.equipmentList = equipmentList;
    }

    public List<Equipment> getSelectedEquipment() {
        return selectedEquipment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_user_equipment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Equipment eq = equipmentList.get(position);
        holder.textName.setText(eq.getName());
        holder.textBonus.setText(eq.getBonus());
        holder.textDescription.setText(eq.getDescription());
        holder.textQuantity.setText("Količina: " + eq.getQuantity());

        boolean isSelected = selectedEquipment.contains(eq);
        holder.cardView.setCardBackgroundColor(isSelected ? Color.parseColor("#CCE5FF") : Color.WHITE);
        holder.btnActivate.setText(isSelected ? "Poništi" : "Aktiviraj");

        holder.btnActivate.setOnClickListener(v -> {
            if (selectedEquipment.contains(eq)) {
                selectedEquipment.remove(eq);
                holder.cardView.setCardBackgroundColor(Color.WHITE);
                holder.btnActivate.setText("Aktiviraj");
            } else {
                selectedEquipment.add(eq);
                holder.cardView.setCardBackgroundColor(Color.parseColor("#CCE5FF"));
                holder.btnActivate.setText("Poništi");
            }
            if (onActivateListener != null) {
                onActivateListener.onActivate(eq);
            }
        });
    }

    @Override
    public int getItemCount() {
        return equipmentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textBonus, textDescription, textQuantity;
        Button btnActivate;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.equipment_card);
            textName = itemView.findViewById(R.id.textEquipmentName);
            textBonus = itemView.findViewById(R.id.textEquipmentBonus);
            textDescription = itemView.findViewById(R.id.textEquipmentDescription);
            textQuantity = itemView.findViewById(R.id.textEquipmentQuantity);
            btnActivate = itemView.findViewById(R.id.buttonActivateEquipment);
        }
    }
}
