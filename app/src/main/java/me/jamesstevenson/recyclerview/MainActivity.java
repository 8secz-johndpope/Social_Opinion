package me.jamesstevenson.recyclerview;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<sentiment> sentimentList = new ArrayList<>();
    private RecyclerView recyclerView;
    private sentimentsAdapter mAdapter;

    /**
     * This function is used to add items to the recycler view after they have been read from a JSON file.
     * @param name
     * @param score
     * @param Date
     * @param change
     */
    private void preparesentimentData(String name, String score, String Date, String change) {
        // Checks if the positive score is more than 100, if so sets it to 100.
        // This is doubled as there is a bias in negative scores.
        Integer amount_Positive =  Integer.parseInt(String.valueOf(score.toString().replace("\n","")));
        amount_Positive = amount_Positive *2;
        if (amount_Positive> 100){
            amount_Positive = 100;
        }
        sentiment sentiment = new sentiment(name, amount_Positive+"/100 Positive Comments | " + change, Date);
        sentimentList.add(sentiment);

        // notify adapter about data set changes
        // so that it will render the list with new data
        mAdapter.notifyDataSetChanged();
    }

    /**
     *  The download function uses the Android download manager to download a JSON file from an S3 bucket.
     *
     * @param filename
     * @param url
     * @param Description
     */
    public void download(String filename,String url, String Description){

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription(Description);
        request.setTitle(filename);
        // in order for this if to run, you must use the android 3.2 to compile your app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }
        request.setDestinationInExternalPublicDir("Sentiment_Files", filename);

        // get download service and enqueue file
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        assert manager != null;
        manager.enqueue(request);
    }

    /**
     *  This function initialises the app before downloading.
    * It makes sure that the correct permission is set and that the folder the files will be downaloded to are empty.
    * */
    public void startDownload(){
        // Checks if the user has the write to external storage permission.
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Requests permission

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);


            // Tell the user to try again.

        } else {
            // LOops through directory deleting previous files.
            File dir = new File(String.valueOf(Environment.getExternalStorageDirectory() + "/Sentiment_Files"));
            if (dir.exists()) {
                File[] files = dir.listFiles();
                for (int i = 0; i < files.length; ++i) {
                    File file = files[i];
                    file.delete();
                    // do something here with the file
                }
            }
            // Runs the download function with the s3 bucket url.
            download("All_Results.json","https://s3.eu-west-2.amazonaws.com/sentimentanalysispythonandroid/All_Results.json","Analysed Twitter sentiment data.");
        }


    }

    /**
     *  This function is parsed the location of the json file and returns it as a json object.
     * @param JsonFile
     * @return
     */
    public JSONObject CreateJsonFile (File JsonFile) {
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(JsonFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String jsonStr = null;
        try {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

            jsonStr = Charset.defaultCharset().decode(bb).toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            JSONObject jsonObj = new JSONObject(jsonStr);

            return jsonObj;

        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toast.makeText(getApplicationContext(), "Swipe down to update...", Toast.LENGTH_LONG).show();

        // Initialise the Recycler view
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mAdapter = new sentimentsAdapter(sentimentList);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        // Is used so that the user can update the app by swiping down.
        final SwipeRefreshLayout swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                sentimentList.clear();

                // Checks that the external storage permission is set as otherwise the update icon woun't be told to leave as
                // no file will be downaoded.
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    swipeContainer.setRefreshing(false);

                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1);

                }else {
                    startDownload();
                }
            }
        });

        // Is run when a file is downaloded. In this case this is run when the JSON file is downaloded.
        BroadcastReceiver onComplete=new BroadcastReceiver() {
            public void onReceive(Context ctxt, Intent intent) {

                // Turns off the update refresh icon.
                swipeContainer.setRefreshing(false);

                // Uses the aformentioned function to create a json onject.
                String Path = Environment.getExternalStorageDirectory() + "/Sentiment_Files/All_Results.json";
                File JsonFile = new File(Path);
                JSONObject Json = CreateJsonFile(JsonFile);

                // Reads the json object.
                JSONArray data  = null;
                try {
                    data = Json.getJSONArray("tag");
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                // looping through All nodes and sets the coresponding variables.
                for (int i = 0; i < data.length(); i++) {
                    JSONObject c = null;
                    try {
                        c = data.getJSONObject(i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    String name = null;
                    try {
                        name = c.getString("name");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String score = null;
                    try {
                        score = c.getString("score");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String time = null;
                    try {
                        time = c.getString("time");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String change = null;
                    try {
                        change = c.getString("change");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    preparesentimentData(name,score,time,change);

                }


            }
        };

        // Used to update the app when gthe JSON file in downloaded.
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));


    }

}
