package com.example.mobilnaaplikacija.fragments.battle;

import static com.example.mobilnaaplikacija.model.Equipment.Type.NAPITAK;
import static com.example.mobilnaaplikacija.model.Equipment.Type.ODECA;
import static com.example.mobilnaaplikacija.model.Equipment.Type.ORUZJE;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.airbnb.lottie.LottieAnimationView;
import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.model.Equipment;

import java.util.List;

public class BattleResultFragment extends Dialog {
    public enum ResultType {
        VICTORY,
        DEFEAT
    }

    private final ResultType resultType;
    private final int coins;
    private final Equipment reward;
    private boolean chestOpened = false;
    private OnDialogClosedListener listener;

    public interface OnDialogClosedListener {
        void onDialogClosed();
    }

    public void setOnDialogClosedListener(OnDialogClosedListener listener) {
        this.listener = listener;
    }

    public BattleResultFragment(@NonNull Context context, ResultType type, int coins, Equipment reward) {
        super(context, R.style.CustomDialogTheme);
        this.resultType = type;
        this.coins = coins;
        this.reward = reward;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_battle_result, null);
        setContentView(view);
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        ImageView ivClosedChest = view.findViewById(R.id.ivClosedChest);
        ImageView ivOpenedChest = view.findViewById(R.id.ivOpenedChest);
        TextView tvTitle = view.findViewById(R.id.tvResultTitle);
        TextView tvCoins = view.findViewById(R.id.tvCoins);
        LinearLayout equipmentContainer = view.findViewById(R.id.equipmentContainer);
        LinearLayout layoutEquipment = view.findViewById(R.id.layoutEquipment);
        LinearLayout layoutRewards = view.findViewById(R.id.layoutRewards);
        TextView tvEquipment = view.findViewById(R.id.tvequipment);
        Button btnAccept = view.findViewById(R.id.btnAcceptRewards);
        LottieAnimationView confetti = view.findViewById(R.id.confetti);

        switch (resultType) {
            case VICTORY: tvTitle.setText("Pobeda!"); break;
            case DEFEAT: tvTitle.setText("Poraz..."); break;
            default: tvTitle.setText("Borba u toku");
        }

        layoutRewards.setVisibility(View.GONE);
        tvTitle.setVisibility(View.VISIBLE);

        //shake animacija kovcega
        Animation shake = AnimationUtils.loadAnimation(getContext(), R.anim.chest_shake);
        ivClosedChest.startAnimation(shake);

        //klik na kovceg
        ivClosedChest.setOnClickListener(v -> openChest(ivClosedChest, ivOpenedChest, layoutRewards, tvCoins, confetti));

        tvCoins.setVisibility(coins > 0 ? View.VISIBLE : View.GONE);

        layoutEquipment.setVisibility((reward != null) ? View.VISIBLE : View.GONE);
        if (reward != null) {
            StringBuilder names = new StringBuilder();
            ImageView icon = new ImageView(getContext());
            icon.setLayoutParams(new LinearLayout.LayoutParams(80, 80));

            switch (reward.getType()) {
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

            equipmentContainer.addView(icon);
            names.append(reward.getName());
            tvEquipment.setText(names.toString());
        } else {
            tvEquipment.setText("");
        }

        btnAccept.setOnClickListener(v -> {
            dismiss();
        });

        setOnDismissListener(dialog -> {
            if (listener != null) listener.onDialogClosed();
        });
    }

    private void openChest(ImageView ivClosed, ImageView ivOpened, LinearLayout layoutRewards,
                           TextView tvCoins, LottieAnimationView confetti) {
        if (chestOpened) return;
        chestOpened = true;

        ivClosed.setOnClickListener(null);
        ivClosed.setVisibility(View.GONE);
        ivOpened.setVisibility(View.VISIBLE);

        layoutRewards.setVisibility(View.VISIBLE);

        //fejd in
        layoutRewards.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));

        tvCoins.setText("+ " + coins + " novčića");

        //konfete
        if (resultType == ResultType.VICTORY) {
            confetti.bringToFront();
            confetti.setZ(10f);
            confetti.setVisibility(View.VISIBLE);
            confetti.setAnimation("confetti.json");
            confetti.playAnimation();
            confetti.setFailureListener(result -> {
                Log.e("LottieError", "Failed to load animation", result);
            });

            if (getContext() != null) {
                MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.game_chest_effect);
                if (mp != null) {
                    mp.setOnCompletionListener(player -> player.release());
                    mp.start();
                }
            }
        }
    }

}