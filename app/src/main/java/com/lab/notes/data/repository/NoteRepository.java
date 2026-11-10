package com.lab.notes.data.repository;

import com.lab.notes.data.remote.RemoteSyncException;
import com.lab.notes.model.Note;

import java.util.List;

/**
 * Single entry point for reading and changing notes.
 */
public interface NoteRepository {

    List<Note> getAllNotes();

    void saveNote(Note note);

    void deleteNote(String noteId);

    /**
     * Merges local and cloud notes so that both contain the latest versions.
     */
    void synchronize() throws RemoteSyncException;
}