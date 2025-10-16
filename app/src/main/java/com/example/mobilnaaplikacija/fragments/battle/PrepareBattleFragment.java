package com.example.mobilnaaplikacija.fragments.battle;

import android.media.audiofx.DynamicsProcessing;
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
import com.example.mobilnaaplikacija.model.Boss;
import com.example.mobilnaaplikacija.model.Equipment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrepareBattleFragment extends DialogFragment {

    private DialogPrepareBattleBinding binding;
    private PrepareBattleAdapter adapter;
    private List<Equipment> userEquipment;
    private final List<Equipment> activeEquipment = new ArrayList<>();
    private final List<Equipment> unactiveEquipment = new ArrayList<>();
    private FirebaseFirestore db;
    private int oldLevel = 1;
    private Boss boss = null;
    private Map<String, Object> previousEtapa;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DialogPrepareBattleBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();

        Bundle args = getArguments();
        if (args != null) {
            previousEtapa = (Map<String, Object>) args.getSerializable("previousEtapa");

            oldLevel = args.getInt("oldLevel");

            boss = args.getParcelable("nextBoss");

            userEquipment = (List<Equipment>) args.getSerializable("userEquipmentList");
            if (userEquipment == null) userEquipment = new ArrayList<>();
        }

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


        Map<Long, Equipment> groupedMap = new HashMap<>();
        for (Equipment eq : unactiveEquipment) {
            if (!eq.isActive()) {
                groupedMap.compute(eq.getId(), (id, existing) -> {
                    if (existing == null) return new Equipment(eq);
                    existing.setQuantity(existing.getQuantity() + eq.getQuantity());
                    return existing;
                });
            }
        }
        List<Equipment> groupedUnactiveEquipment = new ArrayList<>(groupedMap.values());

        adapter = new PrepareBattleAdapter(groupedUnactiveEquipment);
        binding.rvUnactiveEquipment.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvUnactiveEquipment.setAdapter(adapter);

        List<Equipment> finalUserEquipment = userEquipment;

        adapter.setOnActivateListener(eq -> {
            Equipment existingActive = null;
            for (Equipment e : activeEquipment) {
                if (e.getId() == eq.getId()) {
                    existingActive = e;
                    break;
                }
            }

            if (existingActive != null) {
                //broj aktivne
                existingActive.setQuantity(existingActive.getQuantity() + 1);
            } else {
                //nova aktivna
                Equipment newActive = new Equipment(eq);
                newActive.setActive(true);
                newActive.setQuantity(1);
                activeEquipment.add(newActive);
            }

            if (eq.getQuantity() <= 0) {
                unactiveEquipment.remove(eq);
            }

            updateActivatedEquipmentUI();
            adapter.notifyDataSetChanged();
            activateEquipment(eq, finalUserEquipment);
        });

        binding.btnStartBattle.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("activeEquipmentList", new ArrayList<>(activeEquipment));
            if (previousEtapa != null) bundle.putSerializable("previousEtapa", (Serializable) previousEtapa);
            bundle.putInt("oldLevel", oldLevel);
            Navigation.findNavController(requireView()).navigate(R.id.action_prepareBattleFragment_to_battleFragment, bundle);
            dismiss();
        });

        return binding.getRoot();
    }

    private void activateEquipment(Equipment eq, List<Equipment> userEquipment) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(userId);

        //smanji quantity u objektu neaktivnom
        eq.setQuantity(eq.getQuantity() - 1);

        //provjeri da li ista oprema već postoji u aktivnim
        Equipment activeEq = null;
        for (Equipment e : userEquipment) {
            if (e.isActive() && e.getId() == eq.getId()) {
                activeEq = e;
                break;
            }
        }

        if (activeEq == null) {
            //ako ne postoji, kreiraj novu aktivnu instancu
            activeEq = new Equipment();
            activeEq.setId(eq.getId());
            activeEq.setName(eq.getName());
            activeEq.setDescription(eq.getDescription());
            activeEq.setBonus(eq.getBonus());
            activeEq.setDuration(eq.getDuration());
            activeEq.setPrice(eq.getPrice());
            activeEq.setQuantity(1);
            activeEq.setType(eq.getType());
            activeEq.setActive(true);
            activeEq.setCount(0);
            userEquipment.add(activeEq);
        }

        //ažuriraj bazu
        Equipment finalActiveEq = activeEq;
        userRef.get().addOnSuccessListener(doc -> {
            List<Map<String, Object>> equipmentData = (List<Map<String, Object>>) doc.get("equipment");
            if (equipmentData == null) equipmentData = new ArrayList<>();
            List<Map<String, Object>> updatedList = new ArrayList<>();

            //update quantity u inventaru i dodaj aktivne
            for (Map<String, Object> e : equipmentData) {
                long id = ((Number) e.get("id")).longValue();
                int qty = ((Number) e.get("quantity")).intValue();

                if (id == eq.getId()) {
                    qty = qty - 1; //smanji quantity u inventaru
                }

                if (qty > 0) {
                    e.put("quantity", qty);
                    updatedList.add(e);
                }
            }

            // Dodaj ili ažuriraj aktivnu opremu
            boolean found = false;
            for (Map<String, Object> e : updatedList) {
                if (((Number) e.get("id")).longValue() == finalActiveEq.getId() && (Boolean) e.get("active")) {
                    e.put("quantity", finalActiveEq.getQuantity());
                    found = true;
                    break;
                }
            }
            if (!found) {
                Map<String, Object> activeMap = new HashMap<>();
                activeMap.put("id", finalActiveEq.getId());
                activeMap.put("name", finalActiveEq.getName());
                activeMap.put("description", finalActiveEq.getDescription());
                activeMap.put("bonus", finalActiveEq.getBonus());
                activeMap.put("duration", finalActiveEq.getDuration());
                activeMap.put("price", finalActiveEq.getPrice());
                activeMap.put("quantity", finalActiveEq.getQuantity());
                activeMap.put("type", finalActiveEq.getType().name());
                activeMap.put("active", true);
                updatedList.add(activeMap);
            }

            userRef.update("equipment", updatedList).addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(),  "Oprema je aktivirana!", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void updateActivatedEquipmentUI() {
        LinearLayout container = binding.activatedIconsContainer;
        container.removeAllViews();

        //grupacija po name + bonus
        Map<String, List<Equipment>> equipmentByIdentity = new HashMap<>();
        for (Equipment e : activeEquipment) {
            String key = e.getName() + "_" + e.getBonus();
            equipmentByIdentity.computeIfAbsent(key, k -> new ArrayList<>()).add(e);
        }

        //za svaku grupu
        for (Map.Entry<String, List<Equipment>> entry : equipmentByIdentity.entrySet()) {
            List<Equipment> items = entry.getValue();
            Equipment first = items.get(0);

            int totalQty = items.stream().mapToInt(Equipment::getQuantity).sum();

            LinearLayout itemLayout = new LinearLayout(getContext());
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            itemLayout.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(140, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 0, 8, 0);
            itemLayout.setLayoutParams(params);

            //ikonice
            ImageView icon = new ImageView(getContext());
            icon.setLayoutParams(new LinearLayout.LayoutParams(80, 80));
            switch (first.getType()) {
                case ORUZJE: icon.setImageResource(R.drawable.ic_swords); break;
                case ODECA: icon.setImageResource(R.drawable.ic_shield); break;
                case NAPITAK: icon.setImageResource(R.drawable.ic_potion); break;
            }
            itemLayout.addView(icon);

            //sumirane kolicine aktivne opreme
            TextView qty = new TextView(getContext());
            qty.setText(String.valueOf(totalQty));
            qty.setGravity(Gravity.CENTER);
            itemLayout.addView(qty);

            //tooltip za aktivnu opremu u ikonicama
            itemLayout.setOnClickListener(v -> {
                StringBuilder details = new StringBuilder();
                details.append(first.getName()).append(" +").append(first.getBonus()).append(" x").append(totalQty);
                Toast.makeText(getContext(), details.toString(), Toast.LENGTH_SHORT).show();
            });

            container.addView(itemLayout);
        }
    }


}
