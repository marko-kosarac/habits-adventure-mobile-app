package com.example.mobilnaaplikacija.services;

import android.content.Context;

import com.example.mobilnaaplikacija.database.SQLiteHelper;
import com.example.mobilnaaplikacija.model.Attack;
import com.example.mobilnaaplikacija.repository.AttackRepository;

import java.util.List;

public class AttackService {
    private AttackRepository attackRepository;
    public AttackService(Context context) {
        this.attackRepository = new AttackRepository(new SQLiteHelper(context));
    }

    public List<Attack> getAttacksByUser (String userId) {
        return attackRepository.getAttacksByUser(userId);
    }
}
