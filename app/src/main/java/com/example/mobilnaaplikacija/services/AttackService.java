package com.example.mobilnaaplikacija.services;

import android.content.Context;
import android.util.Log;

import com.example.mobilnaaplikacija.database.SQLiteHelper;
import com.example.mobilnaaplikacija.model.Attack;
import com.example.mobilnaaplikacija.repository.AttackRepository;

import java.util.List;

public class AttackService {
    private AttackRepository attackRepository;
    private TaskService taskService;
    public AttackService(Context context) {
        this.attackRepository = new AttackRepository(new SQLiteHelper(context));
        this.taskService = new TaskService(context);
    }

    public List<Attack> getAttacksByUser (String userId) {
        return attackRepository.getAttacksByUser(userId);
    }

    public Attack add (Attack attack) {
        return attackRepository.add(attack);
    }
}
