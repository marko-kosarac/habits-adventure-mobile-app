package com.example.mobilnaaplikacija.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.NavOptions;

import com.example.mobilnaaplikacija.MainActivity;
import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private Button loginButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        loginButton = binding.buttonLogin; // dugme iz XML-a

        loginButton.setOnClickListener(v -> {
            // Dobavljamo referencu na MainActivity
            MainActivity activity = (MainActivity) getActivity();
            if (activity != null) {
                // Promeni meni drawera sa logged_out na main_drawer
                activity.setMainDrawer();
            }

            // Navigacija na MainFragment unutar istog MainActivity
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.mainFragment, null, new NavOptions.Builder()
                    .setPopUpTo(R.id.homeFragment, true) // uklanja HomeFragment iz back stack-a
                    .build());
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
