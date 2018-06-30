package ayevbeosa.com.journalapp.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import ayevbeosa.com.journalapp.database.AppDatabase;
import ayevbeosa.com.journalapp.database.JournalEntry;

public class AddEntryViewModel extends ViewModel {

    private LiveData<JournalEntry> entry;

    public AddEntryViewModel(AppDatabase database, int mEntryId) {
        entry = database.journalDao().loadEntryById(mEntryId);
    }

    public LiveData<JournalEntry> getEntry() {
        return entry;
    }
}
