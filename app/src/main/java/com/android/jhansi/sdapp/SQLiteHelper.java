package com.android.jhansi.sdapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Jhansi Tavva on 3/3/16.
 * Copyright (c) 2016 Jhansi Tavva. All rights reserved.
 */
public class SQLiteHelper extends SQLiteOpenHelper {


        public static final String TABLE_SDFILES = "sdcard_files";
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_FILE_NAME = "filename";
        public static final String COLUMN_FILE_SIZE = "filesize";
        public static final String COLUMN_FILE_EXT = "fileext";


        private static final String DATABASE_NAME = "sdapp_database.db";
        private static final int DATABASE_VERSION = 1;

        // Database creation sql statement
        private static final String DATABASE_CREATE = "create table "
                + TABLE_SDFILES
                + "(" + COLUMN_ID + " integer primary key autoincrement, "
                + COLUMN_FILE_NAME + " text not null,"
                + COLUMN_FILE_SIZE + " integer not null,"
                + COLUMN_FILE_EXT + " text not null);";

        public SQLiteHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase database) {
            database.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(SQLiteHelper.class.getName(),
                    "Upgrading database from version " + oldVersion + " to "
                            + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SDFILES);
            onCreate(db);
        }
    public void delete(SQLiteDatabase database){
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_SDFILES);
    }

}
