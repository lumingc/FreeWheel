package com.cs160.freewheel;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.AsyncTask;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


public class PlanRoute extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    Button explore_places;

    String api_key = "AIzaSyCtzUwL7ejv2N8-5x_sdKvTKKZlA8mOLCU";

    Double longitude;
    Double latitude;

    String destination_lat;
    String destination_lng;
    String destination_name;

    //Marker start_marker;
    //Marker destination_marker;
    MarkerOptions destination_options;
    MarkerOptions start_options;

    ArrayList<LatLng> markerPoints;
    ArrayList<String> route_info;

    private BottomSheetBehavior mBottomSheetBehavior;

    boolean hasDetails;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_route);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        explore_places = findViewById(R.id.explore_places);
        explore_places.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), ExplorePlaces.class);
                startActivity(intent);
            }
        } );

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            System.out.println("no extras");
            longitude = 37.8718992;
            latitude = -122.2607339;
            return;
        }
        longitude = extras.getDouble("longitude", 37.8718992);
        latitude = extras.getDouble("latitude", -122.2607339);

        hasDetails = false;

        destination_lat = extras.getString("destination_lat", "0");
        destination_lng= extras.getString("destination_lng", "0");
        destination_name = extras.getString("destination_name", "Musashi");


        Fragment fragment = (Fragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        fragment.getView().setBackgroundColor(getResources().getColor(R.color.colorPrimaryLight));
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setHint("Your Location");

        Fragment fragment2 = (Fragment) getFragmentManager().findFragmentById(R.id.autocomplete_destination);

        fragment2.getView().setBackgroundColor(getResources().getColor(R.color.colorPrimaryLight));
        PlaceAutocompleteFragment autocompleteFragment2 = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.autocomplete_destination);
        autocompleteFragment2.setHint("Enter a location here");


        ((EditText)fragment.getView().findViewById(R.id.place_autocomplete_search_input)).setTextColor(getResources().getColor(R.color.white));
        ((EditText)fragment2.getView().findViewById(R.id.place_autocomplete_search_input)).setTextColor(getResources().getColor(R.color.white));
        ((EditText)fragment.getView().findViewById(R.id.place_autocomplete_search_input)).setTextSize(14.0f);
        ((EditText)fragment2.getView().findViewById(R.id.place_autocomplete_search_input)).setTextSize(14.0f);
        ((EditText)fragment.getView().findViewById(R.id.place_autocomplete_search_input)).setEllipsize(TextUtils.TruncateAt.END);
        ((EditText)fragment2.getView().findViewById(R.id.place_autocomplete_search_input)).setEllipsize(TextUtils.TruncateAt.END);

        /*
        ((EditText)fragment.getView().findViewById(R.id.place_autocomplete_search_input)).setLines(1);
        ((EditText)fragment.getView().findViewById(R.id.place_autocomplete_search_input)).setSingleLine();
        ((EditText)fragment.getView().findViewById(R.id.place_autocomplete_search_input)).setCompoundDrawablePadding(2);
        */

        destination_options = null;
        start_options = null;
        //start_marker = null;
        //destination_marker = null;

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i("autocomplete", "Place: " + place.getName());
                Log.i("autocomplete", "latlng: " + place.getLatLng());


                map.clear();
                // add current location
                LatLng currentLocation = new LatLng(latitude, longitude);
                MarkerOptions curr_options = new MarkerOptions();
                curr_options.position(currentLocation);
                curr_options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                curr_options.title("Your location");
                map.addMarker(curr_options);

                // add start marker
                markerPoints = new ArrayList<LatLng>();
                markerPoints.clear();
                markerPoints.add(currentLocation);



                LatLng start = place.getLatLng();
                markerPoints.add(start);
                // if there is already a destination
                if (destination_options != null) {
                    markerPoints.add(destination_options.getPosition());
                    map.addMarker(destination_options);
                }


                MarkerOptions options = new MarkerOptions();

                options.position(start);
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                options.title("Start point: " + place.getName());
                options.snippet("Accessibility Review: 4." + (new Random()).nextInt(9 - 0));

                Marker start_m = map.addMarker(options);
                start_options = options;
                start_m.showInfoWindow();
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(start,15));
                map.animateCamera(CameraUpdateFactory.zoomIn());
                map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);

                // draw path if there is already a destination
                if (markerPoints.size() > 2){
                    map.addMarker(destination_options);
                    LatLng origin = markerPoints.get(1);
                    LatLng dest = markerPoints.get(2);

                    String url = getDirectionsUrl(origin, dest);

                    DownloadTask downloadTask = new DownloadTask();

                    downloadTask.execute(url);
                    mBottomSheetBehavior.setPeekHeight(400);
                    hasDetails = true;
                }

            }

            @Override
            public void onError(Status status) {
                Log.i("error autocomplete", "An error occurred in fragment1: " + status);
            }
        });
        //TODO: make it not draw a route unless a desination already exists
        // make a new global variable to contain destination of fragment?


        autocompleteFragment2.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i("autocomplete", "Place: " + place.getName());
                Log.i("autocomplete", "latlng: " + place.getLatLng());
                destination_name = place.getName().toString();
                //Toast.makeText(PlanRoute.this, ""+placeName, Toast.LENGTH_SHORT).show();


                map.clear();

                // add current location
                LatLng currentLocation = new LatLng(latitude, longitude);
                MarkerOptions curr_options = new MarkerOptions();
                curr_options.position(currentLocation);
                curr_options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                curr_options.title("Your location");
                map.addMarker(curr_options);

                markerPoints = new ArrayList<LatLng>();
                // add current location to arraylist
                markerPoints.add(currentLocation);

                // add start point if there is one
                if (start_options != null) {
                    markerPoints.add(start_options.getPosition());
                    map.addMarker(start_options);
                }

                LatLng destination = place.getLatLng();
                markerPoints.add(destination);

                MarkerOptions options = new MarkerOptions();

                options.position(destination);
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                options.title("Your destination: " + place.getName());
                options.snippet("Accessibility Review: 4." + (new Random()).nextInt(9 - 0));

                Marker dest_m = map.addMarker(options);
                destination_options = options;
                dest_m.showInfoWindow();
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(destination,15));
                map.animateCamera(CameraUpdateFactory.zoomIn());
                map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);

                // draw path
                if(markerPoints.size() > 2){

                    System.out.println ("case 1 " + markerPoints.get(1));
                    LatLng origin = markerPoints.get(1);
                    LatLng dest = markerPoints.get(2);

                    String url = getDirectionsUrl(origin, dest);

                    DownloadTask downloadTask = new DownloadTask();

                    downloadTask.execute(url);
                    mBottomSheetBehavior.setPeekHeight(400);
                    hasDetails = true;
                } else if (markerPoints.size() == 2) {
                    LatLng origin = markerPoints.get(0);
                    LatLng dest = markerPoints.get(1);

                    String url = getDirectionsUrl(origin, dest);

                    DownloadTask downloadTask = new DownloadTask();

                    downloadTask.execute(url);
                    hasDetails = true;
                } else {
                    System.out.println ("case 3 " + markerPoints.size());
                }

            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("error autocomplete", "An error occurred: " + status);
            }
        });

        View bottomSheet = findViewById( R.id.bottom_sheet );
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setPeekHeight(200);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

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




    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;


        // Add a marker to current location and move the camera
        LatLng currentLocation = new LatLng(latitude, longitude);
        MarkerOptions curr_options = new MarkerOptions();
        curr_options.position(currentLocation);
        curr_options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        curr_options.title("Your location");

        map.addMarker(curr_options);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,15));
        // Zoom in, animating the camera.
        map.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);



        // Initializing array List
        markerPoints = new ArrayList<LatLng>();

        // Getting reference to SupportMapFragment of the activity_main
        //SupportMapFragment fm = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);

        // Getting Map for the SupportMapFragment
        //ap = fm.getMap();

        //map.setMyLocationEnabled(true);
        markerPoints.add(currentLocation);

        if (!destination_lat.equals("0") && !destination_lng.equals("0")) {


            LatLng destination = new LatLng(Double.parseDouble(destination_lat), Double.parseDouble(destination_lng));
            markerPoints.add(destination);

            MarkerOptions options = new MarkerOptions();

            options.position(destination);
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            options.title("Your destination: " + destination_name);
            options.snippet("Accessibility Review: 4." + (new Random()).nextInt(9 - 0));

            Marker dest_m = map.addMarker(options);
            destination_options = options;
            dest_m.showInfoWindow();
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(destination,15));
            map.animateCamera(CameraUpdateFactory.zoomIn());
            map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);


            if(markerPoints.size() >= 2){
                LatLng origin = markerPoints.get(0);
                LatLng dest = markerPoints.get(1);

                String url = getDirectionsUrl(origin, dest);

                DownloadTask downloadTask = new DownloadTask();

                downloadTask.execute(url);
                mBottomSheetBehavior.setPeekHeight(400);
                hasDetails = true;
            }
        } else {

            // no destination lat/lng given, allows map to be clickable
            // disabled at the moment

            /*
            map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng point) {
                    if(markerPoints.size()>1){
                        markerPoints.clear();
                        map.clear();
                    }
                    markerPoints.add(point);
                    MarkerOptions options = new MarkerOptions();
                    options.position(point);
                    if(markerPoints.size()==1){
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        options.title("Start point");
                    }else if(markerPoints.size()==2){
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        options.title("Destination point");
                    }
                    map.addMarker(options);
                    if(markerPoints.size() >= 2){
                        LatLng origin = markerPoints.get(0);
                        LatLng dest = markerPoints.get(1);
                        String url = getDirectionsUrl(origin, dest);
                        DownloadTask downloadTask = new DownloadTask();
                        downloadTask.execute(url);
                        mBottomSheetBehavior.setPeekHeight(400);
                        hasDetails = true;
                    }
                }
            });
            */
        }


    }

    public void moreInfo(View view) {

        if (!hasDetails) {
            return;
        }

        Intent intent = new Intent(this, RouteDetails.class);

        intent.putExtra("arrival" , route_info.get(0));
        intent.putExtra("departure" , route_info.get(1));
        intent.putExtra("agency_name" , route_info.get(2));
        intent.putExtra("agency_phone" , route_info.get(3));
        intent.putExtra("agency_url" , route_info.get(4));
        intent.putExtra("route_name_long" , route_info.get(5));
        intent.putExtra("route_name" , route_info.get(6));
        intent.putExtra("transit_type" , route_info.get(7));
        intent.putExtra("arrival_stop" , route_info.get(8));
        intent.putExtra("departure_stop" , route_info.get(9));
        intent.putExtra("rating" , route_info.get(10));

        startActivity(intent);

    }

    private void displayRecommendations(ArrayList<String> details) {
        route_info = details;
        route_info.add(Integer.toString((new Random()).nextInt(9 - 0)));

        TextView route_name = findViewById(R.id.route1);
        TextView route_start = findViewById(R.id.start1);
        TextView route_end = findViewById(R.id.end1);
        ImageView icon = findViewById(R.id.imageView1);

        if (details.get(0).length() == 0) {
            route_name.setText("No routes found.");
            route_start.setText("");
            route_end.setText("");
            //route_start.setVisibility(View.GONE);
            //route_end.setVisibility(View.GONE);
            icon.setVisibility(View.INVISIBLE);
        } else {
            //route_start.setVisibility(View.VISIBLE);
            //route_end.setVisibility(View.VISIBLE);
            icon.setVisibility(View.VISIBLE);
            icon.setImageResource(R.drawable.directions_bus);
            String name = details.get(7) + " Route " + details.get(6);
            route_name.setText(name);
            String start = "Departure time: " + details.get(1);
            route_start.setText(start);
            String end = "Arrival time: " + details.get(0);
            route_end.setText(end);
        }

    }

    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor+"&mode=transit";

        // Output format
        String output = "json";

        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters+"&key="+api_key;
        System.out.println("url is: " + url);


        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Except in url download", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }


    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);

                // get route information
                final ArrayList<String> details = parser.getRouteDetails();
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        displayRecommendations(details);

                    }
                });


            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            ArrayList<LatLng> points_walking = null;
            PolylineOptions lineOptions = null;
            PolylineOptions lineOptions_walking = null;
            MarkerOptions markerOptions = new MarkerOptions();
            System.out.println("result.size " + result.size());
            //System.out.println("result.get " + result.get(0));

            Boolean isTransit = true;
            Boolean foundTransit = false;

            if (result.size() == 0) {
                return;
            }

            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();
                points_walking = new ArrayList<LatLng>();
                lineOptions_walking = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    String mode = point.get("mode");

                    // find midpoint of route
                    if (j == path.size() / 2) {
                        MarkerOptions options = new MarkerOptions();

                        options.position(position);
                        Bitmap bus_marker = ((BitmapDrawable) getResources().getDrawable(R.drawable.bus_marker).getCurrent()).getBitmap();
                        bus_marker=Bitmap.createScaledBitmap(bus_marker, 175,175, false);
                        options.icon(BitmapDescriptorFactory.fromBitmap(bus_marker));
                        if (mode.equals("WALKING")) {
                            options.title("Walking route");
                            options.snippet("Accessibility Rating: N/A");
                        } else {
                            options.title("Bus route: " + route_info.get(6));
                            options.snippet("Accessibility Rating: 3." + route_info.get(10));
                        }


                        Marker dest_m = map.addMarker(options);
                    }

                    if (mode.equals("WALKING")) {
                        if(isTransit) {
                            System.out.println("draw transit: " + points);
                            isTransit = false;
                            lineOptions.addAll(points);
                            lineOptions.width(10);
                            lineOptions.color(getResources().getColor(R.color.colorPrimary));

                            map.addPolyline(lineOptions);
                            points = new ArrayList<LatLng>();
                            lineOptions = new PolylineOptions();

                        }
                        points_walking.add(position);
                    } else {
                        if(!isTransit) {
                            System.out.println("draw walking: " + points_walking);
                            isTransit = true;
                            foundTransit = true;
                            lineOptions_walking.addAll(points_walking);
                            lineOptions_walking.width(10);
                            lineOptions_walking.color(getResources().getColor(R.color.colorPrimaryDark));

                            map.addPolyline(lineOptions_walking);
                            points_walking = new ArrayList<LatLng>();
                            lineOptions_walking = new PolylineOptions();

                        }
                        points.add(position);
                    }


                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(getResources().getColor(R.color.colorPrimary));

                lineOptions_walking.addAll(points_walking);
                lineOptions_walking.width(10);
                lineOptions_walking.color(getResources().getColor(R.color.colorPrimaryDark));

            }

            // Drawing polyline in the Google Map for the i-th route
            map.addPolyline(lineOptions);
            map.addPolyline(lineOptions_walking);

            if (!foundTransit) {
                TextView route_name = findViewById(R.id.route1);
                route_name.setText("No public transits found, showing pedestrian route");

                ImageView icon = findViewById(R.id.imageView1);
                icon.setImageResource(R.drawable.pedestrian);
                icon.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}