package com.example.mobilnaaplikacija.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


import com.example.mobilnaaplikacija.R;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

public class QrScanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("Skeniraj QR kod");
        integrator.setBeepEnabled(true);
        integrator.setOrientationLocked(false);
        integrator.setCaptureActivity(CaptureActivity.class); // koristi standardni skener
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            Intent intent = new Intent();
            if (result.getContents() != null) {
                intent.putExtra("scanned_data", result.getContents());
                setResult(Activity.RESULT_OK, intent);
            } else {
                setResult(Activity.RESULT_CANCELED);
            }
            finish();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}