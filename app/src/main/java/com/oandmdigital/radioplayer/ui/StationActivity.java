package com.oandmdigital.radioplayer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.oandmdigital.radioplayer.event.StationOnClickEvent;
import com.oandmdigital.radioplayer.model.Category;

import de.greenrobot.event.EventBus;

public class StationActivity extends AppCompatActivity {

    // added 'SingleTop' launch mode - stops the activity being destroyed
    // and recreated with the fragment when navigating back to it using
    // the 'Up arrow' from the child activity, eg PlayerActivity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Category category = getIntent().getParcelableExtra(StationFragment.CATEGORY_PARCELABLE);

        // load the fragment into the default container
        if(getFragmentManager().findFragmentById(android.R.id.content) == null) {
            getFragmentManager().beginTransaction()
                    .add(android.R.id.content, StationFragment.newInstance(category))
                    .addToBackStack("Category")
                    .commit();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }


    // deal with the fragments onListItem click event by launching the player
    @SuppressWarnings("unused")
    public void onEventMainThread(StationOnClickEvent event) {
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra(PlayerFragment.STATION_PARCELABLE, event.getStation());
        startActivity(intent);
    }

}
