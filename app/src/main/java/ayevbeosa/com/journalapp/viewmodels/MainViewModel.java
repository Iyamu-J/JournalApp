package ayevbeosa.com.journalapp.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

import ayevbeosa.com.journalapp.database.AppDatabase;
import ayevbeosa.com.journalapp.database.JournalEntry;

public class MainViewModel extends AndroidViewModel {

    private LiveData<List<JournalEntry>> entries;

    public MainViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(this.getApplication());
        entries = database.journalDao().loadAllEntries();
    }

    public LiveData<List<JournalEntry>> getEntries() {
        return entries;
    }
}
