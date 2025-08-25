package com.example.mobilnaaplikacija;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private TextView textRegisterRedirect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicijalizacija view-ova
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textRegisterRedirect = findViewById(R.id.textRegisterRedirect);

        // Login dugme
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();

                // Prosto validacija (kasnije možeš dodati Firebase ili lokalnu bazu)
                if(email.isEmpty() || password.isEmpty()){
                    Toast.makeText(LoginActivity.this, "Popunite sve podatke", Toast.LENGTH_SHORT).show();
                } else {
                    // Prelazak na MainActivity
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // zatvara LoginActivity
                }
            }
        });

        // Redirect na registraciju (za sad samo Toast)
        textRegisterRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(LoginActivity.this, "Otvori registraciju", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
