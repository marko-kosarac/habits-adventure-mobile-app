package com.example.mobilnaaplikacija.fragments.battle;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.adapters.PrepareBattleAdapter;
import com.example.mobilnaaplikacija.databinding.DialogPrepareBattleBinding;
import com.example.mobilnaaplikacija.model.Equipment;

import java.util.ArrayList;
import java.util.List;

public class PrepareBattleFragment extends DialogFragment {

    private DialogPrepareBattleBinding binding;
    private PrepareBattleAdapter adapter;
    private final List<Equipment> activatedEquipment = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DialogPrepareBattleBinding.inflate(inflater, container, false);

        List<Equipment> userEquipment = (List<Equipment>) getArguments().getSerializable("userEquipmentList");
        if (userEquipment == null) userEquipment = new ArrayList<>();

        adapter = new PrepareBattleAdapter(userEquipment);
        binding.rvEquipment.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvEquipment.setAdapter(adapter);

        adapter.setOnActivateListener((eq, activated) -> {
//            if (!activatedEquipment.contains(eq)) {
//                activatedEquipment.add(eq);
//            } else {
//                activatedEquipment.remove(eq);
//            }
            if (activated) {
                activatedEquipment.add(eq);
            } else {
                activatedEquipment.remove(eq);
            }
            updateActivatedEquipmentUI();
        });

        binding.btnStartBattle.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("activatedEquipmentList", new ArrayList<>(activatedEquipment));
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_prepareBattleFragment_to_battleFragment, bundle);
            dismiss();
        });

        return binding.getRoot();
    }

    private void updateActivatedEquipmentUI() {
        LinearLayout container = binding.activatedIconsContainer;
        container.removeAllViews();

        for (Equipment eq : activatedEquipment) {
            ImageView icon = new ImageView(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(80, 80);
            params.setMargins(8, 0, 8, 0);
            icon.setLayoutParams(params);

            switch (eq.getType()) {
                case ORUZJE:
                    icon.setImageResource(R.drawable.ic_swords);
                    break;
                case ODECA:
                    icon.setImageResource(R.drawable.ic_shield);
                    break;
                case NAPITAK:
                    icon.setImageResource(R.drawable.ic_potion);
                    break;
            }

            container.addView(icon);
        }
    }
}
