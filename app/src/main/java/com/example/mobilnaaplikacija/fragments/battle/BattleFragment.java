package com.example.mobilnaaplikacija.fragments.battle;

import android.animation.ObjectAnimator;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.databinding.FragmentBattleBinding;
import com.example.mobilnaaplikacija.databinding.FragmentTaskListBinding;
import com.example.mobilnaaplikacija.model.Attack;
import com.example.mobilnaaplikacija.model.Battle;
import com.example.mobilnaaplikacija.model.Boss;
import com.example.mobilnaaplikacija.model.User;
import com.example.mobilnaaplikacija.services.AttackService;
import com.example.mobilnaaplikacija.services.BattleService;
import com.example.mobilnaaplikacija.services.BossService;
import com.example.mobilnaaplikacija.services.TaskService;
import com.example.mobilnaaplikacija.services.UserService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ktx.Firebase;

import java.util.ArrayList;
import java.util.List;

public class BattleFragment extends Fragment {

    private FragmentBattleBinding binding;
    private AnimationDrawable idleAnimation, hitAnimation;
    private FirebaseFirestore db;
    private FirebaseUser firebaseUser;
    private Battle battle;
    private Boss boss;
    private UserService userService;
    private BossService bossService;
    private BattleService battleService;
    private TaskService taskService;
    private AttackService attackService;
    private int PP = 0;
    private int remainingAttacks;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.userService = new UserService();
        this.bossService = new BossService(getContext());
        this.battleService = new BattleService(getContext());
        this.taskService = new TaskService(getContext());
        this.attackService = new AttackService(getContext());
        db = FirebaseFirestore.getInstance();

        binding = FragmentBattleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseUser = userService.getCurrentUser();
        battle = battleService.startOrGetBattle(firebaseUser);
        boss = bossService.getBossById(battle.getBossId());

        fetchUserDataFromFirebase();
        setupAnimations(view);
        taskService.getSuccessRate(firebaseUser.getUid(), successRate -> {
            //success rate napada
            String text = String.format(getString(R.string.attack_chance), (int)successRate);
            binding.tvAttackChance.setText(text);
            setupAttackButton(battle, (int)successRate);
        });
    }

    private User fetchUserDataFromFirebase() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                int xp = doc.getLong("experiencePoints") != null ? doc.getLong("experiencePoints").intValue() : 0;
                int maxPP = doc.getLong("powerPoints") != null ? doc.getLong("powerPoints").intValue() : 0;
                PP = maxPP; //PP se mijenja kako korisnik trosi snagu
                long coins = doc.getLong("coins") != null ? doc.getLong("coins") : 0;
                int level = doc.getLong("level") != null ? doc.getLong("level").intValue() : 1;
                String title = doc.getString("title");
                User user = new User();
                user.setExperiencePoints(xp);
                //currentUser.setCoins();
                //currentUser.setEquipment();
                user.setPowerPoints(maxPP);
                user.setLevel(level);

                setupProgressBars(PP, maxPP);
                //TODO coins azuriraj ako dobije i obavijesti
                //TODO titulu azuriraj ako dobije
                //TODO level azuriraj ako predje
            }
        });
        return null;
    }

    private void setupAnimations(View view){
        ImageView bossSpriteIdle = view.findViewById(R.id.ivBossSpriteIdle);
        ImageView bossSpriteHit = view.findViewById(R.id.ivBossSpriteHit);
        bossSpriteIdle.setBackgroundResource(R.drawable.boss_idle);
        bossSpriteHit.setBackgroundResource(R.drawable.boss_hit);

        idleAnimation = (AnimationDrawable) bossSpriteIdle.getBackground();
        hitAnimation = (AnimationDrawable) bossSpriteHit.getBackground();
        bossSpriteIdle.setVisibility(View.VISIBLE);
        bossSpriteHit.setVisibility(View.GONE);
        idleAnimation.start();
    }

    private void setupProgressBars(int pp, int ppMax){
        binding.pbHPBar.setMax(boss.getMaxHp());
        binding.pbHPBar.setProgress(boss.getCurrentHp());
        binding.pbPPBar.setMax(ppMax);
        binding.pbPPBar.setProgress(pp);
        //TODO
        ObjectAnimator.ofInt(binding.pbHPBar, "progress", boss.getCurrentHp()).setDuration(600).start();
        binding.tvUserPP.setText("PP: " + pp + "/" + ppMax);
        binding.tvBossHP.setText("HP: " + boss.getCurrentHp() + "/" + boss.getMaxHp());
    }

    private void setupAttackButton(Battle battle, int successRate) {
        binding.btnAttackBoss.setOnClickListener(v-> {
            double luck = Math.random() * 100;
            boolean userHit = luck < successRate;
            if (!userHit) {
                    //battleService.missBoss();
                    Toast.makeText(getContext(), "Promašaj! Bos se izmakao! Sreca: " + luck + ".", Toast.LENGTH_SHORT).show();
                    Log.i("ATTACK", "Missed. Luck: " + luck + ". Success rate: " + successRate + ".");
                    return;
                } else {
                    if (firebaseUser == null || battle == null || boss == null) return;

                    Toast.makeText(getContext(), "Pogodak! Napad je uspeo! Sreca: " + luck + ".", Toast.LENGTH_SHORT).show();
                    Log.i("ATTACK", "Hit. Luck: " + luck + ". Success rate: " + successRate + ".");

                    //animacija udarca
                    idleAnimation.stop();
                    binding.ivBossSpriteIdle.setVisibility(View.GONE);
                    binding.ivBossSpriteHit.setVisibility(View.VISIBLE);
                    hitAnimation.stop();
                    hitAnimation.start();

                    int totalDuration = 0;
                    for (int i = 0; i < hitAnimation.getNumberOfFrames(); i++) {
                        totalDuration += hitAnimation.getDuration(i);
                    }

                    binding.ivBossSpriteHit.postDelayed(() -> {
                        hitAnimation.stop();
                        binding.ivBossSpriteHit.setVisibility(View.GONE);
                        binding.ivBossSpriteIdle.setVisibility(View.VISIBLE);
                        idleAnimation.start();
                    }, totalDuration);

                    //napad
                    boss.setCurrentHp(boss.getCurrentHp() - PP);
                    PP
                    battleService.attackBoss(firebaseUser.getUid(), boss.getId(), battle, luck, successRate, new BattleService.OnBattleCompleted() {
                        @Override
                        public void onBattleFinished(Battle battle) {
                            //TODO update/add battle?
                        }

                        @Override
                        public void onError(String message) {
                            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
                        }
                    });

                    /*
                    // Ažuriranje baze level-xp-pp-title
                    db.collection("users").document(userId)
                            .update("level", level, "title", title, "powerPoints", pp)
                            .addOnSuccessListener(aVoid -> Log.d("UserProfile", "Level, titula i PP ažurirani"))
                            .addOnFailureListener(e -> Log.e("UserProfile", "Greška pri ažuriranju levela i PP-a", e));
                    */
                }
        });
    }

    private void goToVictoryScreen(boolean won) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("won", won);
        //Navigation.findNavController(requireView())
        //        .navigate(R.id.action_battleFragment_to_victoryFragment, bundle);
    }
}
