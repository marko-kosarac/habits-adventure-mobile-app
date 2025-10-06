package com.example.mobilnaaplikacija.services;

import com.example.mobilnaaplikacija.repository.*;

public class BossService {
    private final BossRepository bossRepository;

    public BossService(BossRepository bossRepository) {
        this.bossRepository = bossRepository;
    }

    public int calculateHp (int level) {
        if (level == 1) return 200;
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


//    public Battle fightBoss(String userId, String bossId) {
//        //UserStats user = userRepository.findById(userId);
//        //Boss boss = bossRepository.findById(bossId);
//        //if (user == null || boss == null) return null;
//
//        List<Attack> attacks = new ArrayList<>();
//
//        for (int i = 1; i <= 5; i++) {
//            //boolean hit = BattleUtils.attackHits(user.getSuccessRate());
//            //double damage = hit ? user.getPp() : 0;
//            //if (hit) boss.takeDamage(damage);
//
//            //attacks.add(new Attack(i, hit, damage));
//            //if (boss.isDefeated()) break;
//        }
//
//        //int coins = calculateCoinsEarned(boss.isDefeated(), boss.getCurrentHp(), boss.getMaxHp());
//        //user.addCoins(coins);
//
//        // persist state
//        //bossRepository.save(boss);
//        //userRepository.save(user);
//
//        //return new Battle(boss.isDefeated(), boss.getCurrentHp(), coins, attacks);
//    }
//
//    private int calculateCoinsEarned(boolean defeated, double currentHp, double maxHp) {
//        if (defeated) return 200;
//        else if (currentHp <= maxHp / 2) return 100;
//        return 0;
//    }
}
