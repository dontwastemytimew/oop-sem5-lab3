package com.lab.notes.data.local;

import com.lab.notes.model.Note;

/**
 * Converts notes between the domain model and the database representation.
 */
public final class NoteMapper {

    private NoteMapper() {
    }

    public static NoteEntity toEntity(Note note) {
        return new NoteEntity(
                note.getId(),
                note.getTitle(),
                note.getContent(),
                note.getUpdatedAtMillis());
    }

    public static Note toNote(NoteEntity entity) {
        return new Note(
                entity.getId(),
                entity.getTitle(),
                entity.getContent(),
                entity.getUpdatedAtMillis());
    }
}