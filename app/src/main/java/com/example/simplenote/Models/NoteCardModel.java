package com.example.simplenote.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoteCardModel implements Parcelable {
    private String noteId;
    private String title;
    private String description;
    private boolean pinned;
    private boolean bookmarked;

    private Date date;
    private List<String> tags;

    public NoteCardModel(String noteId, String title, String description, boolean pinned, boolean bookmarked, Date date, List<String> tags) {
        this.noteId = noteId;
        this.title = title;
        this.description = description;
        this.pinned = pinned;
        this.bookmarked = bookmarked;
        this.date = date;
        this.tags = tags;
    }

    public NoteCardModel() {
    }

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public boolean isBookmarked() {
        return bookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        this.bookmarked = bookmarked;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Map<String, Object> toMap() {
        Timestamp timestamp = new Timestamp(date);

        Map<String, Object> map = new HashMap<>();
        map.put("noteId", noteId);
        map.put("title", title);
        map.put("description", description);
        map.put("pinned", pinned);
        map.put("bookmarked", bookmarked);
        map.put("date", timestamp);
        map.put("tags", tags);

        return map;
    }

    // static method to create note object from a firestore document
    public static NoteCardModel fromMap(Map<String, Object> map) {
        String noteId = (String) map.get("noteId");
        String title = (String) map.get("title");
        String description = (String) map.get("description");
        boolean pinned = (boolean) map.get("pinned");
        boolean bookmarked = (boolean) map.get("bookmarked");
        Timestamp timestamp = (Timestamp) map.get("date");
        Date date = new Date(timestamp.toDate().getTime()); // Convert Timestamp to Date
        List<String> tags = (List<String>) map.get("tags");

        return new NoteCardModel(noteId, title, description, pinned, bookmarked, date, tags);
    }

    // Parcelable implementation
    protected NoteCardModel(Parcel in) {
        noteId = in.readString();
        title = in.readString();
        description = in.readString();
        pinned = in.readByte() != 0;
        bookmarked = in.readByte() != 0;
        date = new Date(in.readLong());
        tags = in.createStringArrayList();
    }

    public static final Creator<NoteCardModel> CREATOR = new Creator<NoteCardModel>() {
        @Override
        public NoteCardModel createFromParcel(Parcel in) {
            return new NoteCardModel(in);
        }

        @Override
        public NoteCardModel[] newArray(int size) {
            return new NoteCardModel[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(noteId);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeByte((byte) (pinned ? 1 : 0));
        dest.writeByte((byte) (bookmarked ? 1 : 0));
        dest.writeLong(date.getTime());
        dest.writeStringList(tags);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
