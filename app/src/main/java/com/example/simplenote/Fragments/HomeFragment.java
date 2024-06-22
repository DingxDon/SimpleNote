package com.example.simplenote.Fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.simplenote.Adapters.NotesAdapter;
import com.example.simplenote.Models.NoteCardModel;
import com.example.simplenote.R;
import com.example.simplenote.databinding.FragmentHomeBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HomeFragment extends Fragment implements NotesAdapter.OnNoteClickListener {

    private Toolbar toolbar;
    private FloatingActionButton fab;
    private FragmentHomeBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ArrayList<NoteCardModel> notesList;
    private NotesAdapter adapter;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;
    private String currentFilter = null;
    private String filterTag = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        toolbar = binding.homeToolbar;
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
        initializeFirebase();

        initializeViews();

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        notesList = new ArrayList<>();
        adapter = new NotesAdapter(notesList, getContext(), this);

        //display as GridView notes
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));


        recyclerView.setAdapter(adapter);




        Bundle args = getArguments();
        if (args != null && args.containsKey("tag")) {
            filterTag = args.getString("tag");
            filterNotesByTag(filterTag);
        } else {
            // If no tag is provided, show all notes

            loadNotes();
        }


        String fontSizePreference = PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getString("font_size", "medium");



        return view;
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void filterNotesByTag(String tag) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId).collection("notes")
                .whereArrayContains("tags", tag)  // Corrected query to filter by tag
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<NoteCardModel> filteredNotes = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            NoteCardModel note = NoteCardModel.fromMap(document.getData());
                            filteredNotes.add(note);
                        }
                        displayFilteredNotes(filteredNotes);
                    } else {
                        Toast.makeText(getContext(), "Failed to fetch notes: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayFilteredNotes(List<NoteCardModel> filteredNotes) {
        notesList.clear();  // Clear existing notesList
        notesList.addAll(filteredNotes);  // Add filtered notes to notesList
        adapter.notifyDataSetChanged();  // Notify adapter of data change
        currentFilter = filterTag;  // Update current filter

    }


    private void initializeViews() {
        drawerLayout = requireActivity().findViewById(R.id.drawer_layout);
        navigationView = requireActivity().findViewById(R.id.nav_view);

        actionBarDrawerToggle = new ActionBarDrawerToggle(requireActivity(), drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        toolbar.setNavigationIcon(R.drawable.baseline_menu_24);
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(navigationView));


        fab = binding.fab;
        fab.setVisibility(View.VISIBLE);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        loadNotes();

        navigationView.setCheckedItem(R.id.nav_all_notes);

        fab.setOnClickListener(v -> openAddNotesFragment());
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Search notes...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void loadNotes() {
        if (mAuth == null || mAuth.getCurrentUser() == null) {
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).collection("notes")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        notesList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            NoteCardModel note = NoteCardModel.fromMap(document.getData());
                            note.setNoteId(document.getId()); // Set the noteId from the document ID
                            selectRandomColor();
                            // Filter notes based on tags
                            if (currentFilter == null ||
                                    (note.getTags() != null && note.getTags().contains(currentFilter)) ||
                                    (currentFilter != null && note.getTags() == null)) {
                                notesList.add(note);}

                        }
                        notesList.sort((o1, o2) -> Boolean.compare(o2.isPinned(), o1.isPinned()));
                        adapter.setNotes(notesList);
                                            } else {
                        Toast.makeText(getContext(), "Error loading notes", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    @Override
    public void onNotePinnedChanged() {
        loadNotes(); // Reload notes when pin state changes
    }
    // Random Color Selector for notes
    private void selectRandomColor() {
        CardView cardView = requireActivity().findViewById(R.id.cardView);
        if (cardView != null) {
            cardView.setCardBackgroundColor(getRandomColor());
        } else {
            Toast.makeText(getContext(), "Error searching notes", Toast.LENGTH_SHORT).show();
        }
    }

    private int getRandomColor() {

        Random random = new Random();
        return Color.argb(255, random.nextInt(256), random.nextInt(256),     random.nextInt(256));    }


    private void performSearch(String query) {
        if (mAuth.getCurrentUser() == null) return;

        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).collection("notes")
                .orderBy("title")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        notesList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            NoteCardModel note = NoteCardModel.fromMap(document.getData());
                            notesList.add(note);

                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "Error searching notes", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void setFilter(String filter) {
        this.currentFilter = filter;
        loadNotes();
    }


    private void openAddNotesFragment() {
        AddNotesFragment addNotesFragment = new AddNotesFragment();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        transaction.replace(R.id.fragment_container, addNotesFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    @Override
    public void onNoteClick(int position) {
        if (position < notesList.size()) {
            openAddNotesFragment(notesList.get(position));
        }
    }


    private void openAddNotesFragment(NoteCardModel note) {
        // Open AddNotesFragment with the clicked note details
        AddNotesFragment addNotesFragment = new AddNotesFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("clicked_note", note);
        bundle.putString("noteId", note.getNoteId());
        addNotesFragment.setArguments(bundle);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        transaction.replace(R.id.fragment_container, addNotesFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }



}
