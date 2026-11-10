package com.lab.notes.data.repository;

import com.lab.notes.data.local.NoteDao;
import com.lab.notes.data.local.NoteEntity;
import com.lab.notes.data.local.NoteMapper;
import com.lab.notes.data.remote.NoteRemoteDataSource;
import com.lab.notes.data.remote.RemoteSyncException;
import com.lab.notes.data.sync.NoteConflictResolver;
import com.lab.notes.model.Note;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores notes locally first and synchronizes them with the cloud on demand.
 */
public class NoteRepositoryImpl implements NoteRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(NoteRepositoryImpl.class);

    private final NoteDao noteDao;
    private final NoteRemoteDataSource remoteDataSource;
    private final NoteConflictResolver conflictResolver;

    public NoteRepositoryImpl(NoteDao noteDao,
                              NoteRemoteDataSource remoteDataSource,
                              NoteConflictResolver conflictResolver) {
        this.noteDao = noteDao;
        this.remoteDataSource = remoteDataSource;
        this.conflictResolver = conflictResolver;
    }

    @Override
    public List<Note> getAllNotes() {
        List<Note> notes = new ArrayList<>();
        for (NoteEntity entity : noteDao.findAllNewestFirst()) {
            notes.add(NoteMapper.toNote(entity));
        }
        return notes;
    }

    @Override
    public void saveNote(Note note) {
        noteDao.insertOrReplace(NoteMapper.toEntity(note));
        LOGGER.debug("Note {} saved locally", note.getId());
    }

    @Override
    public void deleteNote(String noteId) {
        noteDao.deleteById(noteId);
        try {
            remoteDataSource.delete(noteId);
        } catch (RemoteSyncException exception) {
            LOGGER.warn("Note {} was deleted locally but not in the cloud", noteId, exception);
        }
    }

    @Override
    public void synchronize() throws RemoteSyncException {
        Map<String, Note> remoteById = indexById(remoteDataSource.fetchAll());
        Map<String, Note> localById = indexById(getAllNotes());

        for (Note localNote : localById.values()) {
            mergeLocalNote(localNote, remoteById.get(localNote.getId()));
        }
        for (Note remoteNote : remoteById.values()) {
            if (!localById.containsKey(remoteNote.getId())) {
                saveNote(remoteNote);
            }
        }
        LOGGER.info("Synchronization finished: {} local, {} cloud notes checked",
                localById.size(), remoteById.size());
    }

    private void mergeLocalNote(Note localNote, Note remoteNote) throws RemoteSyncException {
        if (remoteNote == null) {
            remoteDataSource.upload(localNote);
            return;
        }
        Note winner = conflictResolver.resolve(localNote, remoteNote);
        if (!winner.equals(localNote)) {
            saveNote(winner);
        }
        if (!winner.equals(remoteNote)) {
            remoteDataSource.upload(winner);
        }
    }

    private Map<String, Note> indexById(List<Note> notes) {
        Map<String, Note> notesById = new HashMap<>();
        for (Note note : notes) {
            notesById.put(note.getId(), note);
        }
        return notesById;
    }
}