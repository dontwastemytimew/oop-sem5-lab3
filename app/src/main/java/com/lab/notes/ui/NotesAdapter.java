package com.lab.notes.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.lab.notes.R;
import com.lab.notes.model.Note;

/**
 * Shows notes in a list and reports user actions on them.
 */
public class NotesAdapter extends ListAdapter<Note, NotesAdapter.NoteViewHolder> {

    /**
     * Receives user actions performed on a note in the list.
     */
    public interface OnNoteActionListener {

        void onNoteClicked(Note note);

        void onNoteLongClicked(Note note);
    }

    private static final DiffUtil.ItemCallback<Note> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Note>() {
                @Override
                public boolean areItemsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
                    return oldItem.equals(newItem);
                }
            };

    private final OnNoteActionListener listener;

    public NotesAdapter(OnNoteActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    /**
     * Holds the views of one list row.
     */
    static class NoteViewHolder extends RecyclerView.ViewHolder {

        private final TextView titleText;
        private final TextView contentText;

        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.noteTitleText);
            contentText = itemView.findViewById(R.id.noteContentText);
        }

        void bind(Note note, OnNoteActionListener listener) {
            String title = note.getTitle().isEmpty()
                    ? itemView.getContext().getString(R.string.untitled_note)
                    : note.getTitle();
            titleText.setText(title);
            contentText.setText(note.getContent());
            itemView.setOnClickListener(view -> listener.onNoteClicked(note));
            itemView.setOnLongClickListener(view -> {
                listener.onNoteLongClicked(note);
                return true;
            });
        }
    }
}