package com.example.mobilnaaplikacija.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mobilnaaplikacija.services.UserService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.example.mobilnaaplikacija.R;

public class RegisterFragment extends Fragment {

    private EditText usernameField, emailField, passwordField, confirmPasswordField;
    private GridLayout avatarGrid;
    private Button registerButton;
    private UserService userService;
    private int selectedAvatar = -1;

    public RegisterFragment() {
        userService = new UserService();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_register, container, false);

        usernameField = root.findViewById(R.id.editTextUsername);
        emailField = root.findViewById(R.id.editTextEmail);
        passwordField = root.findViewById(R.id.editTextPassword);
        confirmPasswordField = root.findViewById(R.id.editTextConfirmPassword);
        avatarGrid = root.findViewById(R.id.avatarGrid);
        registerButton = root.findViewById(R.id.buttonRegister);

        setupAvatarSelection(root);

        registerButton.setOnClickListener(v -> registerUser());

        return root;
    }

    private void setupAvatarSelection(View root) {
        int[] avatarIds = {R.id.avatar1, R.id.avatar2, R.id.avatar3, R.id.avatar4, R.id.avatar5};

        for (int i = 0; i < avatarIds.length; i++) {
            int avatarIndex = i + 1; // avatarId 1–5
            ImageView avatar = root.findViewById(avatarIds[i]);
            avatar.setOnClickListener(v -> {
                selectedAvatar = avatarIndex;
                Toast.makeText(getActivity(), "Izabran avatar " + avatarIndex, Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void registerUser() {
        String username = usernameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String confirmPassword = confirmPasswordField.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(getActivity(), "Popunite sva polja.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(getActivity(), "Lozinke se ne poklapaju.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(getActivity(), "Lozinka mora imati najmanje 6 karaktera.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedAvatar == -1) {
            Toast.makeText(getActivity(), "Izaberite avatar.", Toast.LENGTH_SHORT).show();
            return;
        }

        userService.registerUser(email, password, username, selectedAvatar, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = task.getResult().getUser();
                    if (firebaseUser != null) {
                        userService.saveUserData(firebaseUser.getUid(), email, username, selectedAvatar);
                        userService.sendVerificationEmail(firebaseUser);

                        Toast.makeText(getActivity(), "Registracija uspešna. Proverite email za verifikaciju.", Toast.LENGTH_LONG).show();
                        userService.logout();
                    }
                } else {
                    Toast.makeText(getActivity(), "Greška: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
