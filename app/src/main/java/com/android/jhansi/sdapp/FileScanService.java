package com.android.jhansi.sdapp;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by Jhansi Tavva on 3/2/16.
 * Copyright (c) 2016 Jhansi Tavva. All rights reserved.
 */
public class FileScanService extends Service {

    private  final IBinder iBinder = new LocalBinder();

    private static final String TAG = FileScanService.class.getSimpleName();

    final static String MY_ACTION = "MY_ACTION";


    SQLiteDataSource sqlitedatasource;

//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        return Service.START_STICKY;
//    }
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }


    public class LocalBinder extends Binder {
        public FileScanService getFileScanService(){
            return FileScanService.this;
        }
    }


//    void workerThread(){
//        final Handler threadHandler;
//        threadHandler = new Handler();
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                threadHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        File directory = Environment.getExternalStorageDirectory();
//                        getAllFilesOfDir( directory);
//                    }
//                });
//            }
//        }).start();
//    }

void scanSDcard(File directory){
    sqlitedatasource = new SQLiteDataSource(this);
    sqlitedatasource.open();
    sqlitedatasource.deleteAllEntries();
    getAllFilesOfDir(directory);

}

    void  getAllFilesOfDir(File directory) {
        Log.d(TAG, "Directory: " + directory.getAbsolutePath() + "\n");


        //List<String> fileList = new ArrayList<String>();

        final File[] files = directory.listFiles();
        String filename;

        if (files != null) {
            for (File file : files) {
                if (file != null) {
                    if (file.isDirectory()) {  // it is a folder...
                        getAllFilesOfDir(file);
                    } else {  // it is a file...
                        filename = file.getName();
                        if(accept(filename)) {
                            sqlitedatasource.createFileEnrty(filename, (int) file.length(), getFileExt(filename));
                            Log.d(TAG, "File: " + filename + " " + file.length() + " " + getFileExt(filename) + "\n");
                        }
                        //fileList.add(file.getName());


                    }
                }
            }
        }
        return;
    }

    List<FileEntry> getLargestTenFiles(){
        SQLiteDataSource sqlitedatasource = new SQLiteDataSource(this);
        sqlitedatasource.open();

        List<FileEntry> fileEntries = new ArrayList<FileEntry>();
        fileEntries = sqlitedatasource.getLargestAllFileEntry();
        sqlitedatasource.close();
        return fileEntries;

    }

    private final List<String> exts = Arrays.asList("jpeg", "jpg", "png", "bmp", "gif", ".mp3", ".mp4", ".pdf",".txt" , ".xml", ".doc",
            ".xls",".xlsx", "ogg");


    public boolean accept(String filename) {
        String ext;
//        String path = pathname.getPath();
//        ext = path.substring(path.lastIndexOf(".") + 1);

        ext = getFileExt(filename);
        return exts.contains(ext);
    }
    public static String getFileExt(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
    }

    public long getAvgFileSize(){
       return sqlitedatasource.getAverageFilesize();
    }

    public List<Map<String,String>> getFrequentFiles(){
        return sqlitedatasource.getFrequentFiles();
    }


    public long getAvgFileSizeFromService(){
        return MainActivity.parcelableData.avgFileSize;
    }

    public List<Map<String,String>> getFrequentFilesFromService(){
        return MainActivity.parcelableData.listFrequentFiles;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "Service onStartCommand");

        //Creating new thread for my service
        //Always write your long running tasks in a separate thread, to avoid ANR
        new Thread(new Runnable() {
            @Override
            public void run() {

                File WhatsApp = new File("/storage/emulated/0/WhatsApp/Profile Pictures");

                scanSDcard(WhatsApp); //TODO change later
//                scanSDcard(Environment.getExternalStorageDirectory());

                List<FileEntry> list = getLargestTenFiles();
                long avgFileSize = getAvgFileSize();
                List<Map<String,String>> listFreqFiles = getFrequentFiles();


                MainActivity.parcelableData.setAvgFileSize(avgFileSize);
                MainActivity.parcelableData.setListFrequentFiles(listFreqFiles);
                MainActivity.parcelableData.setListLargeFiles(list);

                Intent intent = new Intent();
                intent.setAction(MY_ACTION);
//
//                intent.putExtra("DATAPASSED",  parcelableData);
                sendBroadcast(intent);

                stopSelf();
            }
        }).start();

        return Service.START_STICKY;
    }

}
