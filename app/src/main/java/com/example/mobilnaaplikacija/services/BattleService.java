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
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class BattleService {
    private BattleRepository battleRepository;
    private BossService bossService;
    private UserService userService;
    private AttackService attackService;
    private EquipmentService equipmentService;

    public BattleService(Context context) {
        this.battleRepository = new BattleRepository(new SQLiteHelper(context));
        this.bossService = new BossService(context);
        this.userService = new UserService();
        this.attackService = new AttackService(context);
        this.equipmentService = new EquipmentService(context);
    }

    public List<Battle> startOrGetBattle(FirebaseUser user) {
        List<Battle> battles = battleRepository.getBattlesByUser(user.getUid());

        if(battles.isEmpty()) {
            //prvi boss
            Boss boss = new Boss();
            boss.setMaxHp(200);
            boss.setCurrentHp(200);
            boss.setLevel(1);
            boss.setDefeated(false);
            bossService.add(boss);

            //prva borba
            Battle battle = new Battle();
            battle.setUserId(user.getUid());
            battle.setBossId(boss.getId());
            //equipment setuje na prazno u repo
            battle.setAttacks(new ArrayList<>());
            battle.setUserWon(null);
            battle.setCoinsEarned(0);
            battleRepository.add(battle);

            battles.add(battle);
            return battles;
        }

        List<Battle> battlesToWin = new ArrayList<>();
        for (Battle existing : battles) {
            Boss boss = bossService.getBossById(existing.getBossId());
            if (!Boolean.TRUE.equals(existing.hasUserWon()) && boss.getCurrentHp() > 0) {
                //slucajno izasao iz borbe - nastavi gdje je stao ili...
                List<Attack> attacks = attackService.getAttacksByUserAndBoss(user.getUid(), boss.getId());
                //...ima >5 (bonus) napada - friska attack lista
                if (attacks.size() >= 5) {
                    //obrisi prethodne napade iz attacks
                    for (Attack a : attacks) {
                        attackService.deleteById(a.getId());
                    }
                    attacks = new ArrayList<>();
                }
                existing.setAttacks(attacks);

                battlesToWin.add(existing);
            }
        }

       return battlesToWin;
    }

    public interface OnBattleCompleted {
        void onBattleFinished(Battle battle, Equipment equipment, int coins);
        void onError(String message);
    }

    public void attackBoss (FirebaseUser user, Boss boss, Battle battle, List<Battle> userBattles, double luck, int successRate, int damage, int numberOfAttacks, int bonusCoins, List<Equipment> equipmentFromBattle, OnBattleCompleted callback) {
        if (numberOfAttacks > 5) {
            callback.onError("Svi pokušaji za napad su iskorišteni.");
            return;
        }

        String userId = user.getUid();

        if (boss == null) {
            callback.onError("Bos nije pronađen.");
            return;
        }

        //success rate napada
        boolean hit = luck <= successRate;
        Attack attack = new Attack();
        attack.setUserId(userId);
        attack.setBossId(boss.getId());
        attack.setHit(hit);
        attack.setAttemptNumber(numberOfAttacks);
        attack.setDamageDealt(damage);

        //prethodni napadi
        List<Attack> attacks = attackService.getAttacksByUserAndBoss(userId, boss.getId());
        attacks.add(attack);
        attackService.add(attack);

        if (boss.isDefeated()) { //TODO Nakon 5 napada, ukoliko je bos poražen, uslov nakon 5 napada?
            //boss porazen
            checkAndCreateNextBattleIfNeeded(userBattles, battle, boss, userId);
            handleVictory(userId, boss, battle, attacks, bonusCoins, equipmentFromBattle, callback);
        } else if (!boss.isDefeated() && attacks.size() >= 5) {
            //boss neporazen
            checkAndCreateNextBattleIfNeeded(userBattles, battle, boss, userId);
            handleDefeat(userId, boss, battle, attacks, bonusCoins, equipmentFromBattle, callback);
        } else {
            updateBattleAndBoss(battle, null, boss, userId, 0, attacks, equipmentFromBattle);
            callback.onBattleFinished(battle, null, 0);
        }
    }

    private void handleVictory(String userId, Boss boss, Battle battle, List<Attack> attacks, int bonusCoins, List<Equipment> equipmentFromBattle, OnBattleCompleted callback) {
        int coins = bossService.calculateCoins(boss.getLevel());
        if (bonusCoins != 0) {
            coins *= 1.05; //TODO bonus coins 5%
        }

        int finalCoins = coins;
        userService.addCoinsToUser(userId, coins, () ->
                    Log.d("Battle", "Victory: Coins added successfully: " + finalCoins),
                e -> Log.e("Battle", "Victory: Failed to add coins: " + e.getMessage()));

        //šansa od 20% da se dobije komad opreme (95% šanse za odeću, 5% šanse za oružje)
        double chance = 0.20;
        Equipment equipmentReward = null;//equipmentService.getEquipmentReward(userId, chance); TODO

        updateBattleAndBoss(battle, true, boss, userId, coins, attacks, equipmentFromBattle);
        //equipmentService.manageEquipmentAfterBattle(userId, equipmentFromBattle);
        callback.onBattleFinished(battle, equipmentReward, coins);
    }

    private void handleDefeat(String userId, Boss boss, Battle battle, List<Attack> attacks, int bonusCoins, List<Equipment> equipmentFromBattle, OnBattleCompleted callback) {
        double bossHpPercent = (boss.getCurrentHp() * 100.0) / boss.getMaxHp();
        int baseCoins = bossService.calculateCoins(boss.getLevel());
        int coins;
        double chance = 0.10;
        Equipment equipmentReward = null;

        if (bossHpPercent <= 50) {
            //umanjeno 50% HP boss-a
            coins = baseCoins / 2;
            if (bonusCoins != 0) {
                coins = coins * (1 + bonusCoins/100); //TODO bonus coins 5%
            }

            int finalCoins = coins;
            userService.addCoinsToUser(userId, coins, () ->
                            Log.d("Battle", "Defeat: Added " + finalCoins + " coins, boss HP: " + boss.getCurrentHp()),
                    e -> Log.e("Battle", "Defeat: Failed to add coins", e)
            );

            //equipmentReward = equipmentService.getEquipmentReward(userId, chance); TODO

        } else coins = 0;

        //reset boss HP
        boss.setCurrentHp(bossService.calculateMaxHp(boss.getLevel()));

        updateBattleAndBoss(battle, null, boss, userId, coins, attacks, equipmentFromBattle);
        //equipmentService.manageEquipmentAfterBattle(userId, equipmentFromBattle);
    callback.onBattleFinished(battle, equipmentReward, coins);
    }

    private void updateBattleAndBoss (Battle battle, Boolean win, Boss boss, String userId, int coins, List<Attack> attacks, List<Equipment> equipmentFromBattle) {
        battle.setUserId(userId);
        battle.setBossId(boss.getId());
        battle.setAttacks(attacks);
        battle.setUserWon(win);
        battle.setCoinsEarned(coins);
        //borba prosla - stavi njenu opremu
        List<String> equipmentIds = new ArrayList<>();
        if (!equipmentFromBattle.isEmpty()) {
            for (Equipment eq : equipmentFromBattle) {
                int qty = eq.getQuantity() > 0 ? eq.getQuantity() : 1;
                for (int i = 0; i < qty; i++) {
                    equipmentIds.add(String.valueOf(eq.getId()));
                }
            }
        }
        battle.setEquipmentIds(equipmentIds);

        battleRepository.update(battle);
        bossService.update(boss);
    }

    private void checkAndCreateNextBattleIfNeeded(List<Battle> userBattles, Battle battle, Boss boss, String userId) {
        for (Battle b : userBattles) {
            if (b.getId().equals(battle.getId())) {
                boolean isLastBattle = userBattles.indexOf(b) == userBattles.size() - 1;

                if (isLastBattle) {
                    //poslednji boss pobijedjen - novi dodaj
                    Boss newBoss = bossService.addUpgradedBoss(boss);

                    //naredna borba spremna (ceka ga kad predje ponovo nekad nivo)
                    Battle newBattle = new Battle();
                    newBattle.setUserId(userId);
                    newBattle.setBossId(newBoss.getId());
                    //nova borba - nema opreme
                    newBattle.setEquipmentIdsFromString("");
                    newBattle.setCoinsEarned(0);
                    newBattle.setUserWon(null);

                    battleRepository.add(newBattle);
                }
                break;
            }
        }
    }


}
