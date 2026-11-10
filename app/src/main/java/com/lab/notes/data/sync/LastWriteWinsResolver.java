package com.lab.notes.data.sync;

import com.lab.notes.model.Note;

/**
 * Resolves conflicts by keeping the note that was edited most recently.
 * If both copies have the same edit time, the local copy is kept.
 */
public class LastWriteWinsResolver implements NoteConflictResolver {

    @Override
    public Note resolve(Note localNote, Note remoteNote) {
        if (remoteNote.getUpdatedAtMillis() > localNote.getUpdatedAtMillis()) {
            return remoteNote;
        }
        return localNote;
    }
}