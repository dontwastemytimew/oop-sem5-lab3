package com.lab.notes.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lab.notes.NotesApplication;
import com.lab.notes.R;
import com.lab.notes.model.Note;
import com.lab.notes.i18n.AppLanguage;
import com.lab.notes.i18n.LanguageManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main screen that shows the list of notes and lets the user manage and synchronize them.
 */
public class MainActivity extends AppCompatActivity
        implements NotesAdapter.OnNoteActionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainActivity.class);

    private NotesViewModel viewModel;
    private NotesAdapter adapter;
    private View emptyNotesText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));

        NotesApplication application = (NotesApplication) getApplication();
        viewModel = new ViewModelProvider(this, new NotesViewModelFactory(
                application.getNoteRepository(),
                application.getBackgroundExecutor())).get(NotesViewModel.class);

        emptyNotesText = findViewById(R.id.emptyNotesText);
        setUpNotesList();
        findViewById(R.id.addNoteButton).setOnClickListener(view -> showNoteEditor(null));
        observeViewModel();

        viewModel.loadNotes();
        LOGGER.info("MainActivity created");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_sync) {
            viewModel.synchronize();
            return true;
        }
        if (item.getItemId() == R.id.action_language) {
            showLanguageDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNoteClicked(Note note) {
        showNoteEditor(note);
    }

    @Override
    public void onNoteLongClicked(Note note) {
        new MaterialAlertDialogBuilder(this)
                .setMessage(R.string.delete_note_confirmation)
                .setPositiveButton(R.string.delete,
                        (dialog, which) -> viewModel.deleteNote(note.getId()))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void setUpNotesList() {
        adapter = new NotesAdapter(this);
        RecyclerView recyclerView = findViewById(R.id.notesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getNotes().observe(this, notes -> {
            adapter.submitList(notes);
            emptyNotesText.setVisibility(notes.isEmpty() ? View.VISIBLE : View.GONE);
        });
        viewModel.getSyncState().observe(this, this::showSyncState);
    }

    private void showSyncState(SyncState state) {
        switch (state) {
            case IN_PROGRESS:
                showMessage(R.string.sync_in_progress);
                break;
            case SUCCESS:
                showMessage(R.string.sync_success);
                viewModel.acknowledgeSyncResult();
                break;
            case FAILED:
                showMessage(R.string.sync_failed);
                viewModel.acknowledgeSyncResult();
                break;
            default:
                break;
        }
    }

    private void showMessage(@StringRes int messageId) {
        Toast.makeText(this, messageId, Toast.LENGTH_SHORT).show();
    }

    /**
     * Opens the editor dialog. Pass {@code null} to create a new note.
     */
    private void showNoteEditor(Note note) {
        boolean isNewNote = note == null;
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_note_editor, null);
        EditText titleInput = dialogView.findViewById(R.id.titleInput);
        EditText contentInput = dialogView.findViewById(R.id.contentInput);
        if (!isNewNote) {
            titleInput.setText(note.getTitle());
            contentInput.setText(note.getContent());
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(isNewNote ? R.string.new_note_dialog_title
                        : R.string.edit_note_dialog_title)
                .setView(dialogView)
                .setPositiveButton(R.string.save, (dialog, which) -> saveNoteFromInput(
                        isNewNote ? null : note.getId(),
                        titleInput.getText().toString().trim(),
                        contentInput.getText().toString().trim()))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showLanguageDialog() {
        AppLanguage[] languages = AppLanguage.values();
        String[] languageNames = new String[languages.length];
        for (int i = 0; i < languages.length; i++) {
            languageNames[i] = languages[i].getDisplayName();
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.language_dialog_title)
                .setSingleChoiceItems(languageNames, LanguageManager.getCurrent().ordinal(),
                        (dialog, which) -> {
                            LOGGER.info("Language changed to {}", languages[which].getLanguageTag());
                            LanguageManager.apply(languages[which]);
                            dialog.dismiss();
                        })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void saveNoteFromInput(String noteId, String title, String content) {
        if (title.isEmpty() && content.isEmpty()) {
            LOGGER.debug("Empty note was not saved");
            return;
        }
        viewModel.saveNote(noteId, title, content);
    }
}