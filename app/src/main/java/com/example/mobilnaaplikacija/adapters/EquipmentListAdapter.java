package com.example.mobilnaaplikacija.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.model.Equipment;

import java.util.ArrayList;

public class EquipmentListAdapter extends RecyclerView.Adapter<EquipmentListAdapter.ShopViewHolder> {

    private ArrayList<Equipment> shopItems;
    private Context context;

    public EquipmentListAdapter(Context context, ArrayList<Equipment> shopItems) {
        this.context = context;
        this.shopItems = shopItems;
    }

    @NonNull
    @Override
    public ShopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_equipment, parent, false);
        return new ShopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopViewHolder holder, int position) {
        Equipment item = shopItems.get(position);

        holder.textName.setText(item.getName());
        holder.textDescription.setText(item.getDescription());
        holder.textPrice.setText("Cena: " + item.getPrice() + " gold");

        holder.buttonBuy.setOnClickListener(v -> {
            String quantityText = holder.editQuantity.getText().toString();
            if (TextUtils.isEmpty(quantityText)) {
                Toast.makeText(context, "Unesite količinu", Toast.LENGTH_SHORT).show();
                return;
            }
            int quantity = Integer.parseInt(quantityText);
            Toast.makeText(context,
                    "Kupljeno " + quantity + "x " + item.getName() +
                            " za " + (quantity * item.getPrice()) + " gold",
                    Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return shopItems.size();
    }

    public static class ShopViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textDescription, textPrice;
        EditText editQuantity;
        Button buttonBuy;

        public ShopViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.shop_item_name);
            textDescription = itemView.findViewById(R.id.shop_item_description);
            textPrice = itemView.findViewById(R.id.shop_item_price);
            editQuantity = itemView.findViewById(R.id.shop_item_quantity);
            buttonBuy = itemView.findViewById(R.id.shop_item_buy);
        }
    }
}
