package com.android.jhansi.sdapp;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Created by Jhansi Tavva on 3/2/16.
 * Copyright (c) 2016 Jhansi Tavva. All rights reserved.
 */
public class FileScanService extends Service {

    private  final IBinder iBinder = new LocalBinder();

    private static final String TAG = FileScanService.class.getSimpleName();



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    public class LocalBinder extends Binder {
        public FileScanService getFileScanService(){
            return FileScanService.this;
        }
    }


    void workerThread(){
        final Handler threadHandler;
        threadHandler = new Handler();

        new Thread(new Runnable() {
            @Override
            public void run() {
                threadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        File directory = Environment.getExternalStorageDirectory();
                        getAllFilesOfDir( directory);
                    }
                });
            }
        }).start();
    }



    List<FileEntry>  getAllFilesOfDir(File directory) {
        Log.d(TAG, "Directory: " + directory.getAbsolutePath() + "\n");
        SQLiteDataSource sqlitedatasource = new SQLiteDataSource(this);
        sqlitedatasource.open();

        //List<String> fileList = new ArrayList<String>();
        List<FileEntry> fileEntries = new ArrayList<FileEntry>();
        final File[] files = directory.listFiles();
        String filename;

        if (files != null) {
            for (File file : files) {
                if (file != null) {
                    if (file.isDirectory()) {  // it is a folder...
                        getAllFilesOfDir(file);
                    } else {  // it is a file...
                        filename = file.getName();

                        sqlitedatasource.createFileEnrty(filename, (int)file.length(), getFileExt(filename));
                        Log.d(TAG, "File: " + filename + file.length() + getFileExt(filename) +"\n");
                        //fileList.add(file.getName());

                        fileEntries = sqlitedatasource.getLargestAllFileEntry();
                    }
                }
            }
        }
        sqlitedatasource.close();
    return fileEntries;
    }

    public static String getFileExt(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
    }

}
