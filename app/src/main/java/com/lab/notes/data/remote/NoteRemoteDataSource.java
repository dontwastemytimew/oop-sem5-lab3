package com.lab.notes.data.remote;

import com.lab.notes.model.Note;

import java.util.List;

/**
 * Describes the operations that a cloud storage must provide for notes.
 */
public interface NoteRemoteDataSource {

    List<Note> fetchAll() throws RemoteSyncException;

    void upload(Note note) throws RemoteSyncException;

    void delete(String noteId) throws RemoteSyncException;
}