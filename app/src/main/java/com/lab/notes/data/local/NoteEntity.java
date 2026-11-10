package com.lab.notes.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Database row that stores one note in the local "notes" table.
 */
@Entity(tableName = "notes")
public class NoteEntity {

    @PrimaryKey
    @NonNull
    private final String id;

    @NonNull
    private final String title;

    @NonNull
    private final String content;

    private final long updatedAtMillis;

    public NoteEntity(@NonNull String id, @NonNull String title,
                      @NonNull String content, long updatedAtMillis) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.updatedAtMillis = updatedAtMillis;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    @NonNull
    public String getContent() {
        return content;
    }

    public long getUpdatedAtMillis() {
        return updatedAtMillis;
    }
}