package ayevbeosa.com.journalapp;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Date;

import ayevbeosa.com.journalapp.database.AppDatabase;
import ayevbeosa.com.journalapp.database.JournalEntry;
import ayevbeosa.com.journalapp.viewmodels.AddEntryViewModel;
import ayevbeosa.com.journalapp.viewmodels.AddEntryViewModelFactory;

public class AddEntryActivity extends AppCompatActivity {

    public static final String EXTRA_ENTRY_ID = "extraEntryId";
    public static final String INSTANCE_ENTRY_ID = "instanceTaskId";
    private static final int DEFAULT_ENTRY_ID = -1;
    private int mEntryId = DEFAULT_ENTRY_ID;

    EditText mEntryTitle;
    EditText mEntryNote;

    private AppDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_entry);

        mEntryTitle = findViewById(R.id.editTextEntryTitle);
        mEntryNote = findViewById(R.id.editTextEntryNote);

        mDb = AppDatabase.getInstance(getApplicationContext());

        if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_ENTRY_ID)) {
            mEntryId = savedInstanceState.getInt(INSTANCE_ENTRY_ID, DEFAULT_ENTRY_ID);
        }

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_ENTRY_ID)) {
            if (mEntryId == DEFAULT_ENTRY_ID) {
                mEntryId = intent.getIntExtra(EXTRA_ENTRY_ID, DEFAULT_ENTRY_ID);

                AddEntryViewModelFactory factory = new AddEntryViewModelFactory(mDb, mEntryId);
                final AddEntryViewModel viewModel = ViewModelProviders.of(this, factory).get(AddEntryViewModel.class);

                viewModel.getEntry().observe(this, new Observer<JournalEntry>() {
                    @Override
                    public void onChanged(@Nullable JournalEntry journalEntry) {
                        viewModel.getEntry().removeObserver(this);
                        updateUI(journalEntry);
                    }
                });
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(INSTANCE_ENTRY_ID, mEntryId);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_entry, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.app_bar_done) {
            onDoneActionClick();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateUI(JournalEntry entry) {
        if (entry == null) {
            return;
        }
        mEntryTitle.setText(entry.getTitle());
        mEntryNote.setText(entry.getNote());
    }

    public void onDoneActionClick() {
        String title = mEntryTitle.getText().toString();
        String note = mEntryNote.getText().toString();
        Date date = new Date();

        if (title.isEmpty()) {
            Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
        } else if (note.isEmpty()){
            Toast.makeText(this, "Note cannot be empty", Toast.LENGTH_SHORT).show();
        } else {
            final JournalEntry journalEntry = new JournalEntry(title, note, date);

            AppExecutors.getInstance().getDiskIO().execute(new Runnable() {
                @Override
                public void run() {
                    if (mEntryId == DEFAULT_ENTRY_ID) {
                        // inserts a new entry
                        mDb.journalDao().insertEntry(journalEntry);
                    } else {
                        // updates entry
                        journalEntry.setId(mEntryId);
                        mDb.journalDao().updateEntry(journalEntry);
                    }
                    finish();
                }
            });
        }
    }
}
