package com.example.mobilnaaplikacija.fragments.battle;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.services.BossService;
import com.example.mobilnaaplikacija.services.UserService;

public class BattleFragment extends Fragment {

    private ProgressBar bossHpBar, playerPpBar;
    private TextView chanceText, attackCountText;
    private Button attackButton;
    private int remainingAttacks = 5;
    private double successChance = 0.67; //TODO Example
    private double bossHp = 200;
    private double playerPp = 25; //TODO later from userService

    private UserService userService;
    private BossService bossService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_battle, container, false);

        bossHpBar = view.findViewById(R.id.bossHpBar);
        playerPpBar = view.findViewById(R.id.playerPpBar);
        chanceText = view.findViewById(R.id.chanceText);
        attackCountText = view.findViewById(R.id.attackCountText);
        attackButton = view.findViewById(R.id.attackButton);

        userService = new UserService();
        bossService = new BossService(requireContext());

        attackButton.setOnClickListener(v -> performAttack());
//        ImageView bossSprite = view.findViewById(R.id.ivBossSprite);
//        bossSprite.setBackgroundResource(R.drawable.boss_idle);
//
//        AnimationDrawable idleAnimation = (AnimationDrawable) bossSprite.getBackground();
//        idleAnimation.start();

        /*
        bossSprite.setBackgroundResource(R.drawable.boss_hit);
        AnimationDrawable hitAnimation = (AnimationDrawable) bossSprite.getBackground();
        hitAnimation.start();

        // Optionally return to idle after hit
        bossSprite.postDelayed(() -> {
            bossSprite.setBackgroundResource(R.drawable.boss_idle);
            ((AnimationDrawable) bossSprite.getBackground()).start();
        }, 400);
        */

        updateUI();
        return view;
    }

    private void performAttack() {
        if (remainingAttacks <= 0) return;

        remainingAttacks--;

        double roll = Math.random();
        if (roll <= successChance) {
            bossHp -= playerPp;
            Toast.makeText(requireContext(), "Pogodak! Bos gubi " + playerPp + " HP.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Promašaj!", Toast.LENGTH_SHORT).show();
        }

        if (bossHp <= 0) {
            goToVictoryScreen(true);
            return;
        }

        if (remainingAttacks == 0) {
            goToVictoryScreen(false);
        }

        updateUI();
    }

    private void updateUI() {
        bossHpBar.setProgress((int) Math.max(0, (bossHp / 200) * 100));
        attackCountText.setText("Pokušaji: " + remainingAttacks + "/5");
    }

    private void goToVictoryScreen(boolean won) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("won", won);
        //Navigation.findNavController(requireView())
        //        .navigate(R.id.action_battleFragment_to_victoryFragment, bundle);
    }
}
