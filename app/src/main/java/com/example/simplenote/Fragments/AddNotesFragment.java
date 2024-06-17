package com.example.simplenote.Fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.simplenote.Models.NoteCardModel;
import com.example.simplenote.R;
import com.example.simplenote.databinding.FragmentAddNotesBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Date;
import java.util.Arrays;

public class AddNotesFragment extends Fragment {

    private FragmentAddNotesBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentTitle = "";
    private String currentDescription = "";
    private String currentTags = "";
    private boolean isDataChanged = false;

    private androidx.appcompat.widget.Toolbar toolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        binding = FragmentAddNotesBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        EditText editTextDescription = rootView.findViewById(R.id.editTextDescription);
        EditText editTextTags = rootView.findViewById(R.id.editTextTags);

        editTextDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                isDataChanged = true;
                currentDescription = s.toString().trim();
                // Split description into lines
                String[] lines = currentDescription.split("\n", 2);
                currentTitle = lines[0].trim(); // Set the first line as the title
                currentDescription = lines.length > 1 ? lines[1].trim() : ""; // Set the second line as description
            }
        });

        editTextTags.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                isDataChanged = true;
                currentTags = s.toString().trim();
            }
        });

        toolbar = rootView.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.toolbar_menu);
        toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
        toolbar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_checklist) {
                createChecklist();
                return true;
            } else if (itemId == R.id.action_info) {
                showInfo();
                return true;
            } else if (itemId == R.id.action_more) {
                PopupMenu popupMenu = new PopupMenu(getContext(), toolbar.findViewById(R.id.action_more));
                popupMenu.getMenuInflater().inflate(R.menu.add_notes_vert_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(subItem -> {
                    if (subItem.getItemId() == R.id.action_delete) {
                        moveToTrash();
                        navigateToTrash();
                        return true;
                    } else if (subItem.getItemId() == R.id.action_pin) {
                        // Handle pin action
                        NoteCardModel clickedNote = getArguments().getParcelable("clicked_note");
                        assert clickedNote != null;
                        clickedNote.setPinned(!clickedNote.isPinned());

                        saveNote();
                        return true;
                    }
                    return false;
                });
                popupMenu.show();
                return true;
            }
            return false;
        });

        Bundle args = getArguments();
        if (args != null && args.containsKey("clicked_note")) {
            NoteCardModel clickedNote = args.getParcelable("clicked_note");
            if (clickedNote != null) {
                editTextDescription.setText(clickedNote.getDescription());
                editTextTags.setText(TextUtils.join(",", clickedNote.getTags()));
                // Manually trigger text change listener to set initial values
                editTextDescription.post(() -> editTextDescription.setText(String.format("%s\n%s", clickedNote.getTitle(), clickedNote.getDescription())));
                editTextTags.post(() -> editTextTags.setText(TextUtils.join(",", clickedNote.getTags())));
            }
        }

        return rootView;
    }

    private void navigateToTrash() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new TrashFragment())
                    .commit();
        }
    }

    private void moveToTrash() {
        String noteId = getArguments().getString("noteId");

        if (noteId == null) {
            showToast("NoteId is null. Unable to move note to trash");
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users")
                .document(userId)
                .collection("notes")
                .document(noteId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        db.collection("users")
                                .document(userId)
                                .collection("deleted_notes")
                                .document(noteId)
                                .set(documentSnapshot.getData())
                                .addOnSuccessListener(aVoid -> {
                                    db.collection("users")
                                            .document(userId)
                                            .collection("notes")
                                            .document(noteId)
                                            .delete()
                                            .addOnSuccessListener(aVoid1 -> {
                                                showToast("Note moved to trash successfully");
                                                navigateToTrash();
                                            })
                                            .addOnFailureListener(e -> showToast("Failed to move note to trash"));
                                })
                                .addOnFailureListener(e -> showToast("Failed to move note to trash"));
                    } else {
                        showToast("Note not found in 'notes' collection");
                    }
                })
                .addOnFailureListener(e -> showToast("Error fetching note: " + e.getMessage()));
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        } else {
            Log.e("AddNotesFragment", "Context is null. Unable to show toast.");
        }
    }

    private void navigateToHomeFragment() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
    }

    private void createChecklist() {
        String[] lines = currentDescription.split("\n");
        StringBuilder checklist = new StringBuilder();
        for (String line : lines) {
            checklist.append("- ").append(line).append("\n");
        }
        EditText editTextDescription = binding.editTextDescription;
        editTextDescription.setText(checklist.toString());
    }

    private void showInfo() {
        EditText editTextDescription = binding.editTextDescription;
        String text = editTextDescription.getText().toString();
        int wordCount = text.trim().split("\\s+").length;
        int charCount = text.length();

        String info = "Words: " + wordCount + "\nCharacters: " + charCount;
        Toast.makeText(getContext(), info, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPause() {
        super.onPause();
        saveNote();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void saveNote() {
        if (!isDataChanged || mAuth.getCurrentUser() == null || getActivity() == null || getContext() == null) return;

        String userId = mAuth.getCurrentUser().getUid();
        Date date = new Date(System.currentTimeMillis());
        String[] tagsArray = currentTags.split(",");

        if (currentTitle.isEmpty()) {
            currentTitle = "Untitled";
        } else {
            currentTitle = currentTitle.trim();
        }
        if (currentDescription.isEmpty()) {
            currentDescription = "";
        } else {
            currentDescription = currentDescription.trim();
        }

        Bundle args = getArguments();
        if (args != null && args.containsKey("clicked_note")) {
            NoteCardModel clickedNote = args.getParcelable("clicked_note");
            if (clickedNote != null) {
                clickedNote.setTitle(currentTitle);
                clickedNote.setDescription(currentDescription);
                clickedNote.setTags(Arrays.asList(tagsArray));
                clickedNote.setDate(date);

                db.collection("users").document(userId).collection("notes")
                        .document(clickedNote.getNoteId())
                        .set(clickedNote.toMap())
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                showToast("Note updated successfully");
                                navigateToHomeFragment();
                            } else {
                                showToast("Failed to update note");
                            }
                        });
            } else {
                showToast("Clicked note is null. Unable to update note");
            }
        } else {
            NoteCardModel note = new NoteCardModel(null, currentTitle, currentDescription, false, false, date, Arrays.asList(tagsArray));

            db.collection("users").document(userId).collection("notes")
                    .add(note.toMap())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            String newNoteId = task.getResult().getId();
                            note.setNoteId(newNoteId);

                            db.collection("users").document(userId).collection("notes").document(newNoteId)
                                    .set(note.toMap())
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            showToast("Note saved successfully");
                                            navigateToHomeFragment();
                                        } else {
                                            showToast("Failed to save note");
                                        }
                                    });
                        } else {
                            showToast("Failed to save note");
                        }
                    });
        }
    }
}
