package com.example.mobilnaaplikacija.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.model.Equipment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class PrepareBattleAdapter extends RecyclerView.Adapter<PrepareBattleAdapter.ViewHolder> {

    private List<Equipment> equipmentList;
    private OnActivateListener onActivateListener;
    private Set<Long> activatedIds = new HashSet<>();

    public interface OnActivateListener {
        void onActivate(Equipment eq);
    }

    public PrepareBattleAdapter(List<Equipment> equipmentList) {
        this.equipmentList = equipmentList;
    }

    public void setOnActivateListener(OnActivateListener listener) {
        this.onActivateListener = listener;
    }

    public List<Equipment> getUnactivatedEquipment(long id) { //TODO useful or not?
        List<Equipment> res = new ArrayList<>();
        for (Equipment eq : this.equipmentList) {
            if (!eq.isActive() && eq.getId() == id) {
                res.add(eq);
            }
        }
        return res;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_prepare_equipment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Equipment eq = equipmentList.get(position);

        holder.name.setText(eq.getName());
        holder.desc.setText(eq.getDescription());
        holder.qty.setText("Preostalo: " + eq.getQuantity());

        switch (eq.getType()) {
            case NAPITAK: holder.icon.setImageResource(R.drawable.ic_potion); break;
            case ORUZJE: holder.icon.setImageResource(R.drawable.ic_swords); break;
            case ODECA: holder.icon.setImageResource(R.drawable.ic_shield); break;
            default: holder.icon.setImageResource(R.drawable.ic_potion); break;
        }

        holder.btnActivateEquipment.setOnClickListener(v -> {
            if (eq.getQuantity() <= 0) {
                Toast.makeText(v.getContext(), "Nema više komada ove opreme!", Toast.LENGTH_SHORT).show();
                return;
            }

            holder.qty.setText("Preostalo: " + eq.getQuantity());

            if (onActivateListener != null)
                onActivateListener.onActivate(eq);
        });
    }

    @Override
    public int getItemCount() {
        return equipmentList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name, desc, qty;
        Button btnActivateEquipment;

        ViewHolder(View view) {
            super(view);
            icon = view.findViewById(R.id.ivIcon);
            name = view.findViewById(R.id.tvName);
            desc = view.findViewById(R.id.tvDescription);
            qty = view.findViewById(R.id.tvQuantity);
            btnActivateEquipment = view.findViewById(R.id.btnActivateEquipment);
        }
    }
}
