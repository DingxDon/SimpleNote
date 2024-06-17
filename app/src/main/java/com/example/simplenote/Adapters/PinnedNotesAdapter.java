package com.example.simplenote.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.simplenote.Models.NoteCardModel;
import com.example.simplenote.R;

import java.util.List;

public class PinnedNotesAdapter extends RecyclerView.Adapter<PinnedNotesAdapter.PinnedNotesViewHolder> {

    private List<NoteCardModel> pinnedNotes;
    private Context context;
    private OnNoteClickListener onNoteClickListener;

    public PinnedNotesAdapter(List<NoteCardModel> pinnedNotes, Context context, OnNoteClickListener onNoteClickListener) {
        this.pinnedNotes = pinnedNotes;
        this.context = context;
        this.onNoteClickListener = onNoteClickListener;
    }

    @NonNull
    @Override
    public PinnedNotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.notes_card, parent, false);
        return new PinnedNotesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PinnedNotesViewHolder holder, int position) {
        NoteCardModel note = pinnedNotes.get(position);
        holder.textTitleView.setText(note.getTitle());
        holder.textDescriptionView.setText(note.getDescription());

        holder.itemView.setOnClickListener(v -> {
            if (note.isPinned() && onNoteClickListener != null) {
                onNoteClickListener.onNoteClick(position);
            }
        });

        holder.pinButton.setOnClickListener(v -> {
            note.setPinned(!note.isPinned());
            notifyItemChanged(position);
        });


        holder.pinButton.setOnClickListener(v -> {
            note.setPinned(!note.isPinned());
            notifyItemChanged(position);
        });

    }

    @Override
    public int getItemCount() {
        return pinnedNotes.size();
    }

    public interface OnNoteClickListener {
        void onNoteClick(int position);
    }

    public class PinnedNotesViewHolder extends RecyclerView.ViewHolder {
        TextView textTitleView, textDescriptionView;
        ImageButton pinButton;


        public PinnedNotesViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitleView = itemView.findViewById(R.id.textViewTitle);
            textDescriptionView = itemView.findViewById(R.id.textViewDescription);
            pinButton = itemView.findViewById(R.id.buttonPin);
        }
    }
}
