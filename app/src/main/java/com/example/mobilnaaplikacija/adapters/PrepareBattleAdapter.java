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

public class PrepareBattleAdapter extends RecyclerView.Adapter<PrepareBattleAdapter.ViewHolder> {

    private List<Equipment> equipmentList;
    private OnActivateListener onActivateListener;
    private Set<Long> activatedIds = new HashSet<>();

    public interface OnActivateListener {
        void onActivate(Equipment eq, boolean activated);
    }

    public PrepareBattleAdapter(List<Equipment> equipmentList) {
        this.equipmentList = equipmentList;
    }

    public void setOnActivateListener(OnActivateListener listener) {
        this.onActivateListener = listener;
    }

    public List<Equipment> getActivatedEquipment() { //TODO useful or not?
        List<Equipment> res = new ArrayList<>();
        for (Equipment eq : equipmentList) {
            if (activatedIds.contains(eq.getId())) {
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
        holder.qty.setText("Količina: " + eq.getQuantity());

        switch (eq.getType()) {
            case NAPITAK: holder.icon.setImageResource(R.drawable.ic_potion); break;
            case ORUZJE: holder.icon.setImageResource(R.drawable.ic_swords); break;
            case ODECA: holder.icon.setImageResource(R.drawable.ic_shield); break;
        }

        switch (eq.getType()) {
            case NAPITAK: holder.icon.setImageResource(R.drawable.ic_potion); break;
            case ORUZJE: holder.icon.setImageResource(R.drawable.ic_swords); break;
            case ODECA: holder.icon.setImageResource(R.drawable.ic_shield); break;
            default: holder.icon.setImageResource(R.drawable.ic_potion); break;
        }

        holder.btnActivateEquipment.setOnClickListener(v -> {
            boolean isActivated = activatedIds.contains(eq.getId());
            if (isActivated) {
                activatedIds.remove(eq.getId());
                eq.setQuantity(eq.getQuantity() + 1);
            } else {
                if (eq.getQuantity() <= 0) {
                    Toast.makeText(v.getContext(), "Nema više komada ove opreme", Toast.LENGTH_SHORT).show();
                    return;
                }
                activatedIds.add(eq.getId());
                eq.setQuantity(eq.getQuantity() - 1);
            }
            notifyItemChanged(position);

            if (onActivateListener != null)
                onActivateListener.onActivate(eq, !isActivated);
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
