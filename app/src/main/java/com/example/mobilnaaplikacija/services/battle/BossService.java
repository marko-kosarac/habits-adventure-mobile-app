package com.example.mobilnaaplikacija.services.battle;

import android.content.Context;

import com.example.mobilnaaplikacija.database.SQLiteHelper;
import com.example.mobilnaaplikacija.model.Boss;
import com.example.mobilnaaplikacija.repository.*;

public class BossService {
    private final BossRepository bossRepository;

    public BossService(Context context) {
        this.bossRepository = new BossRepository(new SQLiteHelper(context));
    }

    public Boss getBossById (String id) {
        return  bossRepository.getBossById(id);
    }

    public Boss add (Boss boss) {
        return bossRepository.add(boss);
    }

   public Boss takeDamage(Boss boss, int damage) {
        boss.setCurrentHp(boss.getCurrentHp() - damage);
        if (boss.getCurrentHp() < 0) {
            boss.setCurrentHp(0);
            boss.setDefeated(true);
        } else {
            boss.setDefeated(false);
        }
    return boss;
    }

    public int calculateMaxHp (int level) {
        int hp = 200;
        for (int i = 2; i <= level; i ++) {
            hp = Math.round(hp * 2 + hp / 2); //HP prethodnog bosa * 2 + HP prethodnog bosa / 2
        }
        return hp;
    }

    public int calculateCoins (int level) {
        double coins = 200;
        for (int i = 2; i <= level; i ++) {
            coins = coins * 1.2; //20% više novca nego kod prethodnog
        }
        return (int) coins;
    }

    public Boss addUpgradedBoss (Boss oldBoss) {
        Boss newBoss = new Boss();

        int level = oldBoss.getLevel() + 1;
        newBoss.setLevel(level);
        int maxHP = calculateMaxHp(level);
        newBoss.setMaxHp(maxHP);
        newBoss.setCurrentHp(maxHP);
        newBoss.setDefeated(false);

        return add(newBoss);
    }

    public Boss update (Boss boss) {
        return bossRepository.update(boss);
    }
}
