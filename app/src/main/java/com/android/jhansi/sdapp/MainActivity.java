package com.android.jhansi.sdapp;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceConfigurationError;

public class MainActivity extends AppCompatActivity {

    FileScanService fileScanService;
    boolean status = false;
    List<String> fileList = new ArrayList<String>();

    private SQLiteDataSource sqlitedatasource;

    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // doShare();
        setContentView(R.layout.activity_main);

//        sqlitedatasource = new SQLiteDataSource(this);
//        sqlitedatasource.open();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.action_share);

        // Get its ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        doShare();
//        menu.getItem(0).setEnabled(false);
//        menu.getItem(1).setEnabled(false);
//        invalidateOptionsMenu();
        return true;
    }

    // When sync is done
    public void doShare() {

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "From me to you, this text is new.");
        // When sync is done
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sync:
                onRunSampleClick();
               // bindFileScanService(); //TODO uncomment later
                workerThread();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            FileScanService.LocalBinder localBinder = (FileScanService.LocalBinder)service;
            fileScanService = localBinder.getFileScanService();
            if(fileScanService != null){
                scanSDCard();
               // workerThread();
            }
            status = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private void bindFileScanService(){

        Intent intent = new Intent(this, FileScanService.class);
        bindService(intent,serviceConnection, Context.BIND_AUTO_CREATE);
        status = true;
    //display UI
        Toast.makeText(MainActivity.this, "Service binded", Toast.LENGTH_SHORT).show();
    }

    private void unbindFileScanService(){
        if(status) {
            unbindService(serviceConnection);
            Toast.makeText(MainActivity.this, "Service unbinded", Toast.LENGTH_SHORT).show();
            status = false;
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
                        bindFileScanService();
                    }
                });
            }
        }).start();
    }
    private void scanSDCard(){
        final String state = Environment.getExternalStorageState();
            if ( Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state) ) {  // we can read the External Storage...
        if(status) {
            List<FileEntry> fileEntries = fileScanService.getAllFilesOfDir(Environment.getExternalStorageDirectory());
           // fileScanService.workerThread();
        }
    }
    }


//    private void ShowNotification(){
//        Notification notification = new Notification(R.drawable.icon, getText(R.string.ticker_text),
//                System.currentTimeMillis());
//        Intent notificationIntent = new Intent(this, ExampleActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
//        notification.setLatestEventInfo(this, getText(R.string.notification_title),
//                getText(R.string.notification_message), pendingIntent);
//        startForeground(ONGOING_NOTIFICATION_ID, notification);
//
//    }

    private void showDialog(){
        ProgressDialog myDialog = new ProgressDialog(MainActivity.this);
        myDialog.setMessage("Scanning...");
        myDialog.setCancelable(true);
        myDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        myDialog.show();
    }

    public void onRunSampleClick(){
        final Handler threadHandler;
        threadHandler = new Handler();

        new Thread(new Runnable() {
            @Override
            public void run() {
                threadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        showDialog();
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindFileScanService();
    };
}
