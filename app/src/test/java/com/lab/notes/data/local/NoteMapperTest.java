package com.lab.notes.data.local;

import static org.junit.Assert.assertEquals;

import com.lab.notes.model.Note;

import org.junit.Test;

public class NoteMapperTest {

    @Test
    public void toEntity_copiesAllFields() {
        Note note = new Note("id-1", "Title", "Text", 1000L);

        NoteEntity entity = NoteMapper.toEntity(note);

        assertEquals("id-1", entity.getId());
        assertEquals("Title", entity.getTitle());
        assertEquals("Text", entity.getContent());
        assertEquals(1000L, entity.getUpdatedAtMillis());
    }

    @Test
    public void toNote_restoresNoteConvertedToEntity() {
        Note original = new Note("id-1", "Title", "Text", 1000L);

        Note restored = NoteMapper.toNote(NoteMapper.toEntity(original));

        assertEquals(original, restored);
    }
}