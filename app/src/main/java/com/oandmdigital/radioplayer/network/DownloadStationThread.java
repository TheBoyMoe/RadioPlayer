package com.oandmdigital.radioplayer.network;

import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.oandmdigital.radioplayer.event.DownloadStationsEvent;
import com.oandmdigital.radioplayer.model.Station;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import de.greenrobot.event.EventBus;

@SuppressWarnings("FieldCanBeLocal")
public class DownloadStationThread extends Thread{

    private static final String LOG_TAG = "STATION_THREAD";
    private final boolean L = true;

    private static final String CATEGORY_URL = "http://api.dirble.com/v2/category/";
    private static final String QUERY = "/stations?";

    private int page = 1;
    private int resultsPerPage = 20;
    private String token = "18572bdfe9011ea4a2af1c56eb";
    private final String PAGE_PARAM = "page";
    private final String RESULTS_PER_PAGE_PARAM = "per_page";
    private final String TOKEN_PARAM = "token";

    // build the url so that you can pass in the category id and the page number
    // private static final String STATION_URL =
    //    "http://api.dirble.com/v2/category/5/stations?page=1&per_page=20&token=18572bdfe9011ea4a2af1c56eb";


    private String categoryId;

    public DownloadStationThread(String threadName, String categoryId) {
        super(threadName);
        this.categoryId = categoryId;
    }

    @Override
    public void run() {

        try {
            // build the query
            Uri stationUri = Uri.parse(CATEGORY_URL + categoryId + QUERY).buildUpon()
                    .appendQueryParameter(PAGE_PARAM, Integer.toString(page))
                    .appendQueryParameter(RESULTS_PER_PAGE_PARAM, Integer.toString(resultsPerPage))
                    .appendQueryParameter(TOKEN_PARAM, token)
                    .build();

            if(L) Log.d(LOG_TAG, stationUri.toString());

            HttpURLConnection c = (HttpURLConnection) new URL(stationUri.toString()).openConnection();

            try {
                InputStream in = c.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                // parse the json data as an array of Station objects,
                // convert this to an array list and post to the event bus
                Station[] data = new Gson().fromJson(reader, Station[].class);
                ArrayList<Station> stationList = new ArrayList<>(Arrays.asList(data));
                reader.close();
                EventBus.getDefault().post(new DownloadStationsEvent(stationList));
                if(L) Log.d(LOG_TAG, "Posted station list to the event bus");

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
