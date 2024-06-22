package com.example.simplenote.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.simplenote.Models.NoteCardModel;
import com.example.simplenote.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NotesViewHolder> {

    private List<NoteCardModel> notes;
    private Context context;
    private OnNoteClickListener onNoteClickListener;

    public NotesAdapter(List<NoteCardModel> notes, Context context, OnNoteClickListener onNoteClickListener) {
        this.notes = notes;
        this.context = context;
        this.onNoteClickListener = onNoteClickListener;
        sortNotes(); // Sort notes initially
    }

    public void setNotes(List<NoteCardModel> notes) {
        this.notes = notes;
        sortNotes();
        notifyDataSetChanged();
    }

    private void sortNotes() {
        Collections.sort(notes, new Comparator<NoteCardModel>() {
            @Override
            public int compare(NoteCardModel o1, NoteCardModel o2) {
                return Boolean.compare(o2.isPinned(), o1.isPinned()); // Pinned notes first
            }
        });
    }

    @NonNull
    @Override
    public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.notes_card, parent, false);
        return new NotesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotesViewHolder holder, int position) {
        NoteCardModel note = notes.get(position);
        holder.textTitleView.setText(note.getTitle());
        holder.textDescriptionView.setText(note.getDescription());
        holder.pinButton.setImageResource(note.isPinned() ? R.drawable.baseline_push_pin_24_filled : R.drawable.outline_push_pin_24_outline);
        holder.cardView.setCardBackgroundColor(getRandomColorCode());
        holder.itemView.setOnClickListener(v -> {
            if (onNoteClickListener != null) {
                onNoteClickListener.onNoteClick(position);
            }
        });

        holder.pinButton.setOnClickListener(v -> {
            note.setPinned(!note.isPinned());
            updatePinState(note);
            sortNotes(); // Re-sort notes when pin state changes
            notifyDataSetChanged(); // Notify adapter to refresh the views
            if (onNoteClickListener != null) {
                onNoteClickListener.onNotePinnedChanged();
            }
        });

        holder.cardView.setOnClickListener(v -> {
            if (onNoteClickListener != null) {
                onNoteClickListener.onNoteClick(position);
            }
        });

        applyFontSize(holder.textTitleView);
        applyFontSize(holder.textDescriptionView);
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    private void updatePinState(NoteCardModel note) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).collection("notes").document(note.getNoteId())
                .update("pinned", note.isPinned())
                .addOnSuccessListener(aVoid ->
                {
                    // Handle success
                    note.setPinned(note.isPinned());
                    notifyDataSetChanged();
                    Toast.makeText(context, "Pin state updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    note.setPinned(!note.isPinned());
                    notifyDataSetChanged();
                    e.printStackTrace();
                });
    }
    public interface OnNoteClickListener {
        void onNoteClick(int position);
        void onNotePinnedChanged(); // Notify the fragment to refresh data
    }

    public class NotesViewHolder extends RecyclerView.ViewHolder {
        TextView textTitleView, textDescriptionView;
        ImageButton pinButton;
        CardView cardView;

        public NotesViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitleView = itemView.findViewById(R.id.textViewTitle);
            textDescriptionView = itemView.findViewById(R.id.textViewDescription);
            pinButton = itemView.findViewById(R.id.buttonPin);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }

    private void applyFontSize(TextView textView) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String fontSize = sharedPreferences.getString("font_size", "medium");

        if (fontSize.equals("small")) {
            textView.setTextAppearance(R.style.TextAppearance_AppCompat_Small);
        } else if (fontSize.equals("medium")) {
            textView.setTextAppearance(R.style.TextAppearance_AppCompat_Medium);
        } else if (fontSize.equals("large")) {
            textView.setTextAppearance(R.style.TextAppearance_AppCompat_Large);
        } else {
            textView.setTextAppearance(R.style.TextAppearance_AppCompat_Medium); // Default to medium size
        }
    }


    public int getRandomColorCode(){

        Random random = new Random();

        return Color.argb(255, random.nextInt(256), random.nextInt(256),     random.nextInt(256));

    }



}
