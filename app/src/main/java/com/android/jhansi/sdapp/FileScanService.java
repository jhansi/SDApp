package com.android.jhansi.sdapp;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

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

    private Handler handler = new Handler();
    Thread thread;
     public static boolean status = true;

    SQLiteDataSource sqlitedatasource;

    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;

    private static final int NOTIFICATION_ID = 1;

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }


    public class LocalBinder extends Binder {
        public FileScanService getFileScanService(){
            return FileScanService.this;
        }
    }


void scanSDcard(File directory){
    sqlitedatasource = new SQLiteDataSource(this);
    sqlitedatasource.open();
    sqlitedatasource.deleteAllEntries();
    getAllFilesOfDir(directory);

}

    void  getAllFilesOfDir(File directory) {
        Log.d(TAG, "Directory: " + directory.getAbsolutePath() + "\n");


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


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "Service onStartCommand");

        status = true;
       thread = new Thread(new Runnable() {
            @Override
            public void run() {

                showNotification();
                scanSDcard(Environment.getExternalStorageDirectory());

                List<FileEntry> list = getLargestTenFiles();
                long avgFileSize = getAvgFileSize();
                List<Map<String,String>> listFreqFiles = getFrequentFiles();


                MainActivity.parcelableData.setAvgFileSize(avgFileSize);
                MainActivity.parcelableData.setListFrequentFiles(listFreqFiles);
                MainActivity.parcelableData.setListLargeFiles(list);

                if(status) {
                    Intent intent = new Intent();
                    intent.setAction(MY_ACTION);

                    sendBroadcast(intent);
                    mBuilder.setContentText(getResources().getString(R.string.NotificationTitleDone)).setProgress(0,0,false);
                    mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
                }
                stopSelf();
            }
        });
        thread.start();

        return Service.START_STICKY;
    }


    private void showNotification(){

        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle(getResources().getString(R.string.NotificationTitle))
                .setContentText(getResources().getString(R.string.NotificationText))
                .setSmallIcon(R.drawable.ic_action_sync);
        mBuilder.setProgress(0, 0, true);
        mBuilder.setAutoCancel(true);
        mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());

    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service onDestroy", Toast.LENGTH_LONG).show();
       // Utils.cancelNotification(this);
        status = false;
        handler.removeCallbacksAndMessages(thread);
        mBuilder.setContentText(getResources().getString(R.string.NotificationTitleCancel)).setProgress(0, 0, false);
        mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
        super.onDestroy();
    }

    @Override
    public boolean stopService(Intent name) {
        status = false;
        return super.stopService(name);
    }
}
