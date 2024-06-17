package com.example.simplenote.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.simplenote.Models.NoteCardModel;
import com.example.simplenote.R;

import java.util.List;

public class TrashAdapter extends RecyclerView.Adapter<TrashAdapter.TrashViewHolder> {

    private List<NoteCardModel> deletedNotesList;
    private OnDeleteClickListener onDeleteClickListener; // Interface to handle delete click events

    public TrashAdapter(List<NoteCardModel> deletedNotesList, OnDeleteClickListener onDeleteClickListener) {
        this.deletedNotesList = deletedNotesList;
        this.onDeleteClickListener = onDeleteClickListener;
    }

    @NonNull
    @Override
    public TrashViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.deleted_note, parent, false);
        return new TrashViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrashViewHolder holder, int position) {
        NoteCardModel note = deletedNotesList.get(position);

        holder.textViewTitle.setText(note.getTitle());
        holder.textViewDescription.setText(note.getDescription());

        holder.buttonDelete.setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick(note);
            }
        });
    }

    @Override
    public int getItemCount() {
        return deletedNotesList.size();
    }

    public static class TrashViewHolder extends RecyclerView.ViewHolder {
        private androidx.appcompat.widget.AppCompatTextView textViewTitle;
        private androidx.appcompat.widget.AppCompatTextView textViewDescription;
        private androidx.appcompat.widget.AppCompatImageButton buttonDelete;

        public TrashViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(NoteCardModel note);
    }
}
