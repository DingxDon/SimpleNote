package com.example.simplenote;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.example.simplenote.Fragments.HomeFragment;
import com.example.simplenote.Fragments.LoginFragment;
import com.example.simplenote.Fragments.SettingsFragment;
import com.example.simplenote.Fragments.TrashFragment;
import com.example.simplenote.Models.NoteCardModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private FloatingActionButton fab;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private FirebaseAuth mAuth;
    private HomeFragment homeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        applyTheme();


        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize the views
        initializeViews();

        // Check if the user is logged in or not
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            showLoginFragment();
        } else {
            showHomeFragment();
            // Populate the tags
            populateTags();
        }
        setupDrawerToggle();



    }

    private void populateTags() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) return;

        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).collection("notes")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Set<String> tags = new HashSet<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            NoteCardModel note = document.toObject(NoteCardModel.class); // Convert snapshot to NoteCardModel
                            if (note.getTags() != null) {
                                tags.addAll(note.getTags());
                            }
                        }
                        updateNavigationMenu(tags);
                    } else {
                        Toast.makeText(this, "Failed to load Tags", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void updateNavigationMenu(Set<String> tags) {
        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        MenuItem tagMenuItem = menu.findItem(R.id.nav_tag_group);
        if (tagMenuItem != null) {
            SubMenu tagSubMenu = tagMenuItem.getSubMenu();
            if (tagSubMenu != null) {
                tagSubMenu.clear();
                for (String tag : tags) {
                    tagSubMenu.add(R.id.nav_tag_group, Menu.NONE, Menu.NONE, tag)
                            .setIcon(R.drawable.baseline_checklist_24);
                }
            }
        }
    }
    private void setupDrawerToggle() {
        drawerLayout = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        Log.d("MainActivity", "Setup drawer toggle");
    }
    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);


        // Set up the nav buttons and listeners
        navigationView.setNavigationItemSelectedListener(v -> {
            int id = v.getItemId();
            if (id == R.id.nav_all_notes) {
                showHomeFragment();
            } else if (id == R.id.nav_trash) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new TrashFragment())
                        .addToBackStack(null)
                        .commit();
            } else if (id == R.id.nav_settings) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new SettingsFragment())
                        .addToBackStack(null)
                        .commit();
            } else {
                MenuItem item = navigationView.getMenu().findItem(R.id.nav_tag_group);
                String tag = item.getTitle().toString();
                showTaggedNotes(tag);

            }
            drawerLayout.closeDrawer(navigationView);
            return true;
        });

    }



    // Add a method to show notes tagged with a specific tag
    private void showTaggedNotes(String tag) {
        HomeFragment homeFragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString("tag", tag);
        homeFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, homeFragment)
                .commit();

    }





    // Add a method to open the LoginFragment
    private void showLoginFragment() {

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new LoginFragment())
                .commit();

        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    // Add a method to open the HomeFragment
    private void showHomeFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    // Add a method to logout
    public void logout() {
        FirebaseAuth.getInstance().signOut();
        showLoginFragment();
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Apply theme when the activity resumes
        applyTheme();
    }

    private void applyTheme() {
        // Get the selected theme preference
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String themeValue = sharedPreferences.getString("theme", "light");

        // Apply the selected theme
        if (themeValue.equals("light")) {
            setTheme(R.style.Theme_SimpleNote_Light);
        } else if (themeValue.equals("dark")) {
            setTheme(R.style.Theme_SimpleNote_Dark);
        } else {
            setTheme(R.style.Theme_SimpleNote_Light);
        }
    }





}
