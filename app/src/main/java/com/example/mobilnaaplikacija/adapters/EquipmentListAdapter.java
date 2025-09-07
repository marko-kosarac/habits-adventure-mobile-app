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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

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
        holder.textPrice.setText("Cena: " + item.getPrice());

        holder.buttonBuy.setOnClickListener(v -> {
            String quantityText = holder.editQuantity.getText().toString();
            if (TextUtils.isEmpty(quantityText)) {
                Toast.makeText(context, "Unesite količinu", Toast.LENGTH_SHORT).show();
                return;
            }

            int quantity = Integer.parseInt(quantityText);
            int totalPrice = item.getPrice() * quantity;

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(userId);

            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Long coins = documentSnapshot.getLong("coins");
                    if (coins == null) coins = 0L;

                    if (coins < totalPrice) {
                        Toast.makeText(context, "Nemate dovoljno novca!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if(quantity <= 0){
                        Toast.makeText(context, "Morate da unesete količinu veću od 0", Toast.LENGTH_SHORT).show();
                        return;
                    }


                    // Oduzimanje novca
                    userRef.update("coins", coins - totalPrice);

                    // Dodavanje kupljene opreme sa quantity
                    Equipment purchasedItem = new Equipment(
                            item.getId(),
                            item.getName(),
                            item.getDescription(),
                            item.getType(),
                            item.getBonus(),
                            item.getDuration(),
                            item.getPrice()
                    );
                    purchasedItem.setQuantity(quantity);

                    userRef.update("equipment", FieldValue.arrayUnion(purchasedItem))
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(context, "Kupljeno " + quantity + "x " + item.getName(), Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(context, "Greška prilikom kupovine: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(context, "Greška pri učitavanju korisnika: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
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
