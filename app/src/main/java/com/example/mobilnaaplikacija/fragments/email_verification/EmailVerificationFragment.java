package com.example.mobilnaaplikacija.fragments.email_verification;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class EmailVerificationFragment extends Fragment {

    private static final String TAG = "EmailVerification";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        Intent intent = getActivity().getIntent();
        String action = intent.getAction();
        Uri data = intent.getData();

        if (action != null && data != null && action.equals(Intent.ACTION_VIEW)) {
            String oobCode = data.getQueryParameter("oobCode");

            if (oobCode != null) {
                auth.applyActionCode(oobCode)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = auth.getCurrentUser();
                                if (user != null) {
                                    updateRegistrationConfirmed(user.getUid());
                                } else {
                                    Log.e(TAG, "No user signed in.");
                                    Toast.makeText(getActivity(), "No user signed in.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e(TAG, "Error applying action code: " + task.getException());
                                Toast.makeText(getActivity(), "Invalid or expired action code.", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Log.e(TAG, "No oobCode found in the URL.");
                Toast.makeText(getActivity(), "No oobCode found in the URL.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateRegistrationConfirmed(String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(uid)
                .update("isRegistrationConfirmed", true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), "Email verified successfully.", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "Error updating registration confirmation: " + task.getException());
                        Toast.makeText(getActivity(), "Failed to update registration confirmation.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
