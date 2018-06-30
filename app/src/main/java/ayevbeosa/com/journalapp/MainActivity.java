package ayevbeosa.com.journalapp;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

import ayevbeosa.com.journalapp.database.AppDatabase;
import ayevbeosa.com.journalapp.database.JournalEntry;
import ayevbeosa.com.journalapp.viewmodels.MainViewModel;

public class MainActivity extends AppCompatActivity implements EntryAdapter.ItemClickListener {

    private EntryAdapter mAdapter;
    private AppDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final RecyclerView mRecyclerView = findViewById(R.id.rv_entries);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new EntryAdapter(this, this);
        mRecyclerView.setAdapter(mAdapter);

        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(decoration);

        itemTouchHelper(mRecyclerView);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addJournalActivityIntent = new Intent(MainActivity.this, AddEntryActivity.class);
                startActivity(addJournalActivityIntent);
            }
        });

        mDb = AppDatabase.getInstance(getApplicationContext());
        setupViewModels();
    }

    /**
     * This method implements the swipe option the Recycler Item
     * the swipe option is used to delete an entry
     * @param mRecyclerView the current RecyclerView
     */
    private void itemTouchHelper(final RecyclerView mRecyclerView) {
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback
                = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            // declares the required variables
            Drawable deleteIcon;
            Drawable background;
            int deleteIconMargin;
            int intrinsicWidth;
            int intrinsicHeight;
            boolean initiated;

            /**
             * Initialises the DeleteIcon and the background
             */
            private void init() {
                deleteIcon = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_delete_black_24dp);
                deleteIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                intrinsicWidth = deleteIcon.getIntrinsicWidth();
                intrinsicHeight = deleteIcon.getIntrinsicHeight();
                background = new ColorDrawable(getResources().getColor(R.color.colorItemSwipeBackground));
                initiated = true;
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                // gets position of swiped item
                final int deletedPosition = viewHolder.getAdapterPosition();
                // saves the entries in a List
                final List<JournalEntry> entries = mAdapter.getEntries();
                // get the JournalEntry Object of the swiped item
                final JournalEntry deletedEntry = entries.get(deletedPosition);
                // connects the database and deletes the swiped JournalEntry Object
                AppExecutors.getInstance().getDiskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        mDb.journalDao().deleteEntry(entries.get(deletedPosition));
                    }
                });
                // restores the deleted JournalEntry Object
                restoreEntry(deletedEntry);
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;

                if (viewHolder.getAdapterPosition() == -1) {
                    return;
                }

                if (!initiated) {
                    init();
                }

                background.setBounds(
                        itemView.getRight() + (int) dX,
                        itemView.getTop(),
                        itemView.getRight(),
                        itemView.getBottom());
                background.draw(c);

                // the logic is used to create the desired Drawables(DeleteIcon and Background)
                int itemHeight = itemView.getHeight();
                int deleteIconLeft;
                int deleteIconRight;
                int deleteIconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                int deleteIconBottom = deleteIconTop + intrinsicHeight;
                if (dX < 0) {
                    deleteIconLeft = itemView.getRight() - deleteIconMargin - intrinsicWidth;
                    deleteIconRight = itemView.getRight() - deleteIconMargin;
                } else {
                    deleteIconLeft = itemView.getRight() - deleteIconMargin;
                    deleteIconRight = itemView.getLeft() + deleteIconMargin + intrinsicWidth;
                }
                deleteIcon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);
                deleteIcon.draw(c);

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        // attaches the ItemTouchHelper to the RecyclerView
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView);
    }

    /**
     * This method allows for restoration of the deleted JournalEntry Object
     * makes use of the SnackBar Action Method
     * @param deletedEntry the swiped JournalEntry Object
     */
    private void restoreEntry(final JournalEntry deletedEntry) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.main_layout), "Entry deleted", Snackbar.LENGTH_LONG);
        snackbar.setAction("UNDO", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppExecutors.getInstance().getDiskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        mDb.journalDao().insertEntry(deletedEntry);
                    }
                });
            }
        });
        snackbar.setActionTextColor(Color.YELLOW);
        snackbar.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // if the user clicks on the SignIn Menu, it starts the GoogleSignInActivity
        if (id == R.id.action_sign_in) {
            Intent intent = new Intent(MainActivity.this, GoogleSignInActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClickListener(int itemId) {
        Intent intent = new Intent(MainActivity.this, AddEntryActivity.class);
        intent.putExtra(AddEntryActivity.EXTRA_ENTRY_ID, itemId);
        startActivity(intent);
    }

    /**
     * Creates the ViewModels for the application
     */
    private void setupViewModels() {
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getEntries().observe(this, new Observer<List<JournalEntry>>() {
            @Override
            public void onChanged(@Nullable List<JournalEntry> journalEntries) {
                mAdapter.setEntries(journalEntries);
            }
        });
    }
}
