package com.example.mobilnaaplikacija.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.NavOptions;

import com.example.mobilnaaplikacija.MainActivity;
import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.databinding.FragmentHomeBinding;
import com.example.mobilnaaplikacija.services.UserService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private Button loginButton;
    private EditText emailInput, passwordInput;
    private FirebaseAuth mAuth;
    private UserService userService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        loginButton = binding.buttonLogin;
        emailInput = binding.editTextEmail;
        passwordInput = binding.editTextPassword;
        mAuth = FirebaseAuth.getInstance();
        userService = new UserService();

        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(getActivity(), "Email je obavezan", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(getActivity(), "Lozinka je obavezna", Toast.LENGTH_SHORT).show();
                return;
            }

            userService.login(email, password, (success, message) -> {
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();

                if (success) {
                    FirebaseUser fbUser = mAuth.getCurrentUser();
                    if (fbUser != null) {
                        // Promeni meni drawera na logged_in verziju
                        MainActivity activity = (MainActivity) getActivity();
                        if (activity != null) {
                            activity.setMainDrawer();
                        }

                        // Navigacija na MainFragment
                        NavController navController = Navigation.findNavController(v);
                        navController.navigate(R.id.mainFragment, null, new NavOptions.Builder()
                                .setPopUpTo(R.id.homeFragment, true)
                                .build());
                    }
                }
            });
        });

        TextView registerLink = binding.textRegisterRedirect;
        registerLink.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_homeFragment_to_registerFragment);
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
