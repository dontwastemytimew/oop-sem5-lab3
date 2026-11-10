package com.lab.notes.data.remote;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.lab.notes.model.Note;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Stores notes in the Firebase Firestore cloud database.
 * All methods block until the cloud answers, so they must be called from a background thread.
 */
public class FirestoreNoteRemoteDataSource implements NoteRemoteDataSource {

    private static final String COLLECTION_NOTES = "notes";
    private static final String FIELD_TITLE = "title";
    private static final String FIELD_CONTENT = "content";
    private static final String FIELD_UPDATED_AT = "updatedAtMillis";
    private static final long TIMEOUT_SECONDS = 10;

    private final CollectionReference notesCollection;

    public FirestoreNoteRemoteDataSource(FirebaseFirestore firestore) {
        this.notesCollection = firestore.collection(COLLECTION_NOTES);
    }

    @Override
    public List<Note> fetchAll() throws RemoteSyncException {
        QuerySnapshot snapshot = awaitResult(notesCollection.get());
        List<Note> notes = new ArrayList<>();
        for (DocumentSnapshot document : snapshot.getDocuments()) {
            notes.add(toNote(document));
        }
        return notes;
    }

    @Override
    public void upload(Note note) throws RemoteSyncException {
        awaitResult(notesCollection.document(note.getId()).set(toCloudData(note)));
    }

    @Override
    public void delete(String noteId) throws RemoteSyncException {
        awaitResult(notesCollection.document(noteId).delete());
    }

    private <T> T awaitResult(Task<T> task) throws RemoteSyncException {
        try {
            return Tasks.await(task, TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RemoteSyncException("Cloud request was interrupted", exception);
        } catch (ExecutionException | TimeoutException exception) {
            throw new RemoteSyncException("Cloud request failed", exception);
        }
    }

    private Map<String, Object> toCloudData(Note note) {
        Map<String, Object> data = new HashMap<>();
        data.put(FIELD_TITLE, note.getTitle());
        data.put(FIELD_CONTENT, note.getContent());
        data.put(FIELD_UPDATED_AT, note.getUpdatedAtMillis());
        return data;
    }

    private Note toNote(DocumentSnapshot document) {
        String title = document.getString(FIELD_TITLE);
        String content = document.getString(FIELD_CONTENT);
        Long updatedAt = document.getLong(FIELD_UPDATED_AT);
        return new Note(
                document.getId(),
                title == null ? "" : title,
                content == null ? "" : content,
                updatedAt == null ? 0L : updatedAt);
    }
}