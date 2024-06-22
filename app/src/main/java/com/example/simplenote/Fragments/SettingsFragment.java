package com.example.simplenote.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.example.simplenote.R;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        Toolbar toolbar = view.findViewById(R.id.settings_toolbar);
        if (toolbar != null) {
            toolbar.setTitle("Settings");
            toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24);
            toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
        } else {
            Toast.makeText(requireContext(), "Toolbar not found", Toast.LENGTH_SHORT).show();
        }


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new InnerSettingsFragment())
                .commit();
    }
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Empty implementation to satisfy the abstract method requirement.
        // Preferences setup is delegated to InnerSettingsFragment.
    }

    public static class InnerSettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
            ListPreference themePreference = findPreference("theme");
            if (themePreference != null) {
                themePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                    applyTheme((String) newValue);
                    return true;
                });
            }

            ListPreference fontSizePreference = findPreference("font_size");
            if (fontSizePreference != null) {
                fontSizePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                    applyFontSize((String) newValue);
                    return true;
                });
            }

            Preference logoutPreference = findPreference("logout");
            if (logoutPreference != null) {
                logoutPreference.setOnPreferenceClickListener(preference -> {
                    performLogout();
                    return true;
                });
            }
        }

        private void applyTheme(String theme) {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(requireContext()).edit();
            editor.putString("theme", theme);
            editor.apply();
            requireActivity().recreate();
        }

        private void applyFontSize(String fontSize) {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(requireContext()).edit();
            editor.putString("font_size", fontSize);
            editor.apply();
            requireActivity().recreate();
        }

        private void performLogout() {
            FirebaseAuth.getInstance().signOut();
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new LoginFragment());
            transaction.addToBackStack(null);

            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
            transaction.commit();
        }
    }
}
