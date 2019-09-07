package com.cs160.freewheel;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class PlaceDetails extends AppCompatActivity {
    Button back_to_plan_route;
    Button take_me;
    ImageButton rating;

    Double longitude;
    Double latitude;

    String destination_lat;
    String destination_lng;
    String destination_name;
    String destination_id;
    ImageView main_pic;
    JSONObject place_info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_details);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            System.out.println("no extras");
            longitude = 37.8718992;
            latitude = -122.2607339;
            return;
        }
        longitude = extras.getDouble("longitude", 37.8718992);
        latitude = extras.getDouble("latitude", -122.2607339);

        destination_lat = extras.getString("destination_lat", "37.87132310000001");
        destination_lng= extras.getString("destination_lng", "-122.2585858");
        destination_name = extras.getString("destination_name", "Babette South Hall Coffee Bar");
        destination_id = extras.getString("destination_id", "ChIJ--NzyyV8hYARe3Tq719Z_P0");


        TextView name = findViewById(R.id.musashi_header);
        name.setText(destination_name);

        back_to_plan_route = findViewById(R.id.plan_route);
        back_to_plan_route.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), PlanRoute.class);
                intent.putExtra("longitude", longitude);
                intent.putExtra("latitude", latitude);
                startActivity(intent);
            }});

        take_me = findViewById(R.id.button);
        take_me.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), PlanRoute.class);
                intent.putExtra("longitude", longitude);
                intent.putExtra("latitude", latitude);
                intent.putExtra("destination_lat", destination_lat);
                intent.putExtra("destination_lng", destination_lng);
                intent.putExtra("destination_name", destination_name);
                startActivity(intent);
            }});

        rating = findViewById(R.id.stars);
        rating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), UserRating.class);
                intent.putExtra("longitude", longitude);
                intent.putExtra("latitude", latitude);
                intent.putExtra("destination_lat", destination_lat);
                intent.putExtra("destination_lng", destination_lng);
                intent.putExtra("destination_name", destination_name);
                intent.putExtra("destination_id", destination_id);
                startActivity(intent);
            }
        });


        try {
            place_info = new GooglePlaceDetail().execute(destination_id).get();

            try{
                //set hours
                JSONArray hours = place_info.getJSONObject("opening_hours").getJSONArray("weekday_text");
                for (Integer i = 1; i < 7; i++) {
                    String h = hours.getString(i - 1);
                    int text_id = getResources().getIdentifier( "hours_text" + i.toString(), "id", this.getPackageName());
                    TextView hour = findViewById(text_id);
                    hour.setText(h);

                }
            } catch (Exception e){
                Log.e("ERROR", e.getMessage());
            }

            try {
                //set main pic
                main_pic = findViewById(R.id.restaurant_img);
                String photo_ref = place_info.getJSONArray("photos").getJSONObject(0).getString("photo_reference");
                Object[] inputs = {photo_ref, "1500", main_pic};
                new LoadImageViewFromWeb().execute(inputs).get();
                main_pic = findViewById(R.id.restaurant_img);
                String photo_refernce = place_info.getJSONArray("photos").getJSONObject(0).getString("photo_reference");
                Object[] input = {photo_refernce, "1500", main_pic};
                new LoadImageViewFromWeb().execute(input).get();
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage());
            }

            try {
                //set reviews
                JSONArray reviews = place_info.getJSONArray("reviews");
                for (Integer i = 1; i < 4; i++) {
                    //set review text
                    String text = reviews.getJSONObject(i - 1).getString("text");
                    int text_id = getResources().getIdentifier( "google_review" + i.toString(), "id", this.getPackageName());
                    TextView review = findViewById(text_id);
                    review.setText(text);

                    //set reviewer name
                    Integer j = i + 3;
                    String reviewer_name = reviews.getJSONObject(i - 1).getString("author_name");
                    int name_id = getResources().getIdentifier( "user_name" + j.toString(), "id", this.getPackageName());
                    TextView reviewer = findViewById(name_id);
                    reviewer.setText(reviewer_name);
                }

            } catch (Exception e) {
                Log.e("ERROR", e.getMessage());
            }

            try {
                //set ratings
                RatingBar google_rating = findViewById(R.id.google_rating);
                float rating_score = (float) place_info.getDouble("rating");
                System.out.println("rating score");
                System.out.println(rating_score);
                google_rating.setRating(rating_score);
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage());
            }


        } catch (Exception e) {
            Log.e("ERROR", e.getMessage());
        }

        try{
            String acc_user_name = extras.getString("user_name");
            String acc_user_comment = extras.getString("user_comment");
            if (acc_user_name != null) {
                TextView x = findViewById(R.id.user_name1);
                x.setText(acc_user_name);
                TextView y = findViewById(R.id.access_review1);
                y.setText("\"" + acc_user_comment + "\"");
            }
        } catch (Exception e) {
            Log.e("ERROR", e.getMessage());
        }



    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private static class GooglePlaceDetail extends AsyncTask<String, Void, JSONObject> {
        private String API_KEY = "AIzaSyCtzUwL7ejv2N8-5x_sdKvTKKZlA8mOLCU";
        private String query = "https://maps.googleapis.com/maps/api/place/details/json?placeid=";

        protected JSONObject doInBackground(String... inputs) {
            query = query + inputs[0] + "&fields=rating,opening_hours,photo,review&key=" + API_KEY;
            try {
                URL url = new URL(query);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();

                    JSONObject result = new JSONObject(stringBuilder.toString());
                    return result.getJSONObject("result");

                } catch(JSONException e) {
                    Log.e("ERROR", e.getMessage());
                    return null;
                } finally{
                    connection.disconnect();
                }
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

    }

    static class LoadImageViewFromWeb extends AsyncTask<Object, Void, Void> {
        private String API_KEY = "AIzaSyCtzUwL7ejv2N8-5x_sdKvTKKZlA8mOLCU";
        private String max_height;
        private String photo_ref;

        protected Void doInBackground(Object... objects) {
            try {
                photo_ref = (String) objects[0];
                max_height = (String) objects[1];
                String query = "https://maps.googleapis.com/maps/api/place/photo?maxheight=" +
                        max_height + "&photoreference=" + photo_ref + "&key=" + API_KEY;
                ImageView img = (ImageView) objects[2];
                InputStream is = (InputStream) new URL(query).getContent();
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                img.setImageBitmap(bitmap);
                return null;

            } catch (Exception e) {
                Log.e("ERROR", "PlaceDetail:194 " + e.getMessage());
                return null;
            }
        }
    }

//    private String subFolder = "/user_comment_data";
//    private String file = "data.ser";

//public HashMap<String, Stack<String[]>> user_comments;

//    public void readSetttings() {
//        File cacheDir = null;
//        File appDirectory = null;
//        if (android.os.Environment.getExternalStorageState().
//                equals(android.os.Environment.MEDIA_MOUNTED)) {
//            cacheDir = getApplicationContext().getExternalCacheDir();
//            appDirectory = new File(cacheDir + subFolder);
//        } else {
//            cacheDir = getApplicationContext().getCacheDir();
//            String BaseFolder = cacheDir.getAbsolutePath();
//            appDirectory = new File(BaseFolder + subFolder);
//        }
//
//        if (appDirectory != null && !appDirectory.exists()) return; // File does not exist
//
//        File fileName = new File(appDirectory, file);
//
//        FileInputStream fis = null;
//        ObjectInputStream in = null;
//        try {
//            fis = new FileInputStream(fileName);
//            in = new ObjectInputStream(fis);
//            HashMap<String, Stack<String[]>> myHashMap = (HashMap<String,Stack<String[]>> ) in.readObject();
//            user_comments = myHashMap;
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (StreamCorruptedException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }finally {
//
//            try {
//                if(fis != null) {
//                    fis.close();
//                }
//                if(in != null) {
//                    in.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

//    public HashMap<String, Stack<String[]>> getUserComment() {
//        return user_comments;
//    }
}