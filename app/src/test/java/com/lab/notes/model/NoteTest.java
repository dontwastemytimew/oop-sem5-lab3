package com.lab.notes.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class NoteTest {

    private static final long CREATED_AT = 1000L;
    private static final long EDITED_AT = 2000L;

    @Test
    public void withEditedText_keepsIdAndChangesTextAndTime() {
        Note original = new Note("id-1", "Old title", "Old text", CREATED_AT);

        Note edited = original.withEditedText("New title", "New text", EDITED_AT);

        assertEquals("id-1", edited.getId());
        assertEquals("New title", edited.getTitle());
        assertEquals("New text", edited.getContent());
        assertEquals(EDITED_AT, edited.getUpdatedAtMillis());
    }

    @Test
    public void withEditedText_doesNotModifyOriginalNote() {
        Note original = new Note("id-1", "Title", "Text", CREATED_AT);

        original.withEditedText("Other", "Other", EDITED_AT);

        assertEquals("Title", original.getTitle());
        assertEquals(CREATED_AT, original.getUpdatedAtMillis());
    }

    @Test
    public void equals_returnsFalse_whenContentDiffers() {
        Note first = new Note("id-1", "Title", "A", CREATED_AT);
        Note second = new Note("id-1", "Title", "B", CREATED_AT);

        assertNotEquals(first, second);
    }

    @Test
    public void equals_returnsTrue_whenAllFieldsMatch() {
        Note first = new Note("id-1", "Title", "Text", CREATED_AT);
        Note second = new Note("id-1", "Title", "Text", CREATED_AT);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test(expected = NullPointerException.class)
    public void constructor_rejectsNullId() {
        new Note(null, "Title", "Text", CREATED_AT);
    }
}