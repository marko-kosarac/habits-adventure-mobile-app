package com.example.mobilnaaplikacija.fragments;

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
import com.example.mobilnaaplikacija.model.Equipment;

import java.util.ArrayList;

public class ShopFragment extends Fragment {

    private RecyclerView recyclerView;
    private EquipmentListAdapter adapter;
    private ArrayList<Equipment> equipmentList;

    public ShopFragment() {
        // Obavezno prazan konstruktor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflating fragment layout
        View view = inflater.inflate(R.layout.fragment_shop, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewEquipment);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Primer podataka za test
        equipmentList = new ArrayList<>();
        equipmentList.add(new Equipment(1, "Napitak Snage 20%", "Jednokratni napitak koji povećava PP za 20%", "Napitak", "+20% PP", 0));
        equipmentList.add(new Equipment(2, "Rukavice Snage 10%", "Odeća koja povećava snagu za 10%", "Odeća", "+10% Snage", 2));
        equipmentList.add(new Equipment(3, "Mač Snage 5%", "Oružje koje trajno povećava snagu za 5%", "Oružje", "+5% Snage", 0));

        adapter = new EquipmentListAdapter(getContext(), equipmentList);
        recyclerView.setAdapter(adapter);

        return view;
    }
}
