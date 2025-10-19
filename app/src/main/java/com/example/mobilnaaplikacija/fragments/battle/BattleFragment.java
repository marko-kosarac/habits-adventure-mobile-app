package com.example.mobilnaaplikacija.fragments.battle;

import android.animation.ObjectAnimator;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.databinding.FragmentBattleBinding;
import com.example.mobilnaaplikacija.model.Attack;
import com.example.mobilnaaplikacija.model.Battle;
import com.example.mobilnaaplikacija.model.Boss;
import com.example.mobilnaaplikacija.model.Equipment;
import com.example.mobilnaaplikacija.services.battle.AttackService;
import com.example.mobilnaaplikacija.services.battle.BattleService;
import com.example.mobilnaaplikacija.services.battle.BossService;
import com.example.mobilnaaplikacija.services.EquipmentService;
import com.example.mobilnaaplikacija.services.task.TaskService;
import com.example.mobilnaaplikacija.services.UserService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BattleFragment extends Fragment {

    private FragmentBattleBinding binding;
    private AnimationDrawable idleAnimation, hitAnimation;
    private FirebaseFirestore db;
    private FirebaseUser firebaseUser;
    private List<Battle> battles;
    private Boss boss;
    private Battle battle;
    private boolean nextBattleReady = false;
    private UserService userService;
    private BossService bossService;
    private BattleService battleService;
    private TaskService taskService;
    private AttackService attackService;
    private EquipmentService equipmentService;
    private int PP = 0;
    private int HP = 0, HP_MAX;
    private int numberOfAttacks = 0, maxAttacks = 5;
    private double bonusCoins = 1;
    private int bonusAttackSuccessChance = 0;
    private List<Equipment> userEquipment;
    private List<Equipment> activeEquipment;
    private int oldLevel = 1;
    private boolean isFromUserProfile = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.userService = new UserService();
        this.bossService = new BossService(getContext());
        this.battleService = new BattleService(getContext());
        this.taskService = new TaskService(getContext());
        this.attackService = new AttackService(getContext());
        this.equipmentService = new EquipmentService(getContext());
        db = FirebaseFirestore.getInstance();

        binding = FragmentBattleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseUser = userService.getCurrentUser();
        if (firebaseUser == null) return;

        Bundle args = getArguments();
        if (args != null) {
            isFromUserProfile = args.getBoolean("isFromUserProfile");
            oldLevel = args.getInt("oldLevel", 1);
        }

        fetchPowerPoints(firebaseUser.getUid());

        battles = battleService.startOrGetBattle(firebaseUser);

        for (Battle b : battles) {
            //prva koju nije pobijedio ide u borbu
            if (!Boolean.TRUE.equals(b.hasUserWon())) {
                battle = b;
                boss = bossService.getBossById(b.getBossId());
                break;
            }
        }

        for (Battle b : battles) {
            //kreiraj za novi predjeni nivo i bossa i borbu
            boolean isLastBattle = battles.indexOf(b) == battles.size() - 1;
            if (isLastBattle && isFromUserProfile) {
                Boss bossTemp = bossService.getBossById(b.getBossId());
                Battle newBattle = createNextBattleIfNeeded(b, bossTemp, firebaseUser.getUid(), oldLevel);
                battles.add(newBattle);
            }
        }

        if (boss != null) {
            setupRemainingAttacks();
        } else {
            Log.w("BattleFragment", "No valid boss found for remaining attacks setup");
        }

        if (boss != null && firebaseUser != null) {
            fetchUserEquipment(() -> {
                setupActiveEquipment();
                loadAndHandleEtapaSuccessRate(boss.getLevel(), firebaseUser.getUid()); //posle efekata opreme se ucita
                setupCoinReward();
            });
        }

        setupAnimations(view);
        binding.tvBossLevel.setText("Nivo bosa: " + boss.getLevel());
    }

    private void fetchPowerPoints(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Log.w("PP", "User not found: " + userId);
                        binding.tvUserPP.setText("Snaga korisnika: 0");
                        PP = 0;
                        return;
                    }

                    int level = 1;
                    if (documentSnapshot.contains("level")) {
                        Number lvlNum = documentSnapshot.getLong("level");
                        if (lvlNum != null) level = lvlNum.intValue();
                    }

                    level--;
                    //base PP
                    long basePP = 20;
                    if (level > 1) {
                        basePP = 40;
                        for (int i = 2; i < level; i++) {
                            basePP = basePP + (basePP * 3 / 4);
                        }
                    }

                    double finalPP = basePP;
                    PP = (int) Math.ceil(finalPP);
                    setupProgressBars();
                })
                .addOnFailureListener(e -> {
                    Log.e("PP", "Failed to fetch user", e);
                    binding.tvUserPP.setText("Snaga: 0");
                });
    }

    private void loadAndHandleEtapaSuccessRate(int bossLevel, String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //etape iz etapaHistory
        db.collection("users")
                .document(userId)
                .collection("etapaHistory")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Log.w("EtapaHistory", "No etapas found for user " + userId);
                        setupAttackButton(battle, 0);
                        return;
                    }

                    Map<String, Object> matchingEtapa = null;

                    //etapa ima isti level kao boss
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Long level = doc.getLong("level");
                        if (level != null && level.intValue() == bossLevel) {
                            matchingEtapa = doc.getData();
                            break;
                        }
                    }

                    if (matchingEtapa == null) {
                        Log.w("EtapaHistory", "No matching etapa for boss level " + bossLevel);
                        setupAttackButton(battle, 0);
                        return;
                    }

                    //successRate
                    taskService.getSuccessRate(userId, matchingEtapa, calculatedRate -> {
                        if (!isAdded() || binding == null) return;

                        //bonus
                        calculatedRate += bonusAttackSuccessChance;
                        if (calculatedRate > 100) {
                            calculatedRate = 100;
                        }

                        //UI
                        String text = String.format(getString(R.string.attack_chance), (int) Math.round(calculatedRate));
                        binding.tvAttackChance.setText(text);
                        setupAttackButton(battle, (int) Math.round(calculatedRate));

                        //update u etapi u bazi
                        String etapaDocId = "etapa_" + bossLevel;
                        db.collection("users")
                                .document(userId)
                                .collection("etapaHistory")
                                .document(etapaDocId)
                                .update("successRate", (int) Math.round(calculatedRate))
                                .addOnSuccessListener(aVoid -> Log.d("EtapaHistory", "Updated successRate for etapa " + etapaDocId))
                                .addOnFailureListener(e -> Log.e("EtapaHistory", "Failed to update successRate for etapa " + etapaDocId, e));
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("EtapaHistory", "Failed to fetch etapaHistory for user " + userId, e);
                    setupAttackButton(battle, 0);
                });
    }

    private Battle createNextBattleIfNeeded(Battle b, Boss boss, String userId, int oldLevel) {
        Boss bossFromLastBattle = bossService.getBossById(battle.getBossId());
        boolean exists = battleService.bossExistsForLevel(userId, oldLevel);
        if (bossFromLastBattle != null && boss.getLevel() < oldLevel && !exists) {
            //spremna borba tog nivoa kao i boss
            Boss newBoss = new Boss();
            newBoss.setLevel(oldLevel);
            newBoss.setDefeated(false);
            int hp = bossService.calculateMaxHp(oldLevel);
            newBoss.setMaxHp(hp);
            newBoss.setCurrentHp(hp);
            Boss addedBoss = bossService.add(newBoss);

            Battle newBattle = new Battle();
            newBattle.setUserId(userId);
            newBattle.setBossId(addedBoss.getId());
            //nova borba - nema opreme
            newBattle.setEquipmentIdsFromString("");
            newBattle.setCoinsEarned(0);
            newBattle.setUserWon(null);
            battleService.add(newBattle);
        }
        return battle;
    }

    private void setupCoinReward () {
        int coins = bossService.calculateCoins(boss.getLevel());
        coins *= (int) bonusCoins;
        binding.tvCoinReward.setText("+ " + coins);
    }

    private void applyEquipmentEffects(List<Equipment> equipment) {
        if (!isAdded() || binding == null || equipment == null || equipment.isEmpty()) return;

        double totalPPMultiplier = 1.0;
        double totalBonusCoinsMultiplier = 1.0;
        int totalAttackChance = 0;
        int extraAttacks = 0;

        for (Equipment eq : equipment) {
            switch (eq.getType()) {
                case NAPITAK:
                    totalPPMultiplier *= getPotionMultiplier(eq);
                    break;

                case ORUZJE:
                    if (eq.getName().contains("mač")) {
                        totalPPMultiplier *= Math.pow(1.05, eq.getQuantity());  //jaci napad trajno
                    } else if (eq.getName().contains("Luk")) {
                        totalBonusCoinsMultiplier *= Math.pow(1.05, eq.getQuantity()); //novcici
                    }
                    break;

                case ODECA:
                    if (eq.getName().contains("Čizme")) {  //vise napada
                        for (int i = 0; i < eq.getQuantity(); i++) {
                            if (Math.random() < 0.4) {
                                extraAttacks += 1;
                            }
                        }
                    } else if (eq.getName().contains("Štit")) {
                        totalAttackChance += (10 * eq.getQuantity()); // +10% šanse po komadu
                    }else if (eq.getName().contains("Rukavice")) {
                        totalPPMultiplier *= Math.pow(1.10, eq.getQuantity()); //jaci napad trajno
                    }
                    break;
            }
        }

        PP = (int) Math.ceil(PP * totalPPMultiplier);
        maxAttacks += extraAttacks;
        bonusCoins = totalBonusCoinsMultiplier;
        bonusAttackSuccessChance = totalAttackChance;

        setupProgressBars();
    }

    private double getPotionMultiplier(Equipment eq) {
        double multiplier = 1.0;

        if (eq.getBonus().contains("+20%")) multiplier = 1.20;
        else if (eq.getBonus().contains("+40%")) multiplier = 1.40;
        else if (eq.getBonus().contains("+5%")) multiplier = 1.05;
        else if (eq.getBonus().contains("+10%")) multiplier = 1.10;
        else if (eq.getBonus().contains("+15%")) multiplier = 1.15;
        else if (eq.getBonus().contains("+8%")) multiplier = 1.08;

        return Math.pow(multiplier, eq.getQuantity());
    }

    private void fetchUserEquipment(Runnable onComplete) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        List<Map<String, Object>> equipmentData = (List<Map<String, Object>>) document.get("equipment");

                        userEquipment = new ArrayList<>();

                        if (equipmentData != null) {
                            for (Map<String, Object> data : equipmentData) {
                                Equipment eq = mapToEquipment(data);
                                userEquipment.add(eq);
                            }
                        }
                    }
                    if (onComplete != null) onComplete.run();
                });
    }

    private Equipment mapToEquipment(Map<String, Object> data) {
        Equipment eq = new Equipment();
        eq.setId(((Number) data.get("id")).longValue());
        eq.setName((String) data.get("name"));
        eq.setDescription((String) data.get("description"));
        eq.setBonus((String) data.get("bonus"));
        eq.setDuration(((Number) data.get("duration")).intValue());
        eq.setPrice(((Number) data.get("price")).intValue());
        eq.setQuantity(((Number) data.get("quantity")).intValue());
        eq.setActive((Boolean) data.get("active"));
        eq.setCount(data.get("count") != null ? ((Number) data.get("count")).intValue() : 0);
        eq.setType(Equipment.Type.valueOf((String) data.get("type")));
        return eq;
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
        binding.pbPPBar.setMax(PP);
        binding.pbPPBar.setProgress(PP);
        ObjectAnimator.ofInt(binding.pbHPBar, "progress", boss.getCurrentHp()).setDuration(600).start();
        binding.tvUserPP.setText("Snaga korisnika: " + PP);
        binding.tvBossHP.setText("Energija: " + HP + "/" + HP_MAX);
    }

    private void setupActiveEquipment() {
        activeEquipment = (List<Equipment>) getArguments().getSerializable("activeEquipmentList");
        if (activeEquipment == null) activeEquipment = new ArrayList<>();
        applyEquipmentEffects(activeEquipment);
        updateActiveEquipmentUI();
    }

    private void updateActiveEquipmentUI() {
        if (!isAdded() || binding == null) return;
        LinearLayout container = binding.activatedIconsContainer;
        container.removeAllViews();

        if (activeEquipment == null || activeEquipment.isEmpty()) return;

        //grupisano name + bonus
        Map<String, List<Equipment>> grouped = new HashMap<>();
        for (Equipment eq : activeEquipment) {
            String key = eq.getName() + "_" + eq.getBonus();
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(eq);
        }

        for (Map.Entry<String, List<Equipment>> entry : grouped.entrySet()) {
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
                case ORUZJE:
                    if (first.getName().toLowerCase().contains("mač")) {
                        icon.setImageResource(R.drawable.ic_swords);
                    } else if (first.getName().toLowerCase().contains("luk")) {
                        icon.setImageResource(R.drawable.ic_arrow);
                    }
                    break;
                case ODECA: String nameLower = first.getName().toLowerCase();
                    if (nameLower.contains("čizme")) {
                        icon.setImageResource(R.drawable.ic_boots);
                    } else if (nameLower.contains("štit")) {
                        icon.setImageResource(R.drawable.ic_shield);
                    } else if (nameLower.contains("rukavice")) {
                        icon.setImageResource(R.drawable.ic_glove);
                    }
                    break;
                case NAPITAK: icon.setImageResource(R.drawable.ic_potion); break;
                default:
                    icon.setImageResource(R.drawable.ic_potion);
                    break;
            }
            itemLayout.addView(icon);

            //količine aktivne opreme
            TextView qty = new TextView(getContext());
            qty.setText(String.valueOf(totalQty));
            qty.setGravity(Gravity.CENTER);
            itemLayout.addView(qty);

            //tooltip
            itemLayout.setOnClickListener(v -> {
                String msg = first.getName() + " " + first.getBonus() + " x" + totalQty;
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            });

            container.addView(itemLayout);
        }
    }

    private void setupRemainingAttacks () {
        if (!isAdded() || binding == null || boss == null) return;
        List<Attack> attacks = attackService.getAttacksByUserAndBoss(firebaseUser.getUid(), boss.getId());
        numberOfAttacks = attacks.size();
        String text = String.format(getString(R.string.attack_number), numberOfAttacks, maxAttacks);
        binding.tvAttackCount.setText(text);
    }

    private void updateNumberOfAttacks () {
        numberOfAttacks++;
        String text = String.format(getString(R.string.attack_number), numberOfAttacks, maxAttacks);
        binding.tvAttackCount.setText(text);
    }

    private void setupAttackButton(Battle battle, int successRate) {
        if (!isAdded() || binding == null) return;

        binding.btnAttackBoss.setOnClickListener(v-> {
            if (numberOfAttacks > maxAttacks) {
                Toast.makeText(getContext(), "Svi pokušaji za napad su iskorišteni.", Toast.LENGTH_SHORT).show();
//                return;
            } else updateNumberOfAttacks();

            double luck = Math.random() * 100;
            boolean userHit = luck < successRate;
            int damage = 0;

            if (userHit) {
                if (firebaseUser == null || battle == null || boss == null) return;
                //animacija udarca
                animateAttack();
                //oduzmi štetu
                boss = bossService.takeDamage(boss, PP);
                //azuriraj progress bar-ove
                setupProgressBars();
                damage = PP;
            }

            battleService.attackBoss(firebaseUser, boss, battle, maxAttacks, luck, successRate, damage, numberOfAttacks, bonusCoins, activeEquipment, new BattleService.OnBattleCompleted() {
                @Override
                public void onBattleFinished(Battle battle, Equipment equipment, int coins) {
                    double roundedLuck = Math.round(luck * 10.0) / 10.0; //npr 73.4%
                    Log.d("BattleFlow", "onBattleFinished called | hasUserWon=" + battle.hasUserWon()
                            + ", numberOfAttacks=" + numberOfAttacks
                            + ", coins=" + coins
                            + ", equipment=" + (equipment != null));
                    if (Boolean.TRUE.equals(battle.hasUserWon())) {
                        Toast.makeText(getContext(), "Pobedio si, bravo! Sreća u napadu: " + roundedLuck + "%", Toast.LENGTH_LONG).show();

                        equipmentService.manageEquipmentAfterBattle(firebaseUser.getUid(), activeEquipment);
                        showBattleResultDialog(true, coins, equipment);
                    } else if (numberOfAttacks >= maxAttacks && !Boolean.TRUE.equals(battle.hasUserWon())) {
                        bonusCoins = 0;
                        Toast.makeText(getContext(), "Bos nije poražen! Sreća u napadu: " + roundedLuck + "%", Toast.LENGTH_LONG).show();

                        equipmentService.manageEquipmentAfterBattle(firebaseUser.getUid(), activeEquipment);
                        if (coins > 0 || equipment != null) {
                            showBattleResultDialog(false, coins, equipment);
                        } else {
                            NavHostFragment.findNavController(BattleFragment.this)
                                    .navigate(R.id.action_battleFragment_to_userProfileFragment);
                        }
                    } else {
                        if (userHit) Toast.makeText(getContext(), "Pogodak! Sreća: " + roundedLuck + "%", Toast.LENGTH_SHORT).show();
                        else Toast.makeText(getContext(), "Promašaj! Sreća: " + roundedLuck + "%", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(BattleFragment.this)
                            .navigate(R.id.action_battleFragment_to_userProfileFragment);                }
            });
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

    private void showBattleResultDialog(boolean victory, int coins, Equipment reward) {
        BattleResultFragment.ResultType type = victory ? BattleResultFragment.ResultType.VICTORY : BattleResultFragment.ResultType.DEFEAT;

        BattleResultFragment dialog = new BattleResultFragment(requireActivity(), type, coins, reward);
        dialog.setOnDialogClosedListener(() -> {
            if (isAdded()) {
                if (victory) {
                    loadNextBattle();
                } else {
                    NavHostFragment.findNavController(this).navigate(R.id.profile_page);
                }
            }
        });

        dialog.show();
    }

    private void loadNextBattle() {
        if (firebaseUser == null) return;

        battles = battleService.startOrGetBattle(firebaseUser);
        nextBattleReady = false;
        battle = null;
        boss = null;

        for (Battle b : battles) {
            if (!Boolean.TRUE.equals(b.hasUserWon())) {
                nextBattleReady = true;
                battle = b;
                boss = bossService.getBossById(b.getBossId());
                break;
            }
        }

        Log.d("BattleFragment", "battles.size=" + battles.size() +
                ", nextBattleReady=" + nextBattleReady +
                ", battle=" + battle +
                ", boss=" + boss);

        if (nextBattleReady && battle != null && boss != null) {
            Bundle bundle = new Bundle();
            bundle.putInt("bossLevel", boss.getLevel());
            bundle.putParcelable("nextBoss", boss);

            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_battleFragment_to_prepareBattleFragment, bundle);
        } else {
            Toast.makeText(getContext(), "Nema predstojećih borbi!", Toast.LENGTH_LONG).show();
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_battleFragment_to_userProfileFragment);
        }
    }

}
