package com.android.jhansi.sdapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
//import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jhansi Tavva on 3/3/16.
 * Copyright (c) 2016 Jhansi Tavva. All rights reserved.
 */
public class SQLiteDataSource {
    public static final String TAG = SQLiteDataSource.class.getSimpleName();

    private SQLiteDatabase database;
    private SQLiteHelper dbHelper;
    // Database fields
    private String[] allColumns = {SQLiteHelper.COLUMN_ID, SQLiteHelper.COLUMN_FILE_NAME, SQLiteHelper.COLUMN_FILE_SIZE, SQLiteHelper.COLUMN_FILE_EXT};
    private static long insertId = 0;
    private static long totalFileSize = 0;

    private static final String FILE_EXT = "ext";
    private static final String FILE_FREQ = "freq";


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

        long ret = insertId = database.insert(SQLiteHelper.TABLE_SDFILES, null, values);
        return;
    }

    public void deleteFileEntry(FileEntry fileEntry) {
        long id = fileEntry.getId();
        //Log.w(TAG, "FileEntry deleted with id: " + id);
        database.delete(SQLiteHelper.TABLE_SDFILES, SQLiteHelper.COLUMN_ID + " = " + id, null);
    }

    public void deleteAllEntries() {
        // Log.w(TAG, "table deleted");
        int ret = database.delete(SQLiteHelper.TABLE_SDFILES, null, null);
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
        cursor.close();
        return fileEntries;
    }


    public List<FileEntry> getLargestAllFileEntry() {
        List<FileEntry> fileEntries = new ArrayList<FileEntry>();
        final String READ_TOP_RECORDS = "SELECT * FROM " + SQLiteHelper.TABLE_SDFILES + " ORDER BY " + SQLiteHelper.COLUMN_FILE_SIZE + " DESC LIMIT 10";
        Cursor cursor = database.rawQuery(READ_TOP_RECORDS, null);

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

    public long getAverageFilesize() {

        final String READ_SUM_RECORDS = "SELECT SUM(" + SQLiteHelper.COLUMN_FILE_SIZE + ") FROM " + SQLiteHelper.TABLE_SDFILES;

        long avgFileSize = 0;
        Cursor cursor = database.rawQuery(READ_SUM_RECORDS, null);
        if (cursor.moveToFirst()) {
            totalFileSize = cursor.getInt(0);
        }
        avgFileSize = totalFileSize / insertId;

        cursor.close();
        return avgFileSize;
    }


    public List<Map<String, String>> getFrequentFiles() {

        List<Map<String, String>> extFreqList = new ArrayList<Map<String, String>>(5);
        final String READ_FREQ_RECORDS = "SELECT " + SQLiteHelper.COLUMN_FILE_EXT + ", SUM( " + SQLiteHelper.COLUMN_FILE_SIZE + " ) AS RELATIVE_FILE_SIZE  FROM "
                + SQLiteHelper.TABLE_SDFILES + " GROUP BY " + SQLiteHelper.COLUMN_FILE_EXT + " ORDER BY RELATIVE_FILE_SIZE DESC LIMIT 5";

        Cursor cursor = database.rawQuery(READ_FREQ_RECORDS, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Map<String, String> extEntry = new HashMap<String, String>();

            extEntry.put(FILE_EXT, cursor.getString(0));

            float freq = ((cursor.getLong(1) * 100) / totalFileSize);

            int freqInt = Math.round(freq);
            if (freqInt != 0) {
                extEntry.put(FILE_FREQ, Integer.toString(freqInt));
            } else {
                extEntry.put(FILE_FREQ, "< 1");
            }

            extFreqList.add(extEntry);
            cursor.moveToNext();
        }

        return extFreqList;
    }

    private FileEntry cursorToFileEntry(Cursor cursor) {
        FileEntry fileEntry = new FileEntry();
        fileEntry.setId(cursor.getLong(0));
        fileEntry.setFile_name(cursor.getString(1));
        fileEntry.setFile_size(cursor.getLong(2));
        fileEntry.setFile_ext(cursor.getString(3));
        return fileEntry;
    }
}
