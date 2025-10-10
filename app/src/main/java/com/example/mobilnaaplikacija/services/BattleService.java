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
        void onBattleFinished(Battle battle);
        void onError(String message);
    }

    public void attackBoss (String userId, String bossId, Battle battle, double luck, int successRate, OnBattleCompleted callback) {
        final AtomicReference<Battle> battleRef = new AtomicReference<>(battle);

        Boss boss = bossService.getBossById(bossId);
        if (boss == null) {
            callback.onError("Bos nije pronađen.");
            return;
        }
        final AtomicReference<Boss> bossRef = new AtomicReference<>(boss);

        List<Attack> attacks = attackService.getAttacksByUserAndBoss(userId, bossId);
        if (attacks.size() >= 5) {
            callback.onError("Svi pokušaji za napad su iskorišteni.");
            return;
        }

        //success rate napada
        boolean hit = luck * 100 < successRate;
        Attack attack = new Attack();
        attack.setUserId(userId);
        attack.setBossId(bossId);
        attack.setHit(hit);
        attack.setAttemptNumber(attacks.size() + 1);

        //PP
        userService.getUserDoc(userId).get()
                .addOnSuccessListener(document -> {
                    int pp = 0;
                    if (document.exists()) {
                        Long powerPoints = document.getLong("powerPoints");
                        if (powerPoints != null) pp = powerPoints.intValue();
                    }

                    if (hit) {
                        bossRef.set(bossService.takeDamage(bossRef.get(), pp));
                        attack.setDamageDealt(pp);
                    } else {
                        attack.setDamageDealt(0);
                    }

                    attackService.add(attack);
                    attacks.add(attack);

                    //boss porazen
                    if (bossRef.get().isDefeated()) {
                        int coins = bossService.calculateCoins(bossRef.get().getLevel());
                        battleRef.get().setCoinsEarned(coins);

                        userService.addCoinsToUser(userId, coins, () -> {
                                    Log.d("Battle", "Victory: Coins added successfully: " + coins);
                                    handleVictory(userId, boss, battleRef.get(), attacks, callback);
                                },
                                e -> {
                                    Log.e("Battle", "Victory: Failed to add coins: " + e.getMessage());
                                    handleVictory(userId, boss, battleRef.get(), attacks, callback);
                                }
                        );
                    } else if (attacks.size() >= 5) {
                        int coins = bossService.calculateCoins(bossRef.get().getLevel());
                        battleRef.get().setCoinsEarned(coins);

                        userService.addCoinsToUser(userId, coins, () -> {
                                    Log.d("Battle", "Defeat: Coins added successfully: " + coins);
                                    handleDefeat(userId, boss, battleRef.get(), attacks, callback);
                                },
                                e -> {
                                    Log.e("Battle", "Defeat: Failed to add coins: " + e.getMessage());
                                    handleDefeat(userId, boss, battleRef.get(), attacks, callback);
                                }
                        );
                        handleDefeat(userId, boss, battleRef.get(), attacks, callback);
                    } else {
                        battleRef.get().setUserId(userId);
                        battleRef.get().setBossId(bossId);
                        battleRef.get().setAttacks(attacks);
                        battleRef.get().setUserWon(false);
                        battleRepository.add(battleRef.get()); //TODO add only if does not exist in db, else update if defeated
                        callback.onBattleFinished(battleRef.get());
                    }

                })
                .addOnFailureListener(e -> callback.onError("Failed to load user PP: " + e.getMessage()));
    }

    private void handleVictory(String userId, Boss boss, Battle battle, List<Attack> attacks, OnBattleCompleted callback) {
        int coins = bossService.calculateCoins(boss.getLevel());
        battle.setUserId(userId);
        battle.setBossId(boss.getId());
        battle.setAttacks(attacks);
        battle.setUserWon(true);
        battle.setCoinsEarned(coins);

        //sacuvaj
        battleRepository.add(battle);
        bossService.update(boss);

        //šansa od 20% da se dobije komad opreme (95% šanse za odeću, 5% šanse za oružje)
        double chance = 0.20;
        Equipment equipment = equipmentService.getEquipmentReward(userId, chance);
        //TODO user's lvl++?
        //TODO xp/pp?

        callback.onBattleFinished(battle);
    }

    private void handleDefeat(String userId, Boss boss, Battle battle, List<Attack> attacks, OnBattleCompleted callback) {
        double bossHpPercent = (boss.getCurrentHp() * 100.0) / boss.getMaxHp();
        int baseCoins = bossService.calculateCoins(boss.getLevel());
        int coins;

        if (bossHpPercent <= 50) {
            //pola HP → slabija nagrada
            coins = baseCoins / 2;
            double chance = 0.10;
            Equipment equipment = equipmentService.getEquipmentReward(userId, chance);
            //TODO xp/pp?
        } else {
            coins = 0;
        }

        if (coins > 0) {
            userService.addCoinsToUser(userId, coins,
                    () -> Log.d("Battle", "Defeat: Added " + coins + " coins, boss HP: " + boss.getCurrentHp()),
                    e -> Log.e("Battle", "Defeat: Failed to add coins", e)
            );
        }

        battle.setUserId(userId);
        battle.setBossId(boss.getId());
        battle.setAttacks(attacks);
        battle.setUserWon(false);
        battle.setCoinsEarned(coins);

        battleRepository.add(battle);
        bossService.update(boss);

        callback.onBattleFinished(battle);
    }

}
