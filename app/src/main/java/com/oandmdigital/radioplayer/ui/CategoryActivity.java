package com.oandmdigital.radioplayer.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.oandmdigital.radioplayer.event.PostCategoryEvent;

import de.greenrobot.event.EventBus;

public class CategoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // load the fragment into th default container
        if(getFragmentManager().findFragmentById(android.R.id.content) == null) {
            getFragmentManager().beginTransaction()
                    .add(android.R.id.content, new CategoryFragment())
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


    @SuppressWarnings("unused")
    public void onEventMainThread(PostCategoryEvent event) {
        // fetch the category object corresponding to the list item clicked
        Toast.makeText(this, event.getCategory().toString(), Toast.LENGTH_LONG).show();
    }

}
