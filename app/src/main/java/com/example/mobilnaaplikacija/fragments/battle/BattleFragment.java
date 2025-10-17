package com.example.mobilnaaplikacija.fragments.battle;

import android.animation.ObjectAnimator;
import android.graphics.drawable.AnimationDrawable;
import android.media.audiofx.DynamicsProcessing;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
import com.example.mobilnaaplikacija.model.User;
import com.example.mobilnaaplikacija.services.AttackService;
import com.example.mobilnaaplikacija.services.BattleService;
import com.example.mobilnaaplikacija.services.BossService;
import com.example.mobilnaaplikacija.services.TaskService;
import com.example.mobilnaaplikacija.services.UserService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
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
    private Battle battle, nextBattle;
    private boolean nextBattleReady = false;
    private UserService userService;
    private BossService bossService;
    private BattleService battleService;
    private TaskService taskService;
    private AttackService attackService;
    private int PP = 0;
    private int HP = 0, HP_MAX;
    private int numberOfAttacks = 0, calculatedSuccessRate = 0;
    private int bonusCoins = 0, bonusAttack = 0, bonusAttackSuccessChance = 0;
    private List<Equipment> userEquipment;
    private List<Equipment> activeEquipment;
    private Map<String, Object> previousEtapa;
    private int oldLevel = 1;

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

        previousEtapa = (Map<String, Object>) getArguments().getSerializable("previousEtapa");

        oldLevel = getArguments().getInt("oldLevel");
        PP = 20; //prvi nivo
        if (oldLevel > 1) {
            PP = 40; //nakon prvog nivoa
            for (int i = 2; i < oldLevel; i++) {
                PP = PP + (PP * 3 / 4);
            }
        }

        firebaseUser = userService.getCurrentUser();

        battles = battleService.startOrGetBattle(firebaseUser);
        //TODO for loop battles expose each boss in fight

        //prva koju nije pobijedio
        for (Battle b : battles) {
            if (!Boolean.TRUE.equals(b.hasUserWon())) {
                battle = b;
                boss = bossService.getBossById(b.getBossId());
                break;
            }
        }

        //sve je pobijedio
        if (battle == null) {
            Toast.makeText(getContext(), "Sve borbe su pobijeđene! Bravo!", Toast.LENGTH_LONG).show();
            return;
        }

        fetchUserEquipment(() -> {
            setupActiveEquipment();
        });

        setupAnimations(view);

        binding.tvBossLevel.setText("Nivo bosa: " + boss.getLevel());
        //succ rate for leftover boss
        int lvl = (int) (Number) previousEtapa.get("level");
        if (boss.getLevel() < lvl) {
            int bossLvl = boss.getLevel();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            String bossEtapaId = "etapa_" + bossLvl;
            DocumentReference bossEtapaRef = db.collection("users")
                    .document(firebaseUser.getUid())
                    .collection("etapaHistory")
                    .document(bossEtapaId);
            bossEtapaRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists() && documentSnapshot.contains("successRate")) {
                    double bossSuccessRate = documentSnapshot.getDouble("successRate");
                    if (bonusAttackSuccessChance != 0) { //TODO bonus attack
                        bossSuccessRate += bonusAttackSuccessChance;
                        bonusAttackSuccessChance = 0;
                    }

                    String text = String.format(getString(R.string.attack_chance), (int)bossSuccessRate);
                    binding.tvAttackChance.setText(text);
                    setupAttackButton(battle, (int)bossSuccessRate);
                }
            }).addOnFailureListener(e -> {
                Log.e("Firestore", "Failed to fetch boss etapa success rate", e);
            });

        }

        taskService.getSuccessRate(firebaseUser.getUid(), previousEtapa, successRate -> {
            if (!isAdded() || binding == null) return;
            //success rate završene etape
                if (bonusAttackSuccessChance != 0) { //TODO
                    successRate += bonusAttackSuccessChance;
                    bonusAttackSuccessChance = 0;
                }

            if (boss.getLevel() >= lvl) {
                String text = String.format(getString(R.string.attack_chance), (int) successRate);
                binding.tvAttackChance.setText(text);
                setupAttackButton(battle, (int) successRate);
            }

            //update u bazi: etapaHistory (dodat successRate prethodne etape)
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String etapaId = "etapa_" + previousEtapa.get("level");
            DocumentReference etapaHistoryRef = db.collection("users")
                    .document(firebaseUser.getUid())
                    .collection("etapaHistory")
                    .document(etapaId);

            etapaHistoryRef.update("successRate", Math.round(successRate))
                    .addOnSuccessListener(aVoid ->
                            Log.d("EtapaHistoryUpdate", "Success rate updated for " + etapaId))
                    .addOnFailureListener(e ->
                            Log.e("EtapaHistoryUpdate", "Failed to update success rate for " + etapaId, e));

        });

        setupRemainingAttacks();
        setupCoinReward();
    }

    private void setupCoinReward () {
        int coins = bossService.calculateCoins(boss.getLevel()); //TODO bonus coins
        binding.tvCoinReward.setText("+ " + coins);
    }

    private void applyEquipmentEffects(List<Equipment> equipment) {
        if (!isAdded() || binding == null || equipment == null || equipment.isEmpty()) return;

        double totalPPMultiplier = 1.0;
        double totalAttackMultiplier = 1.0;
        int totalBonusCoins = 0;
        int totalBonusAttackChance = 0;

        for (Equipment eq : equipment) {
            switch (eq.getType()) {
                case NAPITAK:
                    totalPPMultiplier *= getPotionMultiplier(eq);
                    break;

                case ORUZJE:
                    if (eq.getName().contains("mač")) {
                        totalPPMultiplier *= Math.pow(1.05, eq.getQuantity());  //jaci napad trajno TODO
                    } else if (eq.getName().contains("Luk")) {
                        totalBonusCoins += 5 * eq.getQuantity(); //novicic TODO
                    }
                    break;

                case ODECA:
                    if (eq.getName().contains("Čizme")) {
                        totalAttackMultiplier *= Math.pow(1.40, eq.getQuantity()); //vise napada TODO
                    } else if (eq.getName().contains("Štit")) {
                        totalBonusAttackChance += 10 * eq.getQuantity(); //success chance poveca TODO
                    } else if (eq.getName().contains("Rukavice")) {
                        totalPPMultiplier *= Math.pow(1.10, eq.getQuantity()); //jaci napad trajno TODO
                    }
                    break;
            }
        }

        PP = (int) Math.ceil(PP * totalPPMultiplier);
        numberOfAttacks = (int) Math.ceil(numberOfAttacks * totalAttackMultiplier);
        bonusCoins += totalBonusCoins;
        bonusAttackSuccessChance += totalBonusAttackChance;

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
                case ORUZJE: icon.setImageResource(R.drawable.ic_swords); break;
                case ODECA: icon.setImageResource(R.drawable.ic_shield); break;
                case NAPITAK: icon.setImageResource(R.drawable.ic_potion); break;
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
        if (!isAdded() || binding == null) return;
        //TODO from battle get attacks
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
    }

    private void setupAttackButton(Battle battle, int successRate) {
        if (!isAdded() || binding == null) return;

        binding.btnAttackBoss.setOnClickListener(v-> {
            if (numberOfAttacks >= 5) {
                Toast.makeText(getContext(), "Svi pokušaji za napad su iskorišteni.", Toast.LENGTH_SHORT).show();
                return;
            } else updateNumberOfAttacks();

            double luck = Math.random() * 100;
            boolean userHit = luck < successRate;

            if (userHit) {
                if (firebaseUser == null || battle == null || boss == null) return;
                //animacija udarca
                animateAttack();
                //oduzmi štetu
                boss = bossService.takeDamage(boss, PP);
                //azuriraj progress bar-ove
                setupProgressBars();

            }

            battleService.attackBoss(firebaseUser, boss, battle, battles, luck, successRate, 0, numberOfAttacks, bonusCoins, activeEquipment, new BattleService.OnBattleCompleted() {
                @Override
                public void onBattleFinished(Battle battle, Equipment equipment, int coins) {
                    double roundedLuck = Math.round(luck * 10.0) / 10.0; //npr 73.4%
                    Log.d("BattleFlow", "onBattleFinished called | hasUserWon=" + battle.hasUserWon()
                            + ", numberOfAttacks=" + numberOfAttacks
                            + ", coins=" + coins
                            + ", equipment=" + (equipment != null));
                    if (Boolean.TRUE.equals(battle.hasUserWon())) {
                        bonusCoins = 0; //TODO
                        Toast.makeText(getContext(), "Pobedio si, bravo! Sreća u napadu: " + roundedLuck + "%", Toast.LENGTH_LONG).show();
                        //ucitaj narednu borbu ako je ima
                        showBattleResultDialog(true, coins, equipment);
                    } else if (numberOfAttacks >= 5 && !Boolean.TRUE.equals(battle.hasUserWon())) {
                        bonusCoins = 0;
                        Toast.makeText(getContext(), "Bos nije poražen! Sreća u napadu: " + roundedLuck + "%", Toast.LENGTH_LONG).show();
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
                }
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

        loadNextBattle();

        dialog.setOnDialogClosedListener(() -> {
            Log.i("BattleDialog", "Dialog closed, navigating now...");
            if (isAdded()) {
                try {
                    if (victory && nextBattleReady && battle != null && boss != null) {
                        Log.i("ShowDialog", "Slededca borba");
                        //              fetchUserEquipment(() -> {
                        Bundle bundle = new Bundle();
                        bundle.putInt("bossLevel", boss.getLevel());
                        bundle.putParcelable("nextBoss", boss);
                        bundle.putSerializable("userEquipmentList", new ArrayList<>(userEquipment));
                        NavHostFragment.findNavController(this).navigate(R.id.action_battleFragment_to_prepareBattleFragment, bundle);
                        //                        });
                    } else {
                        Log.i("ShowDialog", "Profil1");
                        NavHostFragment.findNavController(this).navigate(R.id.profile_page);
                    }
                } catch (IllegalArgumentException e) {
                    Log.e("NavigationError", "Cannot navigate to profile_page from current destination", e);
                }
            }
        });

        dialog.show();
    }

    private void loadNextBattle() {
        for (Battle b : battles) {
            if (!Boolean.TRUE.equals(b.hasUserWon())) {
                //ima još nepobijeđenih boseva
                nextBattleReady = true;
                boss = bossService.getBossById(b.getBossId());
                battle = b;
                return;
            }
        }

        //nema više
        boss = null;
        battle = null;
        Toast.makeText(getContext(), "Nema predstojećih borbi!", Toast.LENGTH_LONG).show();
        Log.i("ShowDialog", "Profil2");
        NavHostFragment.findNavController(this).navigate(R.id.action_battleFragment_to_userProfileFragment);
    }

}


