package com.cs160.freewheel;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.Stack;

public class UserRating extends AppCompatActivity {
    private RatingBar ratingBar;
    private Button btnSubmit;
    Float rating_score; //haven't decide what to do with the scores and comment
    EditText user_name;
    EditText user_comment;

    Double longitude;
    Double latitude;
    String destination_lat;
    String destination_lng;
    String destination_name;
    String destination_id;

    private String subFolder = "/user_comment_data";
    private String file = "data.ser";
    HashMap<String, Stack<String[]>> comment_storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_rating);


        user_name = findViewById(R.id.user_rating_name);
        user_comment = findViewById(R.id.user_rating_comment);

        Bundle extras = getIntent().getExtras();
        longitude = extras.getDouble("longitude", 37.8718992);
        latitude = extras.getDouble("latitude", -122.2607339);

        destination_lat = extras.getString("destination_lat", "37.87132310000001");
        destination_lng= extras.getString("destination_lng", "-122.2585858");
        destination_name = extras.getString("destination_name", "Babette South Hall Coffee Bar");
        destination_id = extras.getString("destination_id", "ChIJ--NzyyV8hYARe3Tq719Z_P0");

        btnSubmit = findViewById(R.id.sub_rating_btn);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String userName = user_name.getText().toString(); //maybe null
                String userComment = user_comment.getText().toString(); //maybe null
//                comment_storage = new HashMap<>();
//                Stack<String[]> comment_line = new Stack<>();
//                String[] comments = {userName, userComment};
//                comment_line.push(comments);
//                comment_storage.put(destination_id, comment_line);
//
//                String filename = "myfile";
//                String fileContents = "Hello world!";
//                FileOutputStream outputStream;
//
//                try {
//                    outputStream = openFileOutput(filename, UserRating.MODE_PRIVATE);
//                    outputStream.writeObject(comment_storage);
//                    outputStream.close();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                try {
//
//                    ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("data.ser"));
//                    out.writeObject(comment_storage);
//                    out.close();
//                } catch (Exception e) {
//                    Log.e("ERROR", e.getMessage());
//                }


//
//                Properties properties = new Properties();
//                properties.putAll(comment_storage);
//                try {
//                    properties.store(new FileOutputStream("user_comment_data.ser"), "user comments");
//                    System.out.println("finish writing");
//                } catch (Exception e) {
//                    Log.e("ERROR", e.getMessage());
//                }
//                writeSettings();

                Intent intent = new Intent(getBaseContext(), PlaceDetails.class);
                intent.putExtra("destination_lat", destination_lat);
                intent.putExtra("destination_lng", destination_lng);
                intent.putExtra("destination_name", destination_name);
                intent.putExtra("destination_id", destination_id);
                intent.putExtra("user_name", userName);
                intent.putExtra("user_comment", userComment);
                startActivity(intent);
            }
        });

        addListenerOnRatingBar();
    }
//    public void writeFileOnInternalStorage(Context mcoContext, String sFileName, HashMap sBody){
//        File file = new File(mcoContext.getFilesDir(),"mydir");
//        if(!file.exists()){
//            file.mkdir();
//        }
//
//        try{
//            File gpxfile = new File(file, sFileName);
//            FileWriter writer = new FileWriter(gpxfile);
//            writer.append(sBody);
//            writer.flush();
//            writer.close();
//
//        }catch (Exception e){
//            e.printStackTrace();
//
//        }
//    }

//    public void writeSettings() {
//        File cacheDir = null;
//        File appDirectory = null;
//
//        if (android.os.Environment.getExternalStorageState().
//                equals(android.os.Environment.MEDIA_MOUNTED)) {
//            cacheDir = getApplicationContext().getExternalCacheDir();
//            appDirectory = new File(cacheDir + subFolder);
//
//        } else {
//            cacheDir = getApplicationContext().getCacheDir();
//            String BaseFolder = cacheDir.getAbsolutePath();
//            appDirectory = new File(BaseFolder + subFolder);
//
//        }
//
//        if (appDirectory != null && !appDirectory.exists()) {
//            appDirectory.mkdirs();
//        }
//
//        File fileName = new File(appDirectory, file);
//
//        FileOutputStream fos = null;
//        ObjectOutputStream out = null;
//        try {
//            fos = new FileOutputStream(fileName);
//            out = new ObjectOutputStream(fos);
//            out.writeObject(comment_storage);
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (fos != null)
//                    fos.flush();
//                fos.close();
//                if (out != null)
//                    out.flush();
//                out.close();
//            } catch (Exception e) {
//
//            }
//        }
//    }
//

    public void addListenerOnRatingBar() {
        ratingBar = findViewById(R.id.user_rating_bar);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating,
                                        boolean fromUser) {
                rating_score = rating;
            }
        });
    }
}