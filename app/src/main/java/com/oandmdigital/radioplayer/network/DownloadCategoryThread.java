package com.oandmdigital.radioplayer.network;

import android.util.Log;

import com.google.gson.Gson;
import com.oandmdigital.radioplayer.event.DownloadCategoriesEvent;
import com.oandmdigital.radioplayer.model.Category;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import de.greenrobot.event.EventBus;

public class DownloadCategoryThread extends Thread{

    // http://api.dirble.com/v2/categories/primary?token=18572bdfe9011ea4a2af1c56eb
    // API Key : 18572bdfe9011ea4a2af1c56eb

    private static final String LOG_TAG = "CATEGORY_THREAD";
    private final boolean L = false;

    private static final String CATEGORY_URL =
            "http://api.dirble.com/v2/categories/primary?token=18572bdfe9011ea4a2af1c56eb";

    @Override
    public void run() {
        try {
            HttpURLConnection c = (HttpURLConnection) new URL(CATEGORY_URL).openConnection();
            if(L) Log.d(LOG_TAG, CATEGORY_URL);

            try {
                InputStream in = c.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                //Categories categories = new Gson().fromJson(reader, Categories.class);

                // parse the json data as an array of Category objects, convert this to an array list
                Category[] data = new Gson().fromJson(reader, Category[].class);
                ArrayList<Category> categories = new ArrayList<>(Arrays.asList(data));
                reader.close();
                EventBus.getDefault().post(new DownloadCategoriesEvent(categories));
                if(L) Log.d(LOG_TAG, "Posted category list to the event bus");

            } catch (IOException e) {
                Log.e(getClass().getSimpleName(), "Exception parsing JSON", e);
            } finally {
                c.disconnect();
            }
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Exception parsing JSON", e);
        }
    }

}
