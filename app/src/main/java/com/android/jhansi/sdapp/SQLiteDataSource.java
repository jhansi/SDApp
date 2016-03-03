package com.android.jhansi.sdapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jhansi Tavva on 3/3/16.
 * Copyright (c) 2016 Jhansi Tavva. All rights reserved.
 */
public class SQLiteDataSource {
    public static final String TAG = SQLiteDataSource.class.getSimpleName();
    // Database fields
    private SQLiteDatabase database;
    private SQLiteHelper dbHelper;
    private String[] allColumns = { SQLiteHelper.COLUMN_ID, SQLiteHelper.COLUMN_FILE_NAME, SQLiteHelper.COLUMN_FILE_SIZE, SQLiteHelper.COLUMN_FILE_EXT};

    public SQLiteDataSource(Context context) {
        dbHelper = new SQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void createFileEnrty(String file_name, int file_size, String file_ext) {
        ContentValues values = new ContentValues();


        values.put(SQLiteHelper.COLUMN_FILE_EXT, file_ext);
        values.put(SQLiteHelper.COLUMN_FILE_SIZE, file_size);

        values.put(SQLiteHelper.COLUMN_FILE_NAME, file_name);

        long insertId = database.insert(SQLiteHelper.TABLE_SDFILES, null, values);
//        Cursor cursor = database.query(SQLiteHelper.TABLE_SDFILES,
//                allColumns, SQLiteHelper.COLUMN_ID + " = " + insertId, null, null, null, null);
//        cursor.moveToFirst();
//       FileEntry newFileEntry = cursorToFileEntry(cursor);
//        cursor.close();
        return;
    }

    public void deleteFileEntry(FileEntry fileEntry) {
        long id = fileEntry.getId();
        Log.w(TAG, "FileEntry deleted with id: " + id);
        database.delete(SQLiteHelper.TABLE_SDFILES, SQLiteHelper.COLUMN_ID + " = " + id, null);
    }

    public List<FileEntry> getAllFileEntry() {
        List<FileEntry> fileEntries = new ArrayList<FileEntry>();

        Cursor cursor = database.query(SQLiteHelper.TABLE_SDFILES,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            FileEntry fileEntry = cursorToFileEntry(cursor);
            fileEntries.add(fileEntry);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return fileEntries;
    }


    public List<FileEntry> getLargestAllFileEntry() {
        List<FileEntry> fileEntries = new ArrayList<FileEntry>();
        final String READ_TOP_RECORDS = "SELECT * FROM " + SQLiteHelper.TABLE_SDFILES + " ORDER BY "+ SQLiteHelper.COLUMN_FILE_SIZE +" DESC LIMIT 10";
        Cursor cursor = database.rawQuery(READ_TOP_RECORDS, null);



//        Cursor cursor = database.query(SQLiteHelper.TABLE_SDFILES,
//                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            FileEntry fileEntry = cursorToFileEntry(cursor);
            fileEntries.add(fileEntry);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return fileEntries;
    }


    private FileEntry cursorToFileEntry(Cursor cursor) {
        FileEntry fileEntry = new FileEntry();
        fileEntry.setId(cursor.getLong(0));
        fileEntry.setFile_name(cursor.getString(1));
        fileEntry.setFile_size(Integer.parseInt(cursor.getString(2)));
        fileEntry.setFile_ext(cursor.getString(3));
        return fileEntry;
    }
}
