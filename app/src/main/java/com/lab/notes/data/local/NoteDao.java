package com.lab.notes.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * Describes the database operations available for notes.
 */
@Dao
public interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY updatedAtMillis DESC")
    List<NoteEntity> findAllNewestFirst();

    @Query("SELECT * FROM notes WHERE id = :noteId")
    NoteEntity findById(String noteId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrReplace(NoteEntity note);

    @Query("DELETE FROM notes WHERE id = :noteId")
    void deleteById(String noteId);
}