package com.example.mobilnaaplikacija.services;

import android.content.Context;
import android.media.audiofx.DynamicsProcessing;
import android.util.Log;

import com.example.mobilnaaplikacija.database.SQLiteHelper;
import com.example.mobilnaaplikacija.model.Attack;
import com.example.mobilnaaplikacija.model.Battle;
import com.example.mobilnaaplikacija.model.Boss;
import com.example.mobilnaaplikacija.model.Equipment;
import com.example.mobilnaaplikacija.repository.BattleRepository;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class BattleService {
    private BattleRepository battleRepository;
    private BossService bossService;
    private UserService userService;
    private AttackService attackService;
    private TaskService taskService;
    private EquipmentService equipmentService;

    public BattleService(Context context) {
        this.battleRepository = new BattleRepository(new SQLiteHelper(context));
        this.bossService = new BossService(context);
        this.userService = new UserService();
        this.attackService = new AttackService(context);
        this.taskService = new TaskService(context);
        this.equipmentService = new EquipmentService(context);
    }

    public Battle startOrGetBattle(FirebaseUser user) {
        Battle battle = battleRepository.getBattleByUser(user.getUid());
        if (battle != null) {
            battle.setAttacks(attackService.getAttacksByUserAndBoss(battle.getUserId(), battle.getBossId()));
            return battle;
        }

        //prvi boss
        Boss boss = new Boss();
        boss.setMaxHp(200);
        boss.setCurrentHp(200);
        boss.setLevel(1);
        boss.setDefeated(false);
        bossService.add(boss);
        //prva borba
        battle = new Battle();
        battle.setUserId(user.getUid());
        battle.setBossId(boss.getId());
        battle.setAttacks(new ArrayList<>());
        battle.setUserWon(false);
        battle.setCoinsEarned(0);
        battleRepository.add(battle);
        return battle;
    }

    public interface OnBattleCompleted {
        void onBattleFinished(Battle battle, Equipment equipment, int coins);
        void onError(String message);
    }

    public void attackBoss (FirebaseUser user, Boss boss, Battle battle, double luck, int successRate, int numberOfAttacks, int bonusCoins, OnBattleCompleted callback) {
        if (numberOfAttacks >= 5) {
            callback.onError("Svi pokušaji za napad su iskorišteni.");
            return;
        }

        final AtomicReference<Battle> battleRef = new AtomicReference<>(battle);
        String userId = user.getUid();

        if (boss == null) {
            callback.onError("Bos nije pronađen.");
            return;
        }
        final AtomicReference<Boss> bossRef = new AtomicReference<>(boss);

        //success rate napada
        boolean hit = luck * 100 < successRate;Attack attack = new Attack();
        attack.setUserId(userId);
        attack.setBossId(boss.getId());
        attack.setHit(hit);
        attack.setAttemptNumber(numberOfAttacks);
        //prethodni napadi
        List<Attack> attacks = attackService.getAttacksByUserAndBoss(userId, boss.getId());

        //PP
        userService.getUserDoc(userId).get()
                .addOnSuccessListener(document -> {
                    int pp = 0;
                    if (document.exists()) {
                        Long powerPoints = document.getLong("powerPoints");
                        if (powerPoints != null) pp = powerPoints.intValue();
                    }

                    if (hit) {
                        attack.setDamageDealt(pp); //TODO should I pass powerPoints?
                    } else {
                        attack.setDamageDealt(0);
                    }

                    attackService.add(attack);
                    attacks.add(attack);

                    if (bossRef.get().isDefeated()) { //TODO Nakon 5 napada, ukoliko je bos poražen, uslov nakon 5 napada?
                        //boss porazen
                        handleVictory(userId, boss, battle, attacks, bonusCoins, callback);
                    } else if (attacks.size() >= 5) {
                        //boss neporazen, ali je 5x napao
                        handleDefeat(userId, boss, battleRef.get(), attacks, bonusCoins, callback);
                    }

                    updateBattleAndBoss(battle, false, boss, userId, 0, attacks); //TODO update?
                    callback.onBattleFinished(battleRef.get(), null, 0);
                })
                .addOnFailureListener(e -> callback.onError("Failed to load user PP: " + e.getMessage()));
    }

    private void handleVictory(String userId, Boss boss, Battle battle, List<Attack> attacks, int bonusCoins, OnBattleCompleted callback) {
        int coins = bossService.calculateCoins(boss.getLevel());
        if (bonusCoins != 0) {
            coins = coins * (1 + bonusCoins/100); //TODO bonus coins 5%
        }

        int finalCoins = coins;
        userService.addCoinsToUser(userId, coins, () ->
                    Log.d("Battle", "Victory: Coins added successfully: " + finalCoins),
                e -> Log.e("Battle", "Victory: Failed to add coins: " + e.getMessage()));

        //šansa od 20% da se dobije komad opreme (95% šanse za odeću, 5% šanse za oružje)
        double chance = 0.20;
        Equipment equipment = equipmentService.getEquipmentReward(userId, chance);
        //TODO user's lvl++?
        //TODO user's xp/pp?

        updateBattleAndBoss(battle, true, boss, userId, coins, attacks);
        callback.onBattleFinished(battle, equipment, coins);
    }

    private void handleDefeat(String userId, Boss boss, Battle battle, List<Attack> attacks, int bonusCoins,  OnBattleCompleted callback) {
        double bossHpPercent = (boss.getCurrentHp() * 100.0) / boss.getMaxHp();
        int baseCoins = bossService.calculateCoins(boss.getLevel());
        int coins;
        double chance = 0.10;
        Equipment equipment = null;

        if (bossHpPercent <= 50) {
            //umanjeno 50% HP boss-a
            coins = baseCoins / 2;
            if (bonusCoins != 0) {
                coins = coins * (1 + bonusCoins/100); //TODO bonus coins 5%
            }
            battle.setCoinsEarned(coins);

            int finalCoins = coins;
            userService.addCoinsToUser(userId, coins, () ->
                            Log.d("Battle", "Defeat: Added " + finalCoins + " coins, boss HP: " + boss.getCurrentHp()),
                    e -> Log.e("Battle", "Defeat: Failed to add coins", e)
            );

            equipment = equipmentService.getEquipmentReward(userId, chance);
            //TODO user's xp/pp?
        } else coins = 0;

        updateBattleAndBoss(battle, false, boss, userId, coins, attacks);
        callback.onBattleFinished(battle, equipment, coins);
    }

    private void updateBattleAndBoss (Battle battle,boolean win, Boss boss, String userId, int coins, List<Attack> attacks) {
        battle.setUserId(userId);
        battle.setBossId(boss.getId());
        battle.setAttacks(attacks);
        battle.setUserWon(win);
        battle.setCoinsEarned(coins);

        battleRepository.update(battle);
        bossService.update(boss);
    }

}
