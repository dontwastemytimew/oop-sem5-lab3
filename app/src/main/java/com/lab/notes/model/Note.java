package com.lab.notes.model;

import java.util.Objects;

/**
 * Immutable note that is stored locally and synchronized with the cloud.
 */
public final class Note {

    private final String id;
    private final String title;
    private final String content;
    private final long updatedAtMillis;

    /**
     * Creates a note.
     *
     * @param id              unique identifier shared by the local and cloud copies
     * @param title           short heading of the note
     * @param content         main text of the note
     * @param updatedAtMillis time of the last edit in milliseconds since epoch
     */
    public Note(String id, String title, String content, long updatedAtMillis) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.title = Objects.requireNonNull(title, "title must not be null");
        this.content = Objects.requireNonNull(content, "content must not be null");
        this.updatedAtMillis = updatedAtMillis;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public long getUpdatedAtMillis() {
        return updatedAtMillis;
    }

    /**
     * Returns a copy of this note with new text and a new edit time.
     * The identifier stays the same, so the copy replaces the original.
     */
    public Note withEditedText(String newTitle, String newContent, long editedAtMillis) {
        return new Note(id, newTitle, newContent, editedAtMillis);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Note)) {
            return false;
        }
        Note note = (Note) other;
        return updatedAtMillis == note.updatedAtMillis
                && id.equals(note.id)
                && title.equals(note.title)
                && content.equals(note.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, content, updatedAtMillis);
    }
}