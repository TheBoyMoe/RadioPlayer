package com.oandmdigital.radioplayer.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import com.oandmdigital.radioplayer.event.IsPlayingEvent;
import com.oandmdigital.radioplayer.event.LoadingCompleteEvent;
import com.oandmdigital.radioplayer.model.Station;
import com.oandmdigital.radioplayer.ui.PlayerFragment;

import de.greenrobot.event.EventBus;

public class RadioService extends Service {

    private static final String LOG_TAG = "RadioService";
    private Station stn;
    private boolean isPlaying;
    private boolean loadingComplete;

    @Override
    public void onCreate() {
        super.onCreate();

        initMusicPlayer();
    }

    private void initMusicPlayer() {
        // TODO prepare and initialize the media player



    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // retrieve the stn obj from the intent
        stn = intent.getParcelableExtra(PlayerFragment.STATION_PARCELABLE);
        Log.i(LOG_TAG, stn.toString());
        play(); // start mediaplayer
        return START_NOT_STICKY;
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void play() {
        if(!isPlaying) {
            // TODO start the media player
            Log.i(LOG_TAG, "Buffering audio");

            // TODO post loading complete event
            // simulate buffering radio before posting complete loading event
            new BufferThread().start();
            // Log.i(LOG_TAG, "Buffering complete, player started, posting event");
            // EventBus.getDefault().post(new LoadingCompleteEvent(true));

            // post isPlaying event
            isPlaying = true;
            EventBus.getDefault().post(new IsPlayingEvent(true));
        }
    }


    private void stop() {
        if(isPlaying) {
            // TODO stop the media player
            Log.i(LOG_TAG, "Player stopped, posting event");


            //  post isPlaying event
            isPlaying = false;
            EventBus.getDefault().post(new IsPlayingEvent(false));
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stop();
    }


    private class BufferThread extends Thread {

        @Override
        public void run() {
            if(!isInterrupted()) {
                // simulate buffering radio before posting event complete
                // sleep for 2secs before posting event
                SystemClock.sleep(4000);
                Log.i(LOG_TAG, "Buffering complete, player started, posting event");
                EventBus.getDefault().post(new LoadingCompleteEvent(true));
            }
        }
    }


}
