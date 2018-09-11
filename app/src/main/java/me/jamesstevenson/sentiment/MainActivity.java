package me.jamesstevenson.sentiment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    /**
     * @param context used to check the device version and DownloadManager information
     * @return true if the download manager is available
     */
    // The below code is used as part of the download manager to download the files with posiitve sores in.
    public static boolean isDownloadManagerAvailable(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return true;
        }
        return false;
    }

    @SuppressLint("SetTextI18n")

    // The below code was taken from the Storage app that allows us to loop through the directory and list files.
    public void viewDirectory(File directory){

        // globally
        final ListView lv = (ListView) findViewById(R.id.lv);

        // Initializing a new String Array
        String[] listItems = new String[] {};



        // Create a List from String Array elements
        final List<String> dir_list = new ArrayList<String>(Arrays.asList(listItems));

        // Create an ArrayAdapter from List
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, dir_list);

        // DataBind ListView with items from ArrayAdapter
        lv.setAdapter(arrayAdapter);

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Requests permission

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);



        } else {
            //Peermission is set

            //Clears array
            arrayAdapter.clear();
            arrayAdapter.notifyDataSetChanged();

            //Reads files in the filepath and adds to list/ array
            File dir = new File(String.valueOf(directory));
            if (dir.exists()) {
                File[] files = dir.listFiles();
                for (int i = 0; i < files.length; ++i) {
                    File file = files[i];
                    File file_to_read = new File(directory+"/"+file.toString());

                    //Checks if the file is a real file.
                    if (file.isFile()){
                        FileReader fReader = null;
                        //Reads from the file
                        try {
                            fReader = new FileReader(file.getPath());
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        BufferedReader bReader = new BufferedReader(fReader);
                        StringBuilder text = new StringBuilder();

                        try {
                            String line = "";
                            while( (line = bReader.readLine()) != null  ){
                                text.append(line+"\n");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }



                        // Here we set a flag to true if we are currently reading its file.
                        // and in turn enter the IF statement.
                        boolean war_correct = file.getName().equals("War_Results");
                        if (war_correct){

                            Integer amount_Positive =  Integer.parseInt(String.valueOf(text.toString().replace("\n","")));
                            // As part of the sentiment analysis there is a large bias towards negative sentiment. (This may be because negative statments have more words).
                            // That being the case, positive sengtiments have double the value.
                            amount_Positive = amount_Positive * 2;
                            if (amount_Positive > 100) {
                                amount_Positive = 100;
                            }

                            // Creates a text view for the speciifc keyword
                            TextView WarTextView = (TextView)findViewById(R.id.war_TW);
                            WarTextView.setText(amount_Positive.toString().replace("\n","")+"/100");

                            // Next we read the file and set the color of the text view depending on the number.
                            try{
                                if (amount_Positive >= 50){
                                    WarTextView.setTextColor(Color.GREEN);
                                }else{
                                    WarTextView.setTextColor(Color.RED);
                                }
                            } catch(NumberFormatException nfe){}

                        }

                        // Below we do the same as the above but for the other keywords.

                        boolean pol_correct = file.getName().equals("Politics_Results");
                        if (pol_correct){

                            Integer amount_Positive =  Integer.parseInt(String.valueOf(text.toString().replace("\n","")));
                            // As part of the sentiment analysis there is a large bias towards negative sentiment. (This may be because negative statments have more words).
                            // That being the case, positive sengtiments have double the value.
                            amount_Positive = amount_Positive * 2;
                            if (amount_Positive > 100) {
                                amount_Positive = 100;
                            }

                            TextView PolTextView = (TextView)findViewById(R.id.pol_TW);
                            PolTextView.setText(amount_Positive.toString().replace("\n","")+"/100");

                            try{
                                if (amount_Positive >= 50){
                                    PolTextView.setTextColor(Color.GREEN);
                                }else{
                                    PolTextView.setTextColor(Color.RED);
                                }
                            } catch(NumberFormatException nfe){}


                        }

                        boolean mon_correct = file.getName().equals("Money_Results");
                        if (mon_correct){

                            Integer amount_Positive =  Integer.parseInt(String.valueOf(text.toString().replace("\n","")));
                            // As part of the sentiment analysis there is a large bias towards negative sentiment. (This may be because negative statments have more words).
                            // That being the case, positive sengtiments have double the value.
                            amount_Positive = amount_Positive * 2;
                            if (amount_Positive > 100) {
                                amount_Positive = 100;
                            }

                            TextView MonTextView = (TextView)findViewById(R.id.mon_TV);
                            MonTextView.setText(amount_Positive.toString().replace("\n","")+"/100");


                            try{
                                if (amount_Positive >= 50){
                                    MonTextView.setTextColor(Color.GREEN);
                                }else{
                                    MonTextView.setTextColor(Color.RED);
                                }
                            } catch(NumberFormatException nfe){}

                        }

                        boolean fam_correct = file.getName().equals("Family_Results");
                        if (fam_correct){

                            Integer amount_Positive =  Integer.parseInt(String.valueOf(text.toString().replace("\n","")));
                            // As part of the sentiment analysis there is a large bias towards negative sentiment. (This may be because negative statments have more words).
                            // That being the case, positive sengtiments have double the value.
                            amount_Positive = amount_Positive * 2;
                            if (amount_Positive > 100) {
                                amount_Positive = 100;
                            }

                            TextView FamTextView = (TextView)findViewById(R.id.fam_TV);
                            FamTextView.setText(amount_Positive.toString().replace("\n","")+"/100");

                            try{
                                if (amount_Positive >= 50){
                                    FamTextView.setTextColor(Color.GREEN);
                                }else{
                                    FamTextView.setTextColor(Color.RED);
                                }
                            } catch(NumberFormatException nfe){}

                        }

                        boolean coo_correct = file.getName().equals("Cooking_Results");
                        if (coo_correct){

                            Integer amount_Positive =  Integer.parseInt(String.valueOf(text.toString().replace("\n","")));
                            // As part of the sentiment analysis there is a large bias towards negative sentiment. (This may be because negative statments have more words).
                            // That being the case, positive sengtiments have double the value.
                            amount_Positive = amount_Positive * 2;
                            if (amount_Positive > 100) {
                                amount_Positive = 100;
                            }

                            TextView CooTextView = (TextView)findViewById(R.id.cooking_TV);
                            CooTextView.setText(amount_Positive.toString().replace("\n","")+"/100");

                            try{
                                if (amount_Positive >= 50){
                                    CooTextView.setTextColor(Color.GREEN);
                                }else{
                                    CooTextView.setTextColor(Color.RED);
                                }
                            } catch(NumberFormatException nfe){}

                        }

                        boolean fas_correct = file.getName().equals("Fashion_Results");
                        if (fas_correct){

                            Integer amount_Positive =  Integer.parseInt(String.valueOf(text.toString().replace("\n","")));
                            // As part of the sentiment analysis there is a large bias towards negative sentiment. (This may be because negative statments have more words).
                            // That being the case, positive sengtiments have double the value.
                            amount_Positive = amount_Positive * 2;
                            if (amount_Positive > 100) {
                                amount_Positive = 100;
                            }

                            TextView FasTextView = (TextView)findViewById(R.id.fashion_TV);
                            FasTextView.setText(amount_Positive.toString().replace("\n","")+"/100");

                            try{
                                if (amount_Positive >= 50){
                                    FasTextView.setTextColor(Color.GREEN);
                                }else{
                                    FasTextView.setTextColor(Color.RED);
                                }
                            } catch(NumberFormatException nfe){}

                        }

                        boolean tech_correct = file.getName().equals("Technology_Results");
                        if (tech_correct){

                            Integer amount_Positive =  Integer.parseInt(String.valueOf(text.toString().replace("\n","")));
                            // As part of the sentiment analysis there is a large bias towards negative sentiment. (This may be because negative statments have more words).
                            // That being the case, positive sengtiments have double the value.
                            amount_Positive = amount_Positive * 2;
                            if (amount_Positive > 100) {
                                amount_Positive = 100;
                            }

                            TextView TechTextView = (TextView)findViewById(R.id.tech_TV);
                            TechTextView.setText(amount_Positive.toString().replace("\n","")+"/100");

                            try{
                                if (amount_Positive >= 50){
                                    TechTextView.setTextColor(Color.GREEN);
                                }else{
                                    TechTextView.setTextColor(Color.RED);
                                }
                            } catch(NumberFormatException nfe){}

                        }

                    }
                }

                //arrayAdapter.notifyDataSetChanged();
            }
        }
    }

    // The below function is used to download the files needed as part of displaying the sentiment data.
    // Takes in a filename and string for the file to be called.
    // The below uses the Android Download manager.
    public void download(String filename,String url){

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription("A file used for sentiment analysis from Twitter data.");
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // When the download FAB is selected
        FloatingActionButton download_fab = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        download_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Downloading Updated Analysed Tweets...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();


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
                    //Peermission is set

                    File dir = new File(String.valueOf(Environment.getExternalStorageDirectory()+"/Sentiment_Files"));
                    if (dir.exists()) {
                        File[] files = dir.listFiles();
                        for (int i = 0; i < files.length; ++i) {
                            File file = files[i];
                            file.delete();
                            // do something here with the file
                        }
                    }

                    // Downloads the needed sentiment files.
                    download("Fashion_Results","https://s3.eu-west-2.amazonaws.com/sentimentanalysispythonandroid/fashion.txt");
                    download("War_Results","https://s3.eu-west-2.amazonaws.com/sentimentanalysispythonandroid/war.txt");
                    download("Politics_Results","https://s3.eu-west-2.amazonaws.com/sentimentanalysispythonandroid/politics.txt");
                    download("Money_Results","https://s3.eu-west-2.amazonaws.com/sentimentanalysispythonandroid/money.txt");
                    download("Family_Results","https://s3.eu-west-2.amazonaws.com/sentimentanalysispythonandroid/family.txt");
                    download("Cooking_Results","https://s3.eu-west-2.amazonaws.com/sentimentanalysispythonandroid/cooking.txt");
                    download("Technology_Results","https://s3.eu-west-2.amazonaws.com/sentimentanalysispythonandroid/technology.txt");
                }


            }
        });

        // FAB for when the user wants to update their statistics.
        FloatingActionButton update_fab = (FloatingActionButton) findViewById(R.id.floatingActionButton2);
        update_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Updating Statistics...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                viewDirectory(new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/Sentiment_files"));
            }
            });

    }
}
