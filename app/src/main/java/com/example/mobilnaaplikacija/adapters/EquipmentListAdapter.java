package com.example.mobilnaaplikacija.adapters;

import android.content.Context;
import android.text.InputFilter;
import android.text.InputType;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        if (item.getDescription().contains("snag"))
            holder.textBonus.setText("Bonus: " + item.getBonus() + " PP");
        else if (item.getDescription().contains("šansu"))
            holder.textBonus.setText("Bonus: " + item.getBonus() + " šansa napada");
        else if (item.getDescription().contains("broj"))
            holder.textBonus.setText("Bonus: " + item.getBonus() + " broj napada");
        holder.textPrice.setText("Cena: " + item.getPrice());

        holder.buttonBuy.setOnClickListener(v -> {
            String quantityText = holder.editQuantity.getText().toString();
            if (TextUtils.isEmpty(quantityText)) {
                Toast.makeText(context, "Unesite količinu", Toast.LENGTH_SHORT).show();
                return;
            }

            int quantity = Integer.parseInt(quantityText);
            if (quantity <= 0) {
                Toast.makeText(context, "Morate da unesete količinu veću od 0", Toast.LENGTH_SHORT).show();
                return;
            }

            int totalPrice = item.getPrice() * quantity;
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(userId);

            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (!documentSnapshot.exists()) return;

                Long coins = documentSnapshot.getLong("coins");
                if (coins == null) coins = 0L;

                if (coins < totalPrice) {
                    Toast.makeText(context, "Nemate dovoljno novca!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Oduzimanje novca
                userRef.update("coins", coins - totalPrice);

                List<Map<String, Object>> equipmentData = (List<Map<String, Object>>) documentSnapshot.get("equipment");
                if (equipmentData == null) equipmentData = new ArrayList<>();
                boolean exists = false;

                for (Map<String, Object> e : equipmentData) {
                    long id = ((Number) e.get("id")).longValue();
                    boolean active = e.get("active") != null && (Boolean) e.get("active");

                    if (id == item.getId() && !active) {
                        int oldQty = ((Number) e.get("quantity")).intValue();
                        e.put("quantity", oldQty + quantity); // poveća quantity

                        if (!e.containsKey("count") || e.get("count") == null) {
                            e.put("count", 0);
                        }

                        exists = true;
                        break;
                    }
                }

                if (!exists) {
                    Map<String, Object> newItem = new HashMap<>();
                    newItem.put("id", item.getId());
                    newItem.put("name", item.getName());
                    newItem.put("description", item.getDescription());
                    newItem.put("type", item.getType().name());
                    newItem.put("bonus", item.getBonus());
                    newItem.put("duration", item.getDuration());
                    newItem.put("price", item.getPrice());
                    newItem.put("quantity", quantity);
                    newItem.put("count", item.getCount());
                    newItem.put("active", false);

                    equipmentData.add(newItem);
                }

                userRef.update("equipment", equipmentData)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(context, "Kupljeno " + quantity + "x " + item.getName(), Toast.LENGTH_SHORT).show();
                            holder.editQuantity.setText(""); // resetuje unos
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Greška prilikom kupovine: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });

            }).addOnFailureListener(e -> {
                Toast.makeText(context, "Greška pri učitavanju korisnika: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        });
    }

    public void updateList(ArrayList<Equipment> newList) {
        shopItems.clear();
        shopItems.addAll(newList);
        notifyDataSetChanged();
    }



    @Override
    public int getItemCount() {
        return shopItems.size();
    }

    public static class ShopViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textDescription, textPrice, textBonus;
        EditText editQuantity;
        Button buttonBuy;

        public ShopViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.shop_item_name);
            textDescription = itemView.findViewById(R.id.shop_item_description);
            textPrice = itemView.findViewById(R.id.shop_item_price);
            editQuantity = itemView.findViewById(R.id.shop_item_quantity);
            textBonus = itemView.findViewById(R.id.shop_item_bonus);
            buttonBuy = itemView.findViewById(R.id.shop_item_buy);
            editQuantity.setInputType(InputType.TYPE_CLASS_NUMBER);

            editQuantity.setFilters(new InputFilter[]{
                    (source, start, end, dest, dstart, dend) -> {
                        // Proverava da li je svaki karakter cifra
                        for (int i = start; i < end; i++) {
                            if (!Character.isDigit(source.charAt(i))) {
                                return "";
                            }
                        }
                        return null;
                    }
            });
        }

    }
}
