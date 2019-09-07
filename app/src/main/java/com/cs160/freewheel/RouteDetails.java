package com.cs160.freewheel;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Random;

public class RouteDetails extends AppCompatActivity {

    private String arrival = "";
    private String departure = "";
    private String agency_name = "";
    private String agency_phone = "";
    private String agency_url = "";
    private String route_name_long = "";
    private String route_name = "";
    private String transit_type = "";
    private String arrival_stop = "";
    private String departure_stop = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_details);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }

        arrival = extras.getString("arrival", "No arrival time estimate");
        departure = extras.getString("departure", "No departure time estimate");
        route_name = extras.getString("route_name", "No route name");
        arrival_stop = extras.getString("arrival_stop", "No stop name");
        departure_stop = extras.getString("departure_stop", "No stop name");

        agency_name = extras.getString("agency_name", "No agency found");
        agency_phone = extras.getString("agency_phone", "No agency phone number found");
        agency_url = extras.getString("agency_url", "No agency website found");

        if (arrival.length() < 3) {
            arrival = "No arrival time estimate";
        }
        if (departure.length() < 3) {
            departure = "No departure time estimate";
        }
        if (arrival_stop.length() < 3) {
            arrival_stop = "No stop name";
        }
        if (departure_stop.length() < 3) {
            departure_stop = "No stop name";
        }
        if (agency_name.length() < 1) {
            agency_name = "No agency found";
        }
        if (agency_phone.length() < 3) {
            agency_phone = "No agency phone number found";
        }
        if (agency_url.length() < 3) {
            agency_url = "No agency website found";
        }

        TextView arrival_text = findViewById(R.id.textView3);
        TextView departure_text = findViewById(R.id.textView2);

        arrival_text.setText(arrival + " - " + arrival_stop);
        departure_text.setText(departure + " - " + departure_stop);

        TextView agency_text = findViewById(R.id.textView11);
        TextView phone_text = findViewById(R.id.textView12);
        TextView url_text = findViewById(R.id.textView13);

        agency_text.setText("Agency name: " + agency_name);
        phone_text.setText("Phone: " + agency_phone);
        url_text.setText("Website: " + agency_url);

        ((TextView) findViewById(R.id.rating)).setText("Accessibility Rating: 3." + extras.getString("rating", "5"));

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
}