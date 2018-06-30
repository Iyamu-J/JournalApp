package ayevbeosa.com.journalapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import ayevbeosa.com.journalapp.database.JournalEntry;

public class EntryAdapter extends RecyclerView.Adapter<EntryAdapter.EntryViewHolder> {

    private static final String DATE_FORMAT = "E, MMM dd";
    private Context mContext;
    private List<JournalEntry> entries;
    final private ItemClickListener mItemClickListener;

    private SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());


    public EntryAdapter(Context context, ItemClickListener itemClickListener) {
        this.mContext = context;
        this.mItemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public EntryAdapter.EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.entry_item_layout, parent, false);
        return new EntryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntryViewHolder holder, int position) {
        // sets the values
        JournalEntry journalEntry = entries.get(position);
        holder.mEntryTitle.setText(journalEntry.getTitle());
        holder.mLastEditedAt.setText(dateFormat.format(journalEntry.getLastEditedAt()));
    }

    @Override
    public int getItemCount() {
        if (entries == null) {
            return 0;
        } else {
            return entries.size();
        }
    }

    public List<JournalEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<JournalEntry> entries) {
        this.entries = entries;
        notifyDataSetChanged();
    }

    public interface ItemClickListener {
        void onItemClickListener(int itemId);
    }

    class EntryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView mEntryTitle;
        TextView mLastEditedAt;

        private EntryViewHolder(View itemView) {
            super(itemView);

            mEntryTitle = itemView.findViewById(R.id.tv_entry_title);
            mLastEditedAt = itemView.findViewById(R.id.tv_last_edited_at);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int elementId = entries.get(getAdapterPosition()).getId();
            mItemClickListener.onItemClickListener(elementId);
        }
    }
}
