package com.example.mobilnaaplikacija.services.battle;

import android.content.Context;

import com.example.mobilnaaplikacija.database.SQLiteHelper;
import com.example.mobilnaaplikacija.model.Attack;
import com.example.mobilnaaplikacija.repository.AttackRepository;
import com.example.mobilnaaplikacija.services.task.TaskService;

import java.util.List;

public class AttackService {
    private AttackRepository attackRepository;
    private TaskService taskService;
    public AttackService(Context context) {
        this.attackRepository = new AttackRepository(new SQLiteHelper(context));
        this.taskService = new TaskService(context);
    }

    public List<Attack> getAttacksByUserAndBoss (String userId, String bossId) {
        return attackRepository.getAttacksByUserAndBoss(userId, bossId);
    }

    public boolean deleteById (String id) {
        return attackRepository.delete(id) > 0;
    }

    public Attack add (Attack attack) {
        return attackRepository.add(attack);
    }
}
