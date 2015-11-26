package com.oandmdigital.radioplayer.ui;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.oandmdigital.radioplayer.R;
import com.oandmdigital.radioplayer.model.Station;

public class PlayerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Station stn = getIntent().getParcelableExtra(PlayerFragment.STATION_PARCELABLE);

        // load the fragment into th default container
        if(getFragmentManager().findFragmentById(android.R.id.content) == null) {
            getFragmentManager().beginTransaction()
                    .add(android.R.id.content, PlayerFragment.newInstance(stn))
                    .commit();
        }
    }

}
