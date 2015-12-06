package com.oandmdigital.radioplayer.playback;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.oandmdigital.radioplayer.model.Station;
import com.oandmdigital.radioplayer.ui.PlayerFragment;

import java.io.IOException;
import java.net.BindException;

/**
 * References:
 * [1] https://www.youtube.com/watch?v=XQwe30cZffg BigAndroid BBQ 2015 - media player the right way by Ian Lake
 * [2] http://www.code-labs.io/codelabs/android-music-player/index.html#0
 * [3] MarshmallowFM google docs
 */
public class PlaybackService extends Service implements
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener{


    public static final String STATION_URI = "Station_uri";
    public static final String STATION_NAME = "Station_name";
    public static final String ACTION_PLAY = "play";
    public static final String ACTION_STOP = "stop";

    private static final String LOG_TAG = "PlaybackService";
    private final boolean L = true;

    private AudioManager audioManager;
    private MediaSessionCompat mediaSession;
    private PlaybackStateCompat playbackState;
    private MediaControllerCompat mediaController;
    private Binder binder = new ServiceBinder();
    private MediaPlayer mediaPlayer;


    public PlaybackService() {}


    public class ServiceBinder extends Binder {
        public PlaybackService getService() {
            return PlaybackService.this;
        }
    }


    public MediaSessionCompat.Token getMediaSessionToken() {
        return mediaSession.getSessionToken();
    }


    final MediaSessionCompat.Callback mediaSessionCallback = new MediaSessionCompat.Callback() {

        @Override
        public void onPlayFromSearch(String query, Bundle extras) {
            if(L) Log.i(LOG_TAG, "Calling onPlayFromSearch()");
            Uri uri = extras.getParcelable(STATION_URI);
            onPlayFromUri(uri, extras);
        }


        @Override
        public void onPlayFromUri(Uri uri, Bundle extras) {

            if(L) Log.i(LOG_TAG, "Calling onPlayFromUri()");
            String name = extras.getString(STATION_NAME);

            try {
                switch (playbackState.getState()) {
                    case PlaybackStateCompat.STATE_NONE:
                    case PlaybackStateCompat.STATE_STOPPED:
                        // buffer the audio
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(PlaybackService.this, uri);
                        mediaPlayer.prepareAsync();
                        if(L) Log.i(LOG_TAG, "Buffering audio");
                        // set the playback state & set the audio's metadata
                        playbackState = new PlaybackStateCompat.Builder()
                                .setState(PlaybackStateCompat.STATE_CONNECTING, 0, 1.0f)
                                .build();
                        mediaSession.setPlaybackState(playbackState);
                        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, name)
                                .build());
                        break;
                    case PlaybackStateCompat.STATE_PLAYING:
                        // stop
                        mediaPlayer.stop();
                        playbackState = new PlaybackStateCompat.Builder()
                                .setState(PlaybackStateCompat.STATE_STOPPED, 0, 1.0f)
                                .build();
                        mediaSession.setPlaybackState(playbackState);
                        updateNotification();
                        break;
                }

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error thrown during playback");
            }
        }


//        @Override
//        public void onPlay() {
//            if (playbackState.getState() == PlaybackStateCompat.STATE_STOPPED) {
//                if(L) Log.i(LOG_TAG, "Calling onPlay()");
//                mediaPlayer.start();
//                playbackState = new PlaybackStateCompat.Builder()
//                        .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f)
//                        .build();
//                mediaSession.setPlaybackState(playbackState);
//                updateNotification();
//            }
//        }


        @Override
        public void onStop() {
            if(playbackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
                if(L) Log.i(LOG_TAG, "Calling onStop()");
                mediaPlayer.stop();
                playbackState = new PlaybackStateCompat.Builder()
                        .setState(PlaybackStateCompat.STATE_STOPPED, 0, 1.0f)
                        .build();
                mediaSession.setPlaybackState(playbackState);
                updateNotification();
            }
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();

        // set the initial playback state
        playbackState = new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_NONE, 0, 1.0f)
                .build();

        // instantiate the media session
        mediaSession = new MediaSessionCompat(this, LOG_TAG);
        mediaSession.setCallback(mediaSessionCallback);
        mediaSession.setActive(true);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setPlaybackState(playbackState);

        // TODO needs to be tied in to requesting audio focus
        //get instance to AudioManager
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // instantiate and configure the media player
        initMusicPlayer();

        // instantiate the media controller
        try {
            mediaController = new MediaControllerCompat(this, mediaSession.getSessionToken());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        if (L) Log.i(LOG_TAG, "Audio playback came to an end");
        playbackState = new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_NONE, 0, 1.0f)
                .build();
        mediaSession.setPlaybackState(playbackState);
        mediaPlayer.reset();

        // TODO post event to bus
    }


    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(LOG_TAG, "Media player encountered an error");
        playbackState = new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_NONE, 0, 1.0f)
                .build();
        mediaSession.setPlaybackState(playbackState);
        mediaPlayer.reset();

        // TODO post event to bus
        return false;
    }


    @Override
    public void onPrepared(MediaPlayer mp) {
        if(L) Log.i(LOG_TAG, "Buffering complete, playback starting");
        mediaPlayer.start();
        playbackState = new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f)
                .build();
        mediaSession.setPlaybackState(playbackState);
        updateNotification();

        // TODO post event to bus to hide progress bar
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(L) Log.i(LOG_TAG, "Starting service");
        if (intent != null && intent.getAction() != null) {

            switch (intent.getAction()) {
                case ACTION_PLAY:
                    if(L) Log.i(LOG_TAG, "Calling play");
                    mediaController.getTransportControls().play();
                    break;
                case ACTION_STOP:
                    if(L) Log.i(LOG_TAG, "Calling stop");
                    mediaController.getTransportControls().stop();
                    break;
            }
        }

        // receives and handles all media button key events and forwards
        // to the media session which deals with them in its callback methods
        MediaButtonReceiver.handleIntent(mediaSession, intent);

        return super.onStartCommand(intent, flags, startId);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if(L) Log.i(LOG_TAG, "Stopping playback service, releasing resources");
        //playbackManager.stop();
        mediaPlayer.release();
        mediaSession.release();
    }


    private void initMusicPlayer() {

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setLooping(false);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        if(L) Log.i(LOG_TAG, "Initializing media player");
        // tell the system it needs to stay on
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
    }



    private void updateNotification() {
        // TODO to be implemented
        Log.i(LOG_TAG, "Notification updated, if one existed");
    }



}
