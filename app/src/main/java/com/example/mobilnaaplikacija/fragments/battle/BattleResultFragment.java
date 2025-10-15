package com.example.mobilnaaplikacija.fragments.battle;

import android.app.Dialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.model.Equipment;

import java.util.List;

public class BattleResultFragment extends Dialog {
    public enum ResultType {
        VICTORY,
        DEFEAT,
        NEUTRAL
    }

    private final ResultType resultType;
    private final int coins;
    private final List<Equipment> rewards;
    private boolean chestOpened = false;

    public BattleResultFragment(@NonNull Context context, ResultType type, int coins, List<Equipment> rewards) {
        super(context, R.style.CustomDialogTheme);
        this.resultType = type;
        this.coins = coins;
        this.rewards = rewards;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_battle_result, null);
        setContentView(view);
        setCancelable(true);

        ImageView ivChest = view.findViewById(R.id.ivClosedChest);
        TextView tvTitle = view.findViewById(R.id.tvResultTitle);
        TextView tvCoins = view.findViewById(R.id.tvCoinsReward);
        LinearLayout equipmentContainer = view.findViewById(R.id.equipmentContainer);

        switch (resultType) {
            case VICTORY:
                tvTitle.setText("Pobeda!");
                tvTitle.setTextColor(getContext().getColor(com.github.dhaval2404.colorpicker.R.color.green_500));
                break;
            case DEFEAT:
                tvTitle.setText("Poraz...");
                tvTitle.setTextColor(getContext().getColor(R.color.red));
                break;
            default:
                tvTitle.setText("Borba u toku");
        }

        // initially hide rewards until chest opens
        tvCoins.setVisibility(View.GONE);
        equipmentContainer.setVisibility(View.GONE);

        // shake animation for closed chest
        Animation shake = AnimationUtils.loadAnimation(getContext(), R.anim.chest_shake);
        ivChest.startAnimation(shake);

        // handle chest opening on click or shake
        ivChest.setOnClickListener(v -> openChest(ivChest, tvCoins, equipmentContainer));

        tvCoins.setText("+ " + coins + " novčića");

        TextView tvEquipmentNames = view.findViewById(R.id.tvEquipmentNames);

        //oprema
        if (rewards != null && !rewards.isEmpty()) {
            StringBuilder names = new StringBuilder();
            for (Equipment eq : rewards) {
                ImageView icon = new ImageView(getContext());
                icon.setLayoutParams(new LinearLayout.LayoutParams(120, 120));
                switch (eq.getType()) {
                    case ORUZJE: icon.setImageResource(R.drawable.ic_swords); break;
                    case ODECA: icon.setImageResource(R.drawable.ic_shield); break;
                    case NAPITAK: icon.setImageResource(R.drawable.ic_potion); break;
                }
                equipmentContainer.addView(icon);

                if (names.length() > 0) names.append("\n");
                names.append("• ").append(eq.getName());
            }
            tvEquipmentNames.setText(names.toString());
        }
    }

    private void openChest(ImageView ivChest, TextView tvCoins, LinearLayout equipmentContainer) {
        if (chestOpened) return;
        chestOpened = true;

        // optional: sound effect
        //MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.game_chest_effect);
        MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.chest_sound_effect);
        //TODO
        mp.start();

        // swap image
        ivChest.setImageResource(R.drawable.chest_opened);

        // reveal rewards with fade-in
        Animation fade = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
        tvCoins.setVisibility(View.VISIBLE);
        tvCoins.startAnimation(fade);
        equipmentContainer.setVisibility(View.VISIBLE);
        equipmentContainer.startAnimation(fade);
    }

}