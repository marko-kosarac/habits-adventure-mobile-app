package com.example.mobilnaaplikacija.fragments.battle;

import android.animation.ObjectAnimator;
import android.graphics.drawable.AnimationDrawable;
import android.media.audiofx.DynamicsProcessing;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.databinding.FragmentBattleBinding;
import com.example.mobilnaaplikacija.model.Attack;
import com.example.mobilnaaplikacija.model.Battle;
import com.example.mobilnaaplikacija.model.Boss;
import com.example.mobilnaaplikacija.model.Equipment;
import com.example.mobilnaaplikacija.model.User;
import com.example.mobilnaaplikacija.services.AttackService;
import com.example.mobilnaaplikacija.services.BattleService;
import com.example.mobilnaaplikacija.services.BossService;
import com.example.mobilnaaplikacija.services.TaskService;
import com.example.mobilnaaplikacija.services.UserService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

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
    private int PP = 0, PP_MAX;
    private int HP = 0, HP_MAX;
    private int numberOfAttacks = 0, calculatedSuccessRate = 0;
    private int bonusCoins = 0, bonusAttack = 0, bonusAttackSuccessChance = 0;
    private List<Equipment> activeEquipment;

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
            if (!isAdded() || binding == null) return;
            //success rate napada
            if (bonusAttackSuccessChance != 0) { //TODO
                successRate += bonusAttackSuccessChance;
                bonusAttackSuccessChance = 0;
            }

            String text = String.format(getString(R.string.attack_chance), (int)successRate);
            binding.tvAttackChance.setText(text);
            setupAttackButton(battle, (int)successRate);
        });
        setupRemainingAttacks();
    }

    private void applyEquipmentEffects(List<Equipment> equipment) {
        if (!isAdded() || binding == null) return;
        if (equipment.isEmpty()) return;
        for (Equipment eq : equipment) {
            switch (eq.getType()) {
                case NAPITAK:
                    applyPotionEffect(eq);
                    break;
                case ORUZJE:
                    applyWeaponEffect(eq);
                    break;
                case ODECA:
                    applyArmorEffect(eq);
                    break;
                default:
                    return;
            }
        }
        Toast.makeText(getContext(), "Aktivirana oprema!", Toast.LENGTH_SHORT).show();
        setupProgressBars();
    }

    private void applyPotionEffect(Equipment eq) {
        if (eq.getName().contains("PP +20%")) {
            PP_MAX *= 1.20;
        } else if (eq.getName().contains("PP +40%")) {
            PP_MAX *= 1.40;
        } else if (eq.getName().contains("PP +15%")) {
            PP_MAX *= 1.15;
        } else if (eq.getName().contains("Snage +5%")) {
            PP_MAX *= 1.05; //TODO trajno
        } else if (eq.getName().contains("Snage +8%")) {
            PP_MAX *= 1.08; //TODO trajno
        } else if (eq.getName().contains("Snage +10%")) {
            PP_MAX *= 1.10; //TODO trajno
        }
        PP = PP_MAX;
    }

    private void applyWeaponEffect(Equipment eq) {
        if (eq.getName().contains("Mač")) {
            PP_MAX *= 1.05; //TODO trajno
        } else if (eq.getName().contains("Luk")) {
            bonusCoins += 5;
        }
        PP = PP_MAX;
    }

    private void applyArmorEffect(Equipment eq) {
        if (eq.getName().contains("Čizme")) {
            if (Math.random() < 0.4) {
                bonusAttack += 1;
            }
        } else if (eq.getName().contains("Štit")) {
            bonusAttackSuccessChance += 10;
        }
    }

    private User fetchUserDataFromFirebase() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if (!isAdded() || binding == null) return;

            if (doc.exists()) {
                int xp = doc.getLong("experiencePoints") != null ? doc.getLong("experiencePoints").intValue() : 0;
                PP_MAX = doc.getLong("powerPoints") != null ? doc.getLong("powerPoints").intValue() : 0;
                PP = PP_MAX; //PP se mijenja kako korisnik trosi snagu
                long coins = doc.getLong("coins") != null ? doc.getLong("coins") : 0;
                int level = doc.getLong("level") != null ? doc.getLong("level").intValue() : 1;
                String title = doc.getString("title");
                User user = new User();
                user.setExperiencePoints(xp);
                //currentUser.setCoins();
                //currentUser.setEquipment();
                user.setPowerPoints(PP_MAX);
                user.setLevel(level);
                //TODO titulu azuriraj ako dobije
                //TODO level azuriraj ako predje

                setupProgressBars();
                setupEquipment();
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

    private void setupProgressBars(){
        HP = boss.getCurrentHp();
        HP_MAX = boss.getMaxHp();
        binding.pbHPBar.setMax(HP_MAX);
        binding.pbHPBar.setProgress(HP);
        binding.pbPPBar.setMax(PP_MAX);
        binding.pbPPBar.setProgress(PP);
        ObjectAnimator.ofInt(binding.pbHPBar, "progress", boss.getCurrentHp()).setDuration(600).start();
        ObjectAnimator.ofInt(binding.pbPPBar, "progress", PP).setDuration(600).start();
        binding.tvUserPP.setText("PP: " + PP + "/" + PP_MAX);
        binding.tvBossHP.setText("HP: " + HP + "/" + HP_MAX);
    }

    private void setupEquipment() {
        activeEquipment = (List<Equipment>) getArguments().getSerializable("activatedEquipmentList");
        applyEquipmentEffects(activeEquipment); //TODO bring chosen equipment
    }

    private void setupRemainingAttacks () {
        if (!isAdded() || binding == null) return;

        List<Attack> attacks = attackService.getAttacksByUserAndBoss(firebaseUser.getUid(), boss.getId());
        numberOfAttacks = attacks.size();
        if (bonusAttack != 0) {
            bonusAttack--;
            numberOfAttacks += bonusAttack; //TODO
        }
        String text = String.format(getString(R.string.attack_number), numberOfAttacks);
        binding.tvAttackCount.setText(text);
    }

    private void updateNumberOfAttacks () {
        numberOfAttacks++;
        String text = String.format(getString(R.string.attack_number), numberOfAttacks);
        binding.tvAttackCount.setText(text);
        //update UI
    }

    private void setupAttackButton(Battle battle, int successRate) {
        if (!isAdded() || binding == null) return;
        binding.btnAttackBoss.setOnClickListener(v-> {
            updateNumberOfAttacks();
            double luck = Math.random() * 100;
            boolean userHit = luck < successRate;
            if (!userHit) {
                //user missed
                Log.i("ATTACK", "Missed. Luck: " + luck + ". Success rate: " + successRate + ".");
                battleService.attackBoss(firebaseUser, boss, battle, luck, successRate, numberOfAttacks, bonusCoins, new BattleService.OnBattleCompleted() {
                    @Override
                    public void onBattleFinished(Battle battle, Equipment equipment, int coins) {
                        if (numberOfAttacks <= 5 && battle.hasUserWon()) {
                            bonusCoins = 0;
                            Toast.makeText(getContext(), "Pobedio si! Bravo!", Toast.LENGTH_SHORT).show();
                            //goToFinishedBattleScreen(equipment, coins);
                            //show rewards if any
                        } else if (numberOfAttacks >= 5 && !battle.hasUserWon()) {
                            bonusCoins = 0;
                            Toast.makeText(getContext(), "Više sreće drugi put, bos nije poražen.", Toast.LENGTH_SHORT).show();
                            //goToFinishedBattleScreen(equipment, coins);
                            //show rewards if any
                        } else Toast.makeText(getContext(), "Promašaj! Bos se izmakao!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
                    }
                });
            } else {
                //user hit
                Log.i("ATTACK", "Hit. Luck: " + luck + ". Success rate: " + successRate + ".");
                if (firebaseUser == null || battle == null || boss == null) return;

                //animacija udarca
                animateAttack();

                //napad
                boss = bossService.takeDamage(boss, PP);
                PP = 0;
                //azuriraj progress bar-ove
                setupProgressBars();
                battleService.attackBoss(firebaseUser, boss, battle, luck, successRate, numberOfAttacks, bonusCoins, new BattleService.OnBattleCompleted() {
                    @Override
                    public void onBattleFinished(Battle battle, Equipment equipment, int coins) {
                        if (battle.hasUserWon()) {
                            bonusCoins = 0;
                            Toast.makeText(getContext(), "POBEDIO si, bravo!", Toast.LENGTH_SHORT).show();
                            //goToFinishedBattleScreen(equipment, coins);
                            //show rewards if any
                            //TODO if won, show him reward if equipment!=null, and coins !=0
                        } else if (numberOfAttacks >= 5 && !battle.hasUserWon()) {
                            bonusCoins = 0;
                            Toast.makeText(getContext(), "Više sreće drugi put, bos nije poražen.", Toast.LENGTH_SHORT).show();
                            //goToFinishedBattleScreen(equipment, coins);
                            //show rewards if any
                        } else
                            Toast.makeText(getContext(), "Pogodak! Napad je uspeo!", Toast.LENGTH_SHORT).show();
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

    private void animateAttack () {
        if (!isAdded() || binding == null) return;

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
    }

    private void goToVictoryScreen(boolean won) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("won", won);
        //Navigation.findNavController(requireView())
        //        .navigate(R.id.action_battleFragment_to_victoryFragment, bundle);
    }
}
