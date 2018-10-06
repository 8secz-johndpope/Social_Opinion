package me.jamesstevenson.sentiment;

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
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import java.util.List;

import static android.provider.AlarmClock.EXTRA_MESSAGE;


public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "me.jamesstevenson.sentiment.MESSAGE";
    private List<sentiment> sentimentList = new ArrayList<>();
    private RecyclerView recyclerView;
    private sentimentsAdapter mAdapter;

    // The download flags are used to define what has been downaloded when the broadcast receiver is used.
    public String downloadTypeFlag;
    public String downloadTypeFlagExtraMessage;

    /**
     * This function is used to add items to the recycler view after they have been read from a JSON file.
     *
     * @param name
     * @param score
     * @param Date
     * @param change
     */
    private void preparesentimentData(String name, String score, String Date, String change) {
        // Checks if the positive score is more than 100, if so sets it to 100.
        // This is doubled as there is a bias in negative scores.
        Integer amount_Positive = Integer.parseInt(String.valueOf(score.toString().replace("\n", "")));
        //amount_Positive = amount_Positive * 2;
        if (amount_Positive > 100) {
            amount_Positive = 100;
        }

        //Creates a state variable, used as a high level look at the sentiment.
        String state;

        if (amount_Positive < 10){
            state = "Very Low";
        }else if (amount_Positive < 15){
            state = "Low";
        }else if(amount_Positive < 35) {
            state = "Average";
        }else if(amount_Positive < 50) {
            state = "Good";
        }else {
            state = "Very Good";
        }

        sentiment sentiment = new sentiment(name, state.toUpperCase() + " - " + amount_Positive + "% | " + change, Date);
        sentimentList.add(sentiment);

        // notify adapter about data set changes
        // so that it will render the list with new data
        mAdapter.notifyDataSetChanged();
    }

    /**
     * The download function uses the Android download manager to download a JSON file from an S3 bucket.
     *
     * @param filename
     * @param url
     * @param Description
     */
    public void download(String filename, String url, String Description) {

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
     * This function initialises the app before downloading.
     * It makes sure that the correct permission is set and that the folder the files will be downaloded to are empty.
     */
    public void startDownload() {
        // Checks if the user has the write to external storage permission.
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Requests permission

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);

            Toast.makeText(getApplicationContext(), "Please Try Again...", Toast.LENGTH_LONG).show();
            // Tell the user to try again.

        } else {
            // Loops through directory deleting previous files.
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
            downloadTypeFlag = "json";
            download("All_Results.json", "https://s3.eu-west-2.amazonaws.com/sentimentanalysispythonandroid/All_Results.json", "Analysed Twitter sentiment data.");
        }


    }

    /**
     * This function is parsed the location of the json file and returns it as a json object.
     *
     * @param JsonFile
     * @return
     */
    public JSONObject CreateJsonFile(File JsonFile) {
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


    /**
     * On create function
     * @param savedInstanceState
     */
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

        /**
         * Used to respond to the user when they select an item in the recycler view.
         * This will then download an image relating to the name of the item and open it in an imge viewer.
         */
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {

                // Removes all pre existing images in the sentiment folder.
                File dir = new File(String.valueOf(Environment.getExternalStorageDirectory() + "/Sentiment_Files"));
                if (dir.exists()) {
                    File[] files = dir.listFiles();
                    for (int i = 0; i < files.length; ++i) {
                        File file = files[i];
                        if (file.getPath().endsWith("png")) {
                            file.delete();
                        }
                        // do something here with the file
                    }
                }

                // Accesses the name of the selected item in the recyler view.
                sentiment sen = sentimentList.get(position);
                String fileName = sen.getName();

                //Downloads an image file relating to the selected item.
                String filePath = Environment.getExternalStorageDirectory() + File.separator + "Sentiment_Files" + File.separator + fileName + "_graph.png";
                File file = new File(filePath);
                downloadTypeFlag = "image";
                downloadTypeFlagExtraMessage = fileName;
                download(fileName + "_Graph.png", "https://s3.eu-west-2.amazonaws.com/sentimentanalysispythonandroid/" + fileName + "_graph.png", "An image of the " + fileName + " graph.");


            }

            /**
             * This is a placeholder for long clicks in the future.
             * @param view
             * @param position
             */
            @Override
            public void onLongClick(View view, int position) {

            }
        }));

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

                } else {
                    startDownload();
                }
            }
        });

        // Is run when a file is downaloded. In this case this is run when the JSON file is downaloded.
        BroadcastReceiver onComplete = new BroadcastReceiver() {
            public void onReceive(Context ctxt, Intent intent) {

                //Checks if the app recently downaloded an image.
                if (downloadTypeFlag == "image") {
                    // Sets the filename to the extra message that would have been defined when the downlaod started.
                    String fileName = downloadTypeFlagExtraMessage;

                    // Sets a filepath and uses an intent to open the recently downaloded image in an image viewer.
                    String filePath = Environment.getExternalStorageDirectory() + File.separator + "Sentiment_Files" + File.separator + fileName + "_Graph.png";
                    File file = new File(filePath);
                    if (file.exists()) {
                        Intent intentForImage = new Intent();
                        intentForImage.setAction(Intent.ACTION_VIEW);

                        Uri apkURI = FileProvider.getUriForFile(
                                getApplicationContext(),
                                getApplicationContext().getApplicationContext()
                                        .getPackageName() + ".provider", file);
                        intentForImage.setDataAndType(apkURI, "image/*");
                        intentForImage.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        startActivity(intentForImage);
                    }
                }

                // Checks if the app recently downaloded a json file.
                if (downloadTypeFlag == "json") {
                    // Turns off the update refresh icon.
                    swipeContainer.setRefreshing(false);

                    // Uses the aformentioned function to create a json onject.
                    String Path = Environment.getExternalStorageDirectory() + "/Sentiment_Files/All_Results.json";
                    File JsonFile = new File(Path);
                    JSONObject Json = CreateJsonFile(JsonFile);

                    // Reads the json object.
                    JSONArray data = null;
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

                        preparesentimentData(name, score, time, change);

                    }
                }


            }
        };

            // Used to update the app when a file file in downloaded.
            registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));


        }


}
