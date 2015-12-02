package com.oandmdigital.radioplayer.playback;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.oandmdigital.radioplayer.model.Station;
import com.oandmdigital.radioplayer.ui.PlayerFragment;


public class PlaybackService extends Service{


    private static final String LOG_TAG = "PlaybackService";
    private PlaybackManager playbackManager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Station station = intent.getParcelableExtra(PlayerFragment.STATION_PARCELABLE);
        playbackManager = new PlaybackManager(this);
        playbackManager.play(station);
        return START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        playbackManager.stop();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
