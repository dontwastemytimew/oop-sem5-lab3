package com.lab.notes.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.lab.notes.data.remote.RemoteSyncException;
import com.lab.notes.data.repository.NoteRepository;
import com.lab.notes.model.Note;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class NotesViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private NoteRepository noteRepository;

    private NotesViewModel viewModel;

    @Before
    public void setUp() {
        viewModel = new NotesViewModel(noteRepository, Runnable::run);
    }

    @Test
    public void loadNotes_publishesNotesFromRepository() {
        List<Note> notes = Collections.singletonList(new Note("id-1", "Title", "Text", 1000L));
        when(noteRepository.getAllNotes()).thenReturn(notes);

        viewModel.loadNotes();

        assertEquals(notes, viewModel.getNotes().getValue());
    }

    @Test
    public void saveNote_generatesId_whenNoteIsNew() {
        viewModel.saveNote(null, "Title", "Text");

        ArgumentCaptor<Note> captor = ArgumentCaptor.forClass(Note.class);
        verify(noteRepository).saveNote(captor.capture());
        assertNotNull(captor.getValue().getId());
        assertEquals("Title", captor.getValue().getTitle());
        assertEquals("Text", captor.getValue().getContent());
    }

    @Test
    public void saveNote_keepsId_whenEditingExistingNote() {
        viewModel.saveNote("id-1", "Title", "Text");

        ArgumentCaptor<Note> captor = ArgumentCaptor.forClass(Note.class);
        verify(noteRepository).saveNote(captor.capture());
        assertEquals("id-1", captor.getValue().getId());
    }

    @Test
    public void deleteNote_removesNoteFromRepository() {
        viewModel.deleteNote("id-1");

        verify(noteRepository).deleteNote("id-1");
    }

    @Test
    public void synchronize_reportsSuccess_whenRepositorySucceeds() throws RemoteSyncException {
        viewModel.synchronize();

        verify(noteRepository).synchronize();
        assertEquals(SyncState.SUCCESS, viewModel.getSyncState().getValue());
    }

    @Test
    public void synchronize_reportsFailure_whenRepositoryThrows() throws RemoteSyncException {
        doThrow(new RemoteSyncException("offline", new RuntimeException()))
                .when(noteRepository).synchronize();

        viewModel.synchronize();

        assertEquals(SyncState.FAILED, viewModel.getSyncState().getValue());
    }

    @Test
    public void synchronize_isIgnored_whenSynchronizationIsAlreadyRunning() {
        List<Runnable> pendingTasks = new ArrayList<>();
        NotesViewModel slowViewModel = new NotesViewModel(noteRepository, pendingTasks::add);

        slowViewModel.synchronize();
        slowViewModel.synchronize();

        assertEquals(1, pendingTasks.size());
    }

    @Test
    public void acknowledgeSyncResult_resetsStateToIdle() {
        viewModel.synchronize();

        viewModel.acknowledgeSyncResult();

        assertEquals(SyncState.IDLE, viewModel.getSyncState().getValue());
    }
}