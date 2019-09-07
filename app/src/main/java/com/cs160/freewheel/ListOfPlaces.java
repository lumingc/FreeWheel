package com.cs160.freewheel;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class ListOfPlaces extends AppCompatActivity {

    Button restaurant_sample;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_places);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        restaurant_sample = findViewById(R.id.restaurant_sample);
//        restaurant_sample.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                Intent intent = new Intent(getBaseContext(), PlaceDetails.class);
//                startActivity(intent);
//            }});
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
