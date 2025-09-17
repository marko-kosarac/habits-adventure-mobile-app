package com.example.mobilnaaplikacija.fragments.alliance;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.adapters.AllianceMemberAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MyAllianceFragment extends Fragment {

    private TextView textAllianceName, textAllianceLeader, textMembersLabel;
    private Button btnDeleteAlliance, btnLeaveAlliance;
    private RecyclerView recyclerAllianceMembers;
    private AllianceMemberAdapter memberAdapter;
    private List<String> memberList = new ArrayList<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_alliance, container, false);

        textAllianceName = view.findViewById(R.id.textAllianceName);
        textAllianceLeader = view.findViewById(R.id.textAllianceLeader);
        recyclerAllianceMembers = view.findViewById(R.id.recyclerAllianceMembers);
        btnDeleteAlliance = view.findViewById(R.id.btnDeleteAlliance);
        btnLeaveAlliance = view.findViewById(R.id.btnLeaveAlliance);
        textMembersLabel = view.findViewById(R.id.textMembersLabel);

        recyclerAllianceMembers.setLayoutManager(new LinearLayoutManager(getContext()));
        memberAdapter = new AllianceMemberAdapter(memberList);
        recyclerAllianceMembers.setAdapter(memberAdapter);

        btnDeleteAlliance.setOnClickListener(v -> showDeleteConfirmation());
        btnLeaveAlliance.setOnClickListener(v -> showLeaveConfirmation());

        loadAllianceData();

        return view;
    }

    private void loadAllianceData() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    String allianceId = userDoc.getString("currentAllianceId");

                    if (allianceId != null && !allianceId.isEmpty()) {
                        db.collection("alliances").document(allianceId)
                                .get()
                                .addOnSuccessListener(allianceDoc -> {
                                    if (allianceDoc.exists()) {
                                        String allianceName = allianceDoc.getString("name");
                                        String leaderId = allianceDoc.getString("leaderId");
                                        List<String> members = (List<String>) allianceDoc.get("members");

                                        textAllianceName.setText(allianceName != null ? allianceName : "Savez");

                                        if (leaderId != null) {
                                            db.collection("users").document(leaderId)
                                                    .get()
                                                    .addOnSuccessListener(leaderDoc -> {
                                                        String leaderName = leaderDoc.getString("username");
                                                        textAllianceLeader.setText("Vođa: " + (leaderName != null ? leaderName : "Nepoznato"));
                                                    });
                                        }

                                        if (members != null) {
                                            memberList.clear();
                                            memberList.addAll(members);
                                            memberAdapter.notifyDataSetChanged();
                                        }
                                        textMembersLabel.setVisibility(View.VISIBLE);
                                        recyclerAllianceMembers.setVisibility(View.VISIBLE);

                                        // Prikaz dugmadi
                                        if (currentUserId.equals(leaderId)) {
                                            btnDeleteAlliance.setVisibility(View.VISIBLE);
                                            btnLeaveAlliance.setVisibility(View.GONE);
                                        } else {
                                            btnDeleteAlliance.setVisibility(View.GONE);
                                            btnLeaveAlliance.setVisibility(View.VISIBLE);
                                        }

                                    } else {
                                        clearAllianceUI();
                                    }
                                });
                    } else {
                        clearAllianceUI();
                    }
                });
    }

    private void clearAllianceUI() {
        textAllianceName.setText("Trenutno niste član nijednog saveza");
        textAllianceLeader.setText("");
        recyclerAllianceMembers.setVisibility(View.GONE);
        btnDeleteAlliance.setVisibility(View.GONE);
        btnLeaveAlliance.setVisibility(View.GONE);
        textMembersLabel.setVisibility(View.GONE);
        memberList.clear();
        memberAdapter.notifyDataSetChanged();
    }

    private void showLeaveConfirmation() {
        new AlertDialog.Builder(getContext())
                .setTitle("Napusti savez")
                .setMessage("Da li ste sigurni da želite napustiti savez?")
                .setPositiveButton("Da", (dialog, which) -> leaveAlliance())
                .setNegativeButton("Ne", null)
                .show();
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(getContext())
                .setTitle("Obriši savez")
                .setMessage("Da li ste sigurni da želite obrisati savez?")
                .setPositiveButton("Da", (dialog, which) -> deleteAlliance())
                .setNegativeButton("Ne", null)
                .show();
    }

    private void leaveAlliance() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    String allianceId = userDoc.getString("currentAllianceId");
                    if (allianceId != null) {
                        db.collection("alliances").document(allianceId)
                                .update("members", FieldValue.arrayRemove(currentUserId))
                                .addOnSuccessListener(aVoid -> db.collection("users").document(currentUserId)
                                        .update("currentAllianceId", null)
                                        .addOnSuccessListener(aVoid1 -> {
                                            Toast.makeText(getContext(), "Napustili ste savez.", Toast.LENGTH_SHORT).show();
                                            loadAllianceData();
                                        }))
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Greška pri napuštanju saveza.", Toast.LENGTH_SHORT).show());
                    }
                });
    }

    private void deleteAlliance() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    String allianceId = userDoc.getString("currentAllianceId");
                    if (allianceId != null) {
                        db.collection("alliances").document(allianceId)
                                .get()
                                .addOnSuccessListener(allianceDoc -> {
                                    if (allianceDoc.exists()) {
                                        List<String> members = (List<String>) allianceDoc.get("members");
                                        if (members != null) {
                                            for (String memberId : members) {
                                                db.collection("users").document(memberId)
                                                        .update("currentAllianceId", null);
                                            }
                                        }

                                        db.collection("alliances").document(allianceId)
                                                .delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(getContext(), "Savez je uspešno obrisan.", Toast.LENGTH_SHORT).show();
                                                    loadAllianceData();
                                                })
                                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Greška pri brisanju saveza.", Toast.LENGTH_SHORT).show());
                                    }
                                });
                    }
                });
    }
}
