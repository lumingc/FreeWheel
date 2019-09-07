package com.cs160.freewheel;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

public class ExplorePlaces extends AppCompatActivity {
    Button plan_route;
    Button map;
    final int REQUEST_LOCATION_PERMISSION = 100;
    Location mLastLocation;
    Double latitude;
    Double longitude;

    ArrayList<String> destination_latitudes;
    ArrayList<String> destination_longitudes;
    ArrayList<String> destination_names;
    ArrayList<String> destination_id;

    FusedLocationProviderClient mFusedLocationClient;
    String[] types = {"restaurant", "cafe", "movie_theater", "shopping_mall", "bar"};
    ArrayList<JSONArray> places_info = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore_places);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLocation();

        plan_route = findViewById(R.id.plan_route);
        map = findViewById(R.id.map);

        destination_latitudes = new ArrayList<>();
        destination_longitudes = new ArrayList<>();
        destination_names = new ArrayList<>();
        destination_id = new ArrayList<>();

        plan_route.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), PlanRoute.class);
                intent.putExtra("longitude", longitude);
                intent.putExtra("latitude", latitude);
                startActivity(intent);
            }
        });

    }

    public void placeDetails(View v) {
        int imageTag = Integer.parseInt(v.getTag().toString());

        Intent intent = new Intent(getBaseContext(), PlaceDetails.class);
        intent.putExtra("longitude", longitude);
        intent.putExtra("latitude", latitude);

        intent.putExtra("destination_lat", destination_latitudes.get(imageTag));
        intent.putExtra("destination_lng", destination_longitudes.get(imageTag));
        intent.putExtra("destination_name", destination_names.get(imageTag));

        intent.putExtra("destination_id", destination_id.get(imageTag));

        //intent.putExtra()

        startActivity(intent);

    }

    public void placeList(View v) {

        Intent intent = new Intent(getBaseContext(), ListOfPlaces.class);

        startActivity(intent);

    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(
                    new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                mLastLocation = location;

                                latitude = mLastLocation.getLatitude();
                                longitude = mLastLocation.getLongitude();
                                System.out.println(latitude.toString());
                                System.out.println(longitude.toString());

                                // CALL ANY FUNCTION BASE ON THE LATITUDE AND LONGITUDE OF THE USER
                                getNearestLocation(latitude, longitude);
                                displayRecommendedPlaces();


                            } else {
                                AlertDialog alertDialog = new AlertDialog.Builder(ExplorePlaces.this).create();
                                alertDialog.setTitle("Alert");
                                alertDialog.setMessage("Fetching Location Failed");
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                alertDialog.show();

                            }
                        }
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                // If the permission is granted, get the location,
                // otherwise, show a Toast
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("got permission");
                    getLocation();
                } else {
                    Toast.makeText(this,
                            "location_permission_denied",
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    //this method calls the GooglePlaceSearch task for each of the place types
    private void getNearestLocation(Double latitude, Double longitude) {
        try {
            for (String type: types) {
                String[] inputs = new String[3];
                inputs[0] = latitude.toString();
                inputs[1] = longitude.toString();
                inputs[2] = type;
                // how many places to display
                JSONArray place_info =  new GooglePlaceSearch().execute(inputs).get();
                places_info.add(place_info);
            }
        } catch (Exception e) {
            Log.e("ERROR", "ExplorePlaces:179 " + e.getMessage());
        }
    }

    //This method displays the name and photo for the recommended place on ExplorePlace page
    private void displayRecommendedPlaces() {
        for (Integer j = 0; j < 2; j++) {
            for (Integer i  = 1; i < 7; i++) {
                int img_id = getResources().getIdentifier(types[j] + i.toString() + "_img", "id", this.getPackageName());
//                System.out.println(types[j] + i.toString() + "_img");
//                System.out.println(img_id);
                ImageButton photo = findViewById(img_id);
                int text_id = getResources().getIdentifier(types[j] + i.toString() + "_text", "id", this.getPackageName());
                TextView text = findViewById(text_id);
//                System.out.println(text_id);
                try {
                    JSONObject place =  places_info.get(j).getJSONObject(i);
                    // set place name
                    String name = place.getString("name");
                    text.setText(name);
                    destination_names.add(name);
                    destination_id.add(place.getString("place_id"));
//                    System.out.println(destination_names);

                    // get lat/lng
                    String lat = place.getJSONObject("geometry").getJSONObject("location").getString("lat");
                    String lng = place.getJSONObject("geometry").getJSONObject("location").getString("lng");

                    destination_latitudes.add(lat);
                    destination_longitudes.add(lng);


                    //set place image
                    String photo_ref = place.getJSONArray("photos").getJSONObject(0).getString("photo_reference");
                    Object[] inputs = {photo_ref, "450", photo};
                    new LoadImageFromWeb().execute(inputs).get();

                } catch (Exception e) {
                    Log.e("ERROR", "ExplorePlaces:218 " + e.getMessage());
                }

            }
        }
    }

    //This class fetch the Json output of nearby search and return the Json
    private static class GooglePlaceSearch extends AsyncTask<String, Void, JSONArray> {
        private String API_KEY = "AIzaSyCtzUwL7ejv2N8-5x_sdKvTKKZlA8mOLCU";
        private String latitude;
        private String longitude;
        private String type;
        private String query = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";

        @Override
        protected JSONArray doInBackground(String... inputs) {

            try {
                latitude = inputs[0];
                longitude = inputs[1];
                type = inputs[2];
                query = query + "location=" + latitude + "," + longitude + "&rankby=distance&type=" + type + "&key=" + API_KEY;
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
                    return result.getJSONArray("results");

                } catch(JSONException e) {
                    Log.e("ERROR", e.getMessage());
                    return null;
                } finally{
                    connection.disconnect();
                }
            } catch (Exception e) {
                Log.e("ERROR", "ExplorePlaces:262 " + e.getMessage(), e);
                return null;
            }
        }

    }

    //This class fetch the image and put the image inside the image button
    static class LoadImageFromWeb extends AsyncTask<Object, Void, Void> {
        private String API_KEY = "AIzaSyCtzUwL7ejv2N8-5x_sdKvTKKZlA8mOLCU";
        private String max_height;
        private String photo_ref;

        protected Void doInBackground(Object... objects) {
            try {
                photo_ref = (String) objects[0];
                max_height = (String) objects[1];
                String query = "https://maps.googleapis.com/maps/api/place/photo?maxheight=" +
                        max_height + "&photoreference=" + photo_ref + "&key=" + API_KEY;
                ImageButton img = (ImageButton) objects[2];
                InputStream is = (InputStream) new URL(query).getContent();
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                img.setImageBitmap(bitmap);
                return null;

            } catch(Exception e) {
                Log.e("ERROR", "ExplorePlaces:288 " + e.getMessage());
                return null;
            }
        }

    }
}