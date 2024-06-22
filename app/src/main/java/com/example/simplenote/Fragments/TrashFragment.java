package com.example.simplenote.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.simplenote.Adapters.TrashAdapter;
import com.example.simplenote.Models.NoteCardModel;
import com.example.simplenote.R;
import com.example.simplenote.databinding.FragmentTrashBinding;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;


public class TrashFragment extends Fragment {

    private FragmentTrashBinding binding;
    Toolbar trash_toolbar;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<NoteCardModel> deletedNotes;
    private TrashAdapter trashAdapter;

    private NavigationView navigationView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        binding = FragmentTrashBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        trash_toolbar = view.findViewById(R.id.toolbar_trash);
        trash_toolbar.setTitle("Trash");

        trash_toolbar.inflateMenu(R.menu.trash_menu);
        trash_toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24);

        trash_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().onBackPressed();
                onDestroy();
            }
        });

        Menu menu = trash_toolbar.getMenu();
        menu.findItem(R.id.action_delete_all).setVisible(false);


        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        RecyclerView trashRecyclerView = binding.trashRecyclerView;
        trashRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        deletedNotes = new ArrayList<>();
        trashAdapter = new TrashAdapter(deletedNotes, this::deleteNote);
        trashRecyclerView.setAdapter(trashAdapter);

        loadDeletedNotes();


        return view;



    }

    private void loadDeletedNotes() {
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .collection("deleted_notes") // Adjust collection name as per your Firestore structure
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    deletedNotes.clear();
                    for (NoteCardModel note : queryDocumentSnapshots.toObjects(NoteCardModel.class)) {
                        deletedNotes.add(note);
                    }
                    trashAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to load deleted notes", Toast.LENGTH_SHORT).show();
                    });
    }

    private void restoreDeletedNotes(NoteCardModel note) {
        String userId = mAuth.getCurrentUser().getUid();
        String deletedNoteId = note.getNoteId();

        db.collection("users").document(userId).collection("deleted_notes").document(deletedNoteId).delete()
                .addOnSuccessListener(unused -> {
                    db.collection("users").document(userId).collection("notes").document(deletedNoteId).set(note)
                            .addOnSuccessListener(unused1 -> {
                                deletedNotes.remove(note);
                                trashAdapter.notifyDataSetChanged();
                                Toast.makeText(getContext(), "Note restored successfully", Toast.LENGTH_SHORT).show();
                                loadDeletedNotes();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to restore note", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to delete note from trash", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteAllNotes(NoteCardModel note) {
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .collection("deleted_notes")
                .document(note.getNoteId())
                .delete()
                .addOnSuccessListener(unused -> {
                    deletedNotes.remove(note);
                    trashAdapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "All notes deleted successfully", Toast.LENGTH_SHORT).show();
                    loadDeletedNotes();
                    })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to delete all notes", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteNote(NoteCardModel note) {
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .collection("deleted_notes")
                .document(note.getNoteId())
                .delete()
                .addOnSuccessListener(unused -> {
                    deletedNotes.remove(note);
                    trashAdapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Note deleted successfully", Toast.LENGTH_SHORT).show();

                    loadDeletedNotes();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to delete note", Toast.LENGTH_SHORT).show();
                });

    }
}