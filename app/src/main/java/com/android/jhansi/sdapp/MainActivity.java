package com.android.jhansi.sdapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    private static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 1;

    private static final int MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 2;

    FileScanService fileScanService;
    boolean status = false;
    List<String> fileList = new ArrayList<String>();

    private static final String FILE_NAME = "name";
    private static final String FILE_SIZE = "size";


    private static final String FILE_EXT = "ext";
    private static final String FILE_FREQ = "freq";


    private SQLiteDataSource sqlitedatasource;

    private ShareActionProvider mShareActionProvider;

    private ListView listView;
    private SimpleAdapter adapter;

    private long avgFileSize;

    public  ProgressDialog myDialog;

    public static ParcelableData parcelableData = new ParcelableData();
    @Override
    protected void onStart() {
        super.onStart();

        //Register BroadcastReceiver
        //to receive event from our service
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(fileScanService.MY_ACTION);
        registerReceiver(myReceiver, intentFilter);
    }

    private TextView textViewAvgFileSize;

    public static boolean isSyncCompleted = false;

    public static StringBuffer statistics = new StringBuffer();


    MyReceiver myReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.action_share);

        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        if(isSyncCompleted) {
            item.setVisible(true);
            doShare();
        }
        else{
            item.setVisible(false);
        }
        return true;
    }

    // When sync is done
    public void doShare() {

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, statistics.toString());
        //shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml("<p>This is the text shared.</p>"));

        // When sync is done
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sync:
                //ShowDialogThread();
                showDialog();
                //bindFileScanService(); //TODO uncomment later
               // workerThread();
                Intent intent = new Intent(this, FileScanService.class);
                startService(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            isSyncCompleted = false;

            FileScanService.LocalBinder localBinder = (FileScanService.LocalBinder)service;
            fileScanService = localBinder.getFileScanService();
            if(fileScanService != null){
                scanSDCard();
               // workerThread();
            }
            status = true;


            isSyncCompleted = true;
            invalidateOptionsMenu();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            status = false;
            isSyncCompleted = true;
            invalidateOptionsMenu();
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
//                        bindFileScanService();
//                    }
//                });
//            }
//        }).start();
//    }

    private void updateUI(){
        updateListView(parcelableData.listLargeFiles);
        updateAvgSizeTextView(parcelableData.avgFileSize);
        updateEXtFreqTable(parcelableData.listFrequentFiles);
    }

    private void scanSDCard(){
        final String state = Environment.getExternalStorageState();
            if ( Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state) ) {  // we can read the External Storage...
             checkForPermissions();
        if(status) {

            statistics.setLength(0);
            statistics.append(getResources().getString(R.string.top_files));
            fileScanService.scanSDcard(Environment.getExternalStorageDirectory()); //TODO to be uncommented

//            File WhatsApp = new File("/storage/emulated/0/WhatsApp/Profile Pictures");
//            fileScanService.scanSDcard(WhatsApp);
            List<FileEntry> fileEntries = fileScanService.getLargestTenFiles();

           // fileScanService.workerThread();
            updateListView(fileEntries);

            avgFileSize = fileScanService.getAvgFileSize();
            updateAvgSizeTextView(avgFileSize);

            List<Map<String,String>>  ListExtFreq  = fileScanService.getFrequentFiles();

            updateEXtFreqTable(ListExtFreq);
        }
    }
        unbindFileScanService();
    }

    private void updateAvgSizeTextView(long avgFileSize) {
        textViewAvgFileSize =  (TextView) findViewById(R.id.textViewAvgFileSize);
        String file_size_avg = android.text.format.Formatter.formatFileSize(this, avgFileSize);
        Resources res = getResources();
        String text = String.format(res.getString(R.string.avg_file_size_data), file_size_avg);
       // textViewAvgFileSize.setText(textViewAvgFileSize.getText() +" "+file_size_avg);
        statistics.append("."+text);
        textViewAvgFileSize.setText(text);
    }

    void checkForPermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // Assume thisActivity is the current activity
            int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    && permissionCheck != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) ) {

                } else {

                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_READ_STORAGE);
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_WRITE_STORAGE);
                }
            }
        }

    }

    private void updateListView(List<FileEntry> fileEntries ){
        final List<Map<String, String>> listLargestFiles = new ArrayList<Map<String, String>>(10);


        for(FileEntry fileEntry : fileEntries ){
            Map<String, String> listItemMap = new HashMap<String, String>();
           // listItemMap.put(fileEntry.getFile_name(), fileEntry.getFile_size());
            String file_name = fileEntry.getFile_name();
            listItemMap.put(FILE_NAME, file_name);

            statistics.append(" " + file_name);

            String file_size = android.text.format.Formatter.formatFileSize(this, fileEntry.getFile_size());
            listItemMap.put(FILE_SIZE, file_size);

            statistics.append("(" + file_size + "),");
            listLargestFiles.add(listItemMap);
        }


        ArrayAdapter adapter = new ArrayAdapter (MainActivity.this, R.layout.list_item_2, R.id.text, listLargestFiles)
       // ArrayAdapter adapter = new ArrayAdapter (MainActivity.this, android.R.layout.simple_list_item_2, android.R.id.text1, listLargestFiles)
        {
         @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(R.id.text);
                TextView text2 = (TextView) view.findViewById(R.id.text2);

             Map<String, String> listItemMapData= listLargestFiles.get(position);
             //row.getText1().setText(listItemMapData.get(FILE_NAME));
             //row.getText2().setText(data.getValue());

                text1.setText(listItemMapData.get(FILE_NAME));
                text2.setText(listItemMapData.get(FILE_SIZE));
                return view;
            }
        };
        listView = (ListView) findViewById(R.id.listView_bigfiles);
        listView.setAdapter(adapter);
    }


    private void updateEXtFreqTable(List<Map<String, String>> listExtFreq){

        TextView textViewExt1 = (TextView) findViewById(R.id.textViewExt1);
        TextView textViewExt2 = (TextView) findViewById(R.id.textViewExt2);
        TextView textViewExt3 = (TextView) findViewById(R.id.textViewExt3);
        TextView textViewExt4 = (TextView) findViewById(R.id.textViewExt4);
        TextView textViewExt5 = (TextView) findViewById(R.id.textViewExt5);



        TextView textViewFreq1 = (TextView) findViewById(R.id.textViewFreq1);
        TextView textViewFreq2 = (TextView) findViewById(R.id.textViewFreq2);
        TextView textViewFreq3 = (TextView) findViewById(R.id.textViewFreq3);
        TextView textViewFreq4 = (TextView) findViewById(R.id.textViewFreq4);
        TextView textViewFreq5 = (TextView) findViewById(R.id.textViewFreq5);

        statistics.append("."+getResources().getString(R.string.freqFiles));


        if(listExtFreq.size() > 0) {
            String ext = listExtFreq.get(0).get(FILE_EXT);
            String freq = listExtFreq.get(0).get(FILE_FREQ);
            textViewExt1.setText(ext);
            textViewFreq1.setText(freq);
            statistics.append(ext + "("+freq +"), ");
        }
        if(listExtFreq.size() > 1) {
            String ext = listExtFreq.get(1).get(FILE_EXT);
            String freq = listExtFreq.get(1).get(FILE_FREQ);
            textViewExt2.setText(ext);
            textViewFreq2.setText(freq);
            statistics.append(ext + "(" + freq + "), ");

        }
        if(listExtFreq.size() > 2){
            String ext = listExtFreq.get(2).get(FILE_EXT);
            String freq = listExtFreq.get(2).get(FILE_FREQ);

            textViewFreq3.setText(ext);
            textViewExt3.setText(listExtFreq.get(2).get(FILE_EXT));
            statistics.append(ext + "(" + freq + "), ");

        }

        if(listExtFreq.size() > 3){
            String ext = listExtFreq.get(3).get(FILE_EXT);
            String freq = listExtFreq.get(3).get(FILE_FREQ);

            textViewExt4.setText(ext);
            textViewFreq4.setText(freq);
            statistics.append(ext + "(" + freq + "), ");

        }

        if(listExtFreq.size() > 4) {
            String ext = listExtFreq.get(4).get(FILE_EXT);
            String freq = listExtFreq.get(4).get(FILE_FREQ);

            textViewExt5.setText(ext);
            textViewFreq5.setText(freq);
            statistics.append(ext + "(" + freq + ").");

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

    public void ShowDialogThread(){
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
//        unbindFileScanService();
        unregisterReceiver(myReceiver);
        super.onStop();
    }


    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {

            updateUI();
            Toast.makeText(MainActivity.this,
                    "Triggered by Service!\n"
                            + "Data passed: " ,
                    Toast.LENGTH_LONG).show();

        }

    }
}
