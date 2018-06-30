package ayevbeosa.com.journalapp.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "entry")
public class JournalEntry {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String note;
    @ColumnInfo(name = "last_edited_at")
    private Date lastEditedAt;

    public JournalEntry(int id, String title, String note, Date lastEditedAt) {
        this.id = id;
        this.title = title;
        this.note = note;
        this.lastEditedAt = lastEditedAt;
    }

    @Ignore
    public JournalEntry(String title, String note, Date lastEditedAt) {
        this.title = title;
        this.note = note;
        this.lastEditedAt = lastEditedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Date getLastEditedAt() {
        return lastEditedAt;
    }

    public void setLastEditedAt(Date lastEditedAt) {
        this.lastEditedAt = lastEditedAt;
    }
}
