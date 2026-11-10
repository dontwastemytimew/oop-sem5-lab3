package com.lab.notes.data.sync;

import com.lab.notes.model.Note;

/**
 * Chooses which version of a note wins when the local and cloud copies differ.
 */
public interface NoteConflictResolver {

    Note resolve(Note localNote, Note remoteNote);
}