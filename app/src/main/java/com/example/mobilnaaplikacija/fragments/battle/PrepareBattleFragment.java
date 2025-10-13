package com.example.mobilnaaplikacija.fragments.battle;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.adapters.PrepareBattleAdapter;
import com.example.mobilnaaplikacija.databinding.DialogPrepareBattleBinding;
import com.example.mobilnaaplikacija.model.Equipment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrepareBattleFragment extends DialogFragment {

    private DialogPrepareBattleBinding binding;
    private PrepareBattleAdapter adapter;
    private final List<Equipment> activeEquipment = new ArrayList<>();
    private final List<Equipment> unactiveEquipment = new ArrayList<>();
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DialogPrepareBattleBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();

        List<Equipment> userEquipment = (List<Equipment>) getArguments().getSerializable("userEquipmentList");
        if (userEquipment == null) userEquipment = new ArrayList<>();

        for (Equipment e : userEquipment) {
            if (e.isActive()) {
                activeEquipment.add(e);
            } else unactiveEquipment.add(e);
        }

        updateActivatedEquipmentUI();
        if (unactiveEquipment.isEmpty()) {
            binding.rvUnactiveEquipment.setVisibility(View.GONE);
            binding.tvTitle.setVisibility(View.GONE);
            binding.emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            binding.rvUnactiveEquipment.setVisibility(View.VISIBLE);
            binding.tvTitle.setVisibility(View.VISIBLE);
            binding.emptyStateLayout.setVisibility(View.GONE);
        }

        adapter = new PrepareBattleAdapter(unactiveEquipment);
        binding.rvUnactiveEquipment.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvUnactiveEquipment.setAdapter(adapter);

        adapter.setOnActivateListener((eq, activated) -> {
            if (activated) {
                //aktivacija opreme
                eq.setActive(true);
                activeEquipment.add(eq);
                updateUserEquipment(eq);
            }
            else {
                for (int i = 0; i < activeEquipment.size(); i++) {
                    if (activeEquipment.get(i).getId() == eq.getId()) {
                        activeEquipment.remove(i);
                        break;
                    }
                }
            }
            updateActivatedEquipmentUI();
        });

        binding.btnStartBattle.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("activeEquipmentList", new ArrayList<>(activeEquipment));
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_prepareBattleFragment_to_battleFragment, bundle);
            dismiss();
        });

        return binding.getRoot();
    }

    private void updateUserEquipment (Equipment eq) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                List<Map<String, Object>> equipmentData = (List<Map<String, Object>>) doc.get("equipment");
                if (equipmentData != null) {
                    for (Map<String, Object> data : equipmentData) {
                        long id = ((Number)data.get("id")).longValue();
                        if (id == eq.getId()) {
                            data.put("quantity", eq.getQuantity());
                            break;
                        }
                    }
                    db.collection("users").document(uid)
                            .update("equipment", equipmentData);
                }
            }
        });
    }

    private void updateActivatedEquipmentUI() {
        LinearLayout container = binding.activatedIconsContainer;
        container.removeAllViews();

        Map<String, Integer> countsByKey = new HashMap<>(); // use name or type+id key
        for (Equipment e : activeEquipment) {
            String key = e.getType().name(); // group by type; or use e.getId() for per-item
            countsByKey.put(key, countsByKey.getOrDefault(key, 0) + 1);
        }

        for (Map.Entry<String, Integer> entry : countsByKey.entrySet()) {
            Equipment.Type type = Equipment.Type.valueOf(entry.getKey());
            int count = entry.getValue();

            LinearLayout item = new LinearLayout(getContext());
            item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(140, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 0, 8, 0);
            item.setLayoutParams(params);

            ImageView icon = new ImageView(getContext());
            icon.setLayoutParams(new LinearLayout.LayoutParams(80, 80));
            switch (type) {
                case ORUZJE: icon.setImageResource(R.drawable.ic_swords); break;
                case ODECA: icon.setImageResource(R.drawable.ic_shield); break;
                case NAPITAK: icon.setImageResource(R.drawable.ic_potion); break;
            }
            item.addView(icon);

            TextView qty = new TextView(getContext());
            qty.setText(String.valueOf(count));
            qty.setGravity(Gravity.CENTER);
            item.addView(qty);

            container.addView(item);
        }
    }
}
