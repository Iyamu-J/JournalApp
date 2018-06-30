package ayevbeosa.com.journalapp.viewmodels;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import ayevbeosa.com.journalapp.database.AppDatabase;

public class AddEntryViewModelFactory extends ViewModelProvider.NewInstanceFactory {

   private AppDatabase mDb;
   private int mEntryId;

   public AddEntryViewModelFactory(AppDatabase mDb, int mEntryId) {
       this.mDb = mDb;
       this.mEntryId = mEntryId;
   }

    @NonNull
    @SuppressWarnings("unchecked")
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new AddEntryViewModel(mDb, mEntryId);
    }
}
