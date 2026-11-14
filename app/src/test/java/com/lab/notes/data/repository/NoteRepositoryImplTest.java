package com.lab.notes.data.repository;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lab.notes.data.local.NoteDao;
import com.lab.notes.data.local.NoteEntity;
import com.lab.notes.data.local.NoteMapper;
import com.lab.notes.data.remote.NoteRemoteDataSource;
import com.lab.notes.data.remote.RemoteSyncException;
import com.lab.notes.data.sync.LastWriteWinsResolver;
import com.lab.notes.model.Note;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

@RunWith(MockitoJUnitRunner.class)
public class NoteRepositoryImplTest {

    private static final long OLDER_TIME = 1000L;
    private static final long NEWER_TIME = 2000L;

    @Mock
    private NoteDao noteDao;

    @Mock
    private NoteRemoteDataSource remoteDataSource;

    private NoteRepositoryImpl repository;

    @Before
    public void setUp() {
        repository = new NoteRepositoryImpl(noteDao, remoteDataSource, new LastWriteWinsResolver());
    }

    @Test
    public void getAllNotes_returnsNotesFromDatabase() {
        Note note = new Note("id-1", "Title", "Text", OLDER_TIME);
        when(noteDao.findAllNewestFirst())
                .thenReturn(Collections.singletonList(NoteMapper.toEntity(note)));

        assertEquals(Collections.singletonList(note), repository.getAllNotes());
    }

    @Test
    public void saveNote_storesNoteInDatabase() {
        Note note = new Note("id-1", "Title", "Text", OLDER_TIME);

        repository.saveNote(note);

        ArgumentCaptor<NoteEntity> captor = ArgumentCaptor.forClass(NoteEntity.class);
        verify(noteDao).insertOrReplace(captor.capture());
        assertEquals("id-1", captor.getValue().getId());
    }

    @Test
    public void deleteNote_removesNoteLocallyAndInCloud() throws RemoteSyncException {
        repository.deleteNote("id-1");

        verify(noteDao).deleteById("id-1");
        verify(remoteDataSource).delete("id-1");
    }

    @Test
    public void deleteNote_keepsLocalDeletion_whenCloudFails() throws RemoteSyncException {
        doThrow(new RemoteSyncException("offline", new RuntimeException()))
                .when(remoteDataSource).delete("id-1");

        repository.deleteNote("id-1");

        verify(noteDao).deleteById("id-1");
    }

    @Test
    public void synchronize_uploadsNote_whenItExistsOnlyLocally() throws RemoteSyncException {
        Note local = new Note("id-1", "Title", "Text", OLDER_TIME);
        when(noteDao.findAllNewestFirst())
                .thenReturn(Collections.singletonList(NoteMapper.toEntity(local)));
        when(remoteDataSource.fetchAll()).thenReturn(Collections.emptyList());

        repository.synchronize();

        verify(remoteDataSource).upload(local);
        verify(noteDao, never()).insertOrReplace(any(NoteEntity.class));
    }

    @Test
    public void synchronize_savesNoteLocally_whenItExistsOnlyInCloud() throws RemoteSyncException {
        Note remote = new Note("id-1", "Title", "Text", OLDER_TIME);
        when(noteDao.findAllNewestFirst()).thenReturn(Collections.emptyList());
        when(remoteDataSource.fetchAll()).thenReturn(Collections.singletonList(remote));

        repository.synchronize();

        ArgumentCaptor<NoteEntity> captor = ArgumentCaptor.forClass(NoteEntity.class);
        verify(noteDao).insertOrReplace(captor.capture());
        assertEquals(remote, NoteMapper.toNote(captor.getValue()));
        verify(remoteDataSource, never()).upload(any(Note.class));
    }

    @Test
    public void synchronize_keepsCloudVersion_whenCloudIsNewer() throws RemoteSyncException {
        Note local = new Note("id-1", "Title", "Old", OLDER_TIME);
        Note remote = new Note("id-1", "Title", "New", NEWER_TIME);
        when(noteDao.findAllNewestFirst())
                .thenReturn(Collections.singletonList(NoteMapper.toEntity(local)));
        when(remoteDataSource.fetchAll()).thenReturn(Collections.singletonList(remote));

        repository.synchronize();

        ArgumentCaptor<NoteEntity> captor = ArgumentCaptor.forClass(NoteEntity.class);
        verify(noteDao).insertOrReplace(captor.capture());
        assertEquals(remote, NoteMapper.toNote(captor.getValue()));
        verify(remoteDataSource, never()).upload(any(Note.class));
    }

    @Test
    public void synchronize_uploadsLocalVersion_whenLocalIsNewer() throws RemoteSyncException {
        Note local = new Note("id-1", "Title", "New", NEWER_TIME);
        Note remote = new Note("id-1", "Title", "Old", OLDER_TIME);
        when(noteDao.findAllNewestFirst())
                .thenReturn(Collections.singletonList(NoteMapper.toEntity(local)));
        when(remoteDataSource.fetchAll()).thenReturn(Collections.singletonList(remote));

        repository.synchronize();

        verify(remoteDataSource).upload(local);
        verify(noteDao, never()).insertOrReplace(any(NoteEntity.class));
    }

    @Test
    public void synchronize_changesNothing_whenNotesAreIdentical() throws RemoteSyncException {
        Note note = new Note("id-1", "Title", "Text", OLDER_TIME);
        when(noteDao.findAllNewestFirst())
                .thenReturn(Collections.singletonList(NoteMapper.toEntity(note)));
        when(remoteDataSource.fetchAll()).thenReturn(Collections.singletonList(note));

        repository.synchronize();

        verify(remoteDataSource, never()).upload(any(Note.class));
        verify(noteDao, never()).insertOrReplace(any(NoteEntity.class));
    }

    @Test(expected = RemoteSyncException.class)
    public void synchronize_throwsException_whenCloudIsUnavailable() throws RemoteSyncException {
        when(remoteDataSource.fetchAll())
                .thenThrow(new RemoteSyncException("offline", new RuntimeException()));

        repository.synchronize();
    }
}