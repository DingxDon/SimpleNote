package com.example.simplenote.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.simplenote.R;

import java.util.List;

public class ChecklistAdapter extends RecyclerView.Adapter<ChecklistAdapter.ChecklistViewHolder> {

    private List<String> checklistItems;

    public ChecklistAdapter(List<String> checklistItems) {
        this.checklistItems = checklistItems;
    }

    @NonNull
    @Override
    public ChecklistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checklist, parent, false);
        return new ChecklistViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ChecklistViewHolder holder, int position) {
        String item = checklistItems.get(position);
        holder.editTextChecklistItem.setText(item);
        holder.checkBox.setChecked(item.startsWith("[x] "));
    }

    @Override
    public int getItemCount() {
        return checklistItems.size();
    }

    public static class ChecklistViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        EditText editTextChecklistItem;

        public ChecklistViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox);
            editTextChecklistItem = itemView.findViewById(R.id.editTextChecklistView);

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    editTextChecklistItem.setText("[x] " + editTextChecklistItem.getText().toString());
                } else {
                    editTextChecklistItem.setText(editTextChecklistItem.getText().toString().replace("[x] ", ""));
                }
            });
        }
    }
}
