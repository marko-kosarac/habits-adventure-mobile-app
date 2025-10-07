package com.example.mobilnaaplikacija.services;

import android.content.Context;
import android.media.audiofx.DynamicsProcessing;
import android.util.Log;

import com.example.mobilnaaplikacija.database.SQLiteHelper;
import com.example.mobilnaaplikacija.model.Attack;
import com.example.mobilnaaplikacija.model.Battle;
import com.example.mobilnaaplikacija.model.Boss;
import com.example.mobilnaaplikacija.repository.BattleRepository;
import com.google.firebase.firestore.DocumentReference;

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

    public interface OnBattleCompleted {
        void onBattleFinished(Battle battle);
        void onError(String message);
    }

    public void attackBoss (String userId, String bossId, OnBattleCompleted callback) {
        Battle battle = battleRepository.getBattleByUserAndBoss(userId, bossId);
        if (battle == null) battle = new Battle();
        final AtomicReference<Battle> battleRef = new AtomicReference<>(battle);

        Boss boss = bossService.getBossById(bossId);
        if (boss == null) {
            callback.onError("Boss not found");
            return;
        }
        final AtomicReference<Boss> bossRef = new AtomicReference<>(boss);

        List<Attack> attacks = attackService.getAttacksByUser(userId);
        if (attacks.size() >= 5) {
            callback.onError("Svi pokušaji za napad su iskorišteni.");
            return;
        }

        taskService.getSuccessRate(userId, successRate -> {
            //success rate of attack
            boolean hit = Math.random() * 100 < successRate;
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

                        //boss defeated check
                        if (bossRef.get().isDefeated()) {
                            int coins = bossService.calculateCoins(bossRef.get().getLevel()); // or define logic, e.g. random or level-based
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
                            int coins = bossService.calculateCoins(bossRef.get().getLevel()); // or define logic, e.g. random or level-based
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
                            battleRepository.add(battleRef.get()); //add only if do not exist in db, else update if defeated
                            callback.onBattleFinished(battleRef.get());
                        }

                    })
                    .addOnFailureListener(e -> callback.onError("Failed to load user PP: " + e.getMessage()));
        });
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
        equipmentService.getEquipmentReward(userId, chance);
        //TODO oprema dobijena? pohraniti ili ne?

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
            equipmentService.getEquipmentReward(userId, chance);
            //TODO oprema dobijena? pohraniti ili ne?
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
