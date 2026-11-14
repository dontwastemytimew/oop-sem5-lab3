package com.lab.notes;

import android.app.Application;

import com.google.firebase.firestore.FirebaseFirestore;
import com.lab.notes.data.local.AppDatabase;
import com.lab.notes.data.remote.FirestoreNoteRemoteDataSource;
import com.lab.notes.data.repository.NoteRepository;
import com.lab.notes.data.repository.NoteRepositoryImpl;
import com.lab.notes.data.sync.LastWriteWinsResolver;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Application entry point that creates and shares the objects used across the app.
 */
public class NotesApplication extends Application {

    private NoteRepository noteRepository;
    private ExecutorService backgroundExecutor;

    @Override
    public void onCreate() {
        super.onCreate();
        AppDatabase database = AppDatabase.getInstance(this);
        noteRepository = new NoteRepositoryImpl(
                database.noteDao(),
                new FirestoreNoteRemoteDataSource(FirebaseFirestore.getInstance()),
                new LastWriteWinsResolver());
        backgroundExecutor = Executors.newSingleThreadExecutor();
    }

    public NoteRepository getNoteRepository() {
        return noteRepository;
    }

    public ExecutorService getBackgroundExecutor() {
        return backgroundExecutor;
    }
}