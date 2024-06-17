package com.example.simplenote.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.example.simplenote.R;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends PreferenceFragmentCompat {

    private Toolbar toolbar;


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
        LoginFragment loginFragment = new LoginFragment();
        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, loginFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
