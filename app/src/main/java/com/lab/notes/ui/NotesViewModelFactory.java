package com.lab.notes.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.lab.notes.data.repository.NoteRepository;

import java.util.concurrent.ExecutorService;

/**
 * Creates {@link NotesViewModel} together with the dependencies it needs.
 */
public class NotesViewModelFactory implements ViewModelProvider.Factory {

    private final NoteRepository noteRepository;
    private final ExecutorService backgroundExecutor;

    public NotesViewModelFactory(NoteRepository noteRepository,
                                 ExecutorService backgroundExecutor) {
        this.noteRepository = noteRepository;
        this.backgroundExecutor = backgroundExecutor;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(NotesViewModel.class)) {
            return modelClass.cast(new NotesViewModel(noteRepository, backgroundExecutor));
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}