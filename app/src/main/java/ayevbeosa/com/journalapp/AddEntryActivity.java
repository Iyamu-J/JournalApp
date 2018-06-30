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

    // required tags
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

        // TextView setups
        mEntryTitle = findViewById(R.id.editTextEntryTitle);
        mEntryNote = findViewById(R.id.editTextEntryNote);

        // connecting to database
        mDb = AppDatabase.getInstance(getApplicationContext());

        // checks if an Entry already exists
        if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_ENTRY_ID)) {
            mEntryId = savedInstanceState.getInt(INSTANCE_ENTRY_ID, DEFAULT_ENTRY_ID);
        }

        // gets Intent extra
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_ENTRY_ID)) {
            // checks if entry exists and stores the Id
            if (mEntryId == DEFAULT_ENTRY_ID) {
                mEntryId = intent.getIntExtra(EXTRA_ENTRY_ID, DEFAULT_ENTRY_ID);

                // creates a ViewModel for this Activity using the ViewModelFactory
                AddEntryViewModelFactory factory = new AddEntryViewModelFactory(mDb, mEntryId);
                final AddEntryViewModel viewModel = ViewModelProviders.of(this, factory).get(AddEntryViewModel.class);

                // checks for changes and updates accordingly
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
        // saves or updates a JournalEntry Object
        if (itemId == R.id.app_bar_done) {
            onDoneActionClick();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * sets the TextViews
     * @param entry the JournalEntry Object needed to set the TextViews
     */
    private void updateUI(JournalEntry entry) {
        if (entry == null) {
            return;
        }
        mEntryTitle.setText(entry.getTitle());
        mEntryNote.setText(entry.getNote());
    }

    /**
     * connects the database and
     * save or update an entry to the database
     */
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
