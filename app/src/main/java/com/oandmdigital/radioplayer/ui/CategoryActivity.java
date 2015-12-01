package com.oandmdigital.radioplayer.ui;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.oandmdigital.radioplayer.R;
import com.oandmdigital.radioplayer.event.StationOnClickEvent;
import com.oandmdigital.radioplayer.model.Category;
import com.oandmdigital.radioplayer.util.ScreenUtility;

import de.greenrobot.event.EventBus;

public class CategoryActivity extends AppCompatActivity implements
                CategoryFragment.OnCategoryItemSelectedListener{

    /**
        NOTES:

        added 'SingleTop' launch mode - stops the activity being destroyed
        and recreated with the fragment when navigating back to it using
        the 'Up arrow' from the child activity, eg StationActivity.

        Using the support.v4.app.Fragment version of the Fragment class
        since onAttach(context) is not called in the SDK's Fragment class.

     */


    // private CategoryFragment categories;
    // private StationFragment stations;
    private boolean dualPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // instantiate the category fragment if it does not already exist
        //categories = (CategoryFragment) getSupportFragmentManager().findFragmentById(R.id.category_list);
        if(getSupportFragmentManager().findFragmentById(R.id.category_list) == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.category_list, CategoryFragment.newInstance())
                    .commit();
        }

        // is this a phone or tablet?
        if(findViewById(R.id.station_list) != null) {
            dualPane = true; // tablet
        }

        // add an empty fragment prompting the user to click on a category
        // item on tablet devices when first launched
        if(savedInstanceState == null) {

            // stations = (StationFragment) getSupportFragmentManager().findFragmentById(R.id.station_list);
            if(findViewById(R.id.station_list) != null) {
                Log.i("DEBUG", "instantiating empty station fragment");
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.station_list, EmptyStationFragment.newInstance())
                        .commit();
            }
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


    // deal with the station fragments onListItem click event in dual pane
    @SuppressWarnings("unused")
    public void onEventMainThread(StationOnClickEvent event) {
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra(PlayerFragment.STATION_PARCELABLE, event.getStation());
        startActivity(intent);
    }



    @Override
    public void OnCategoryItemClickSelected(Category category) {

        if(dualPane) {
            // on a tablet replace the current station fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.station_list, StationFragment.newInstance(category))
                    .commit();
        } else {
            // on a phone fetch the category object corresponding to the list item clicked
            // bundle it into an intent and launch the Station activity
            Intent intent = new Intent(this, StationActivity.class);
            intent.putExtra(StationFragment.CATEGORY_PARCELABLE, category);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_size) {

            // determine the screen dimensions in dp
            ScreenUtility utility = new ScreenUtility(this);
            String output  = String.format("Width %.0f, height %.0f",
                    utility.getWidth(), utility.getHeight());

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(output)
                    .setTitle("Screen dimensions")
                    .create().show();

            return true;

        }
        return super.onOptionsItemSelected(item);
    }
}
