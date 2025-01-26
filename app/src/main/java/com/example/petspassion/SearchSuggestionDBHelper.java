package com.example.petspassion;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class SearchSuggestionDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SearchSuggestions.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "Suggestions";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TEXT = "text";

    private static final int MAX_SUGGESTIONS = 5;

    public SearchSuggestionDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TEXT + " TEXT UNIQUE)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Insert a new suggestion, ensure maximum suggestions are kept
    public void addSuggestion(String suggestion) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TEXT, suggestion);

        // Insert suggestion if not exists, else ignore
        db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);

        // Ensure only the latest MAX_SUGGESTIONS are kept
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " NOT IN (SELECT "
                + COLUMN_ID + " FROM " + TABLE_NAME + " ORDER BY " + COLUMN_ID
                + " DESC LIMIT " + MAX_SUGGESTIONS + ")");

        db.close();
    }

    // Get all suggestions
    public List<String> getAllSuggestions() {
        List<String> suggestions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_TEXT}, null, null, null, null, COLUMN_ID + " DESC");

        if (cursor.moveToFirst()) {
            do {
                suggestions.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEXT)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return suggestions;
    }


}

