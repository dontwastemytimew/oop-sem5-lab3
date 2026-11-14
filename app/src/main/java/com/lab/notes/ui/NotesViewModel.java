package com.lab.notes.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lab.notes.data.remote.RemoteSyncException;
import com.lab.notes.data.repository.NoteRepository;
import com.lab.notes.model.Note;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

/**
 * Prepares notes for the screen and runs all storage work on a background thread.
 */
public class NotesViewModel extends ViewModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotesViewModel.class);

    private final NoteRepository noteRepository;
    private final ExecutorService backgroundExecutor;
    private final MutableLiveData<List<Note>> notes =
            new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<SyncState> syncState =
            new MutableLiveData<>(SyncState.IDLE);

    public NotesViewModel(NoteRepository noteRepository, ExecutorService backgroundExecutor) {
        this.noteRepository = noteRepository;
        this.backgroundExecutor = backgroundExecutor;
    }

    public LiveData<List<Note>> getNotes() {
        return notes;
    }

    public LiveData<SyncState> getSyncState() {
        return syncState;
    }

    public void loadNotes() {
        backgroundExecutor.execute(this::publishNotes);
    }

    /**
     * Saves a note. Pass {@code null} as the identifier to create a new note.
     */
    public void saveNote(String noteId, String title, String content) {
        backgroundExecutor.execute(() -> {
            String id = noteId == null ? UUID.randomUUID().toString() : noteId;
            noteRepository.saveNote(new Note(id, title, content, System.currentTimeMillis()));
            publishNotes();
        });
    }

    public void deleteNote(String noteId) {
        backgroundExecutor.execute(() -> {
            noteRepository.deleteNote(noteId);
            publishNotes();
        });
    }

    public void synchronize() {
        syncState.setValue(SyncState.IN_PROGRESS);
        backgroundExecutor.execute(() -> {
            try {
                noteRepository.synchronize();
                publishNotes();
                syncState.postValue(SyncState.SUCCESS);
            } catch (RemoteSyncException exception) {
                LOGGER.error("Synchronization failed", exception);
                syncState.postValue(SyncState.FAILED);
            }
        });
    }

    /**
     * Resets the sync state after the screen has shown its result to the user.
     */
    public void acknowledgeSyncResult() {
        syncState.setValue(SyncState.IDLE);
    }

    private void publishNotes() {
        notes.postValue(noteRepository.getAllNotes());
    }
}