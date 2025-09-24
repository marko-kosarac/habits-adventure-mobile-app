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

import java.util.ArrayList;

public class ShopFragment extends Fragment {

    private RecyclerView recyclerView;
    private EquipmentListAdapter adapter;

    public ShopFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shop, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewEquipment);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Seedovanje baze ako je prazna
        SQLiteHelper dbHelper = new SQLiteHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        long count = android.database.DatabaseUtils.queryNumEntries(db, SQLiteHelper.TABLE_EQUIPMENT);
        db.close();

        if(count == 0) {
            seedEquipment();
        }

        // Povlačenje opreme iz baze
        ArrayList<Equipment> equipmentList = getAllEquipment();

        // Postavljanje adaptera
        adapter = new EquipmentListAdapter(getContext(), equipmentList);
        recyclerView.setAdapter(adapter);

        return view;
    }

    // Seedovanje baze sa nekoliko stavki
    private void seedEquipment() {
        SQLiteHelper dbHelper = new SQLiteHelper(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        EquipmentSeeder seeder = new EquipmentSeeder(getContext(), db);
        seeder.seedData(); // ovde ubacuje 2-3 stavke
        db.close();
    }

    // Povlačenje svih stavki iz baze
    private ArrayList<Equipment> getAllEquipment() {
        ArrayList<Equipment> equipmentList = new ArrayList<>();
        SQLiteHelper dbHelper = new SQLiteHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] columns = {
                SQLiteHelper.COLUMN_ID,
                SQLiteHelper.COLUMN_NAME,
                SQLiteHelper.COLUMN_DESCRIPTION,
                SQLiteHelper.COLUMN_TYPE,
                SQLiteHelper.COLUMN_BONUS,
                SQLiteHelper.COLUMN_DURATION,
                SQLiteHelper.COLUMN_PRICE
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
                String typeStr = cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_TYPE));
                String bonus = cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_BONUS));
                int duration = cursor.getInt(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_DURATION));
                int price = cursor.getInt(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_PRICE));

                Equipment.Type type = Equipment.Type.valueOf(typeStr);
                equipmentList.add(new Equipment(id, name, description, type, bonus, duration, price));
            }
        }

        db.close();
        return equipmentList;
    }
}
