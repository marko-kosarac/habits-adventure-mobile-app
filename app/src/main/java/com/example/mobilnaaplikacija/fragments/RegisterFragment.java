package com.example.mobilnaaplikacija.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mobilnaaplikacija.R;

public class RegisterFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public RegisterFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        TextView textLoginRedirect = view.findViewById(R.id.textLoginRedirect);
        textLoginRedirect.setOnClickListener(v -> {
            NavHostFragment.findNavController(RegisterFragment.this)
                    .navigate(R.id.action_registerFragment_to_homeFragment);
        });

        return view;
    }
}