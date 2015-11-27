package com.oandmdigital.radioplayer.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class RadioService extends Service{

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

        // TODO retrieve the stn obj from the intent


        return START_NOT_STICKY;
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void play() {
        // TODO start the media player
    }


    private void stop() {
        // TODO stop the media player
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stop();
    }


}
