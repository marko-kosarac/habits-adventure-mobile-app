package com.example.mobilnaaplikacija.services;

import com.example.mobilnaaplikacija.model.SpecialMission;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AllianceService {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void startMission(String allianceId, String leaderId, List<String> members,
                             Runnable onSuccess, Runnable onFailure) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Nova misija
        String missionId = db.collection("alliances").document(allianceId)
                .collection("missions").document().getId();

        SpecialMission newMission = new SpecialMission(
                missionId,
                leaderId,
                true,
                false,
                members,
                System.currentTimeMillis(),
                0L
        );

        Map<String, Object> missionMap = new HashMap<>();
        missionMap.put("id", newMission.getId());
        missionMap.put("leaderId", newMission.getLeaderId());
        missionMap.put("isStarted", true);
        missionMap.put("isDone", false);
        missionMap.put("members", newMission.getMembers());
        missionMap.put("startTime", newMission.getStartTime());
        missionMap.put("endTime", 0L);

        // 🔹 Čuvanje u podkolekciju "missions"
        db.collection("alliances")
                .document(allianceId)
                .collection("missions")
                .document(missionId)
                .set(missionMap)
                .addOnSuccessListener(aVoid -> {
                    // Takođe ažuriramo "activeMission" polje
                    db.collection("alliances").document(allianceId)
                            .update("activeMission", missionMap)
                            .addOnSuccessListener(v -> onSuccess.run())
                            .addOnFailureListener(e -> onFailure.run());
                })
                .addOnFailureListener(e -> onFailure.run());
    }


    public void endMission(String allianceId, Runnable onSuccess, Runnable onFailure) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("alliances").document(allianceId)
                .get()
                .addOnSuccessListener(allianceDoc -> {
                    Map<String, Object> activeMission = (Map<String, Object>) allianceDoc.get("activeMission");
                    if (activeMission == null) {
                        onFailure.run();
                        return;
                    }

                    String missionId = (String) activeMission.get("id");
                    if (missionId == null) {
                        onFailure.run();
                        return;
                    }

                    long endTime = System.currentTimeMillis();
                    activeMission.put("isStarted", false);
                    activeMission.put("isDone", true);
                    activeMission.put("endTime", endTime);

                    // 🔹 Ažuriraj aktivnu misiju
                    db.collection("alliances").document(allianceId)
                            .update("activeMission", null)
                            .addOnSuccessListener(aVoid -> {
                                // 🔹 Ažuriraj dokument misije u istoriji
                                db.collection("alliances")
                                        .document(allianceId)
                                        .collection("missions")
                                        .document(missionId)
                                        .update(activeMission)
                                        .addOnSuccessListener(v -> onSuccess.run())
                                        .addOnFailureListener(e -> onFailure.run());
                            })
                            .addOnFailureListener(e -> onFailure.run());
                })
                .addOnFailureListener(e -> onFailure.run());
    }

}
