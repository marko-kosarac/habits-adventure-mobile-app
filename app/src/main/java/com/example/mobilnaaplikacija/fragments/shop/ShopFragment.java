package com.example.mobilnaaplikacija.fragments.shop;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.adapters.EquipmentListAdapter;
import com.example.mobilnaaplikacija.database.EquipmentSeeder;
import com.example.mobilnaaplikacija.database.SQLiteHelper;
import com.example.mobilnaaplikacija.model.Equipment;
import com.example.mobilnaaplikacija.utils.PriceCalculator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ShopFragment extends Fragment {

    private RecyclerView recyclerView;
    private EquipmentListAdapter adapter;
    private FirebaseFirestore firestore;

    private int currentUserLevel = 1;

    public ShopFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shop, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewEquipment);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        firestore = FirebaseFirestore.getInstance();

        // Seedovanje baze ako je prazna
        SQLiteHelper dbHelper = new SQLiteHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        long count = android.database.DatabaseUtils.queryNumEntries(db, SQLiteHelper.TABLE_EQUIPMENT);
        db.close();

        seedEquipment();

        // Prvo učitaj nivo korisnika, pa tek onda listu opreme
        fetchUserLevel();

        return view;
    }

    private void fetchUserLevel() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userRef = firestore.collection("users").document(userId);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Long levelValue = documentSnapshot.getLong("level");
                if (levelValue != null) currentUserLevel = levelValue.intValue();
            }
            // Tek sada učitaj opremu sa pravim nivoom
            loadEquipmentList();
        }).addOnFailureListener(e -> loadEquipmentList());
    }

    private void seedEquipment() {
        SQLiteHelper dbHelper = new SQLiteHelper(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        EquipmentSeeder seeder = new EquipmentSeeder(getContext(), db);
        seeder.seedData(); // ubacuje stavke
        db.close();
    }

    private void loadEquipmentList() {
        ArrayList<Equipment> equipmentList = new ArrayList<>();
        SQLiteHelper dbHelper = new SQLiteHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] columns = {
                SQLiteHelper.COLUMN_ID,
                SQLiteHelper.COLUMN_NAME,
                SQLiteHelper.COLUMN_DESCRIPTION,
                SQLiteHelper.COLUMN_TYPE,
                SQLiteHelper.COLUMN_BONUS,
                SQLiteHelper.COLUMN_DURATION
        };

        try (android.database.Cursor cursor = db.query(
                SQLiteHelper.TABLE_EQUIPMENT,
                columns,
                null, null, null, null, null
        )) {
            while(cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_NAME));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_DESCRIPTION));
                Equipment.Type type = Equipment.Type.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_TYPE)));
                String bonus = cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_BONUS));
                int duration = cursor.getInt(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_DURATION));

                // Dinamičko računanje cene na osnovu nivoa korisnika
                int price = PriceCalculator.calculatePrice(currentUserLevel, type, bonus);

                equipmentList.add(new Equipment(id, name, description, type, bonus, duration, price));
            }
        }

        db.close();

        if (adapter == null) {
            adapter = new EquipmentListAdapter(getContext(), equipmentList);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateList(equipmentList); // metoda u adapteru za osvežavanje liste
        }
    }
}
