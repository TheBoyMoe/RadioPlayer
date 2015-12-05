package com.oandmdigital.radioplayer.playback;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.oandmdigital.radioplayer.model.Station;
import com.oandmdigital.radioplayer.ui.PlayerFragment;

/**
 * References:
 * [1] https://www.youtube.com/watch?v=XQwe30cZffg BigAndroid BBQ 2015 - media player the right way by Ian Lake
 * [2] http://www.code-labs.io/codelabs/android-music-player/index.html#0
 */
public class PlaybackService extends Service{


    private static final String LOG_TAG = "PlaybackService";
    private final boolean L = true;
    private PlaybackManager playbackManager;
    private MediaSessionCompat mediaSession;
    private Station station;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent != null) {

            if(intent.hasExtra(PlayerFragment.STATION_PARCELABLE)) {
                if(L) Log.i(LOG_TAG, "Starting playback service");
                station = intent.getParcelableExtra(PlayerFragment.STATION_PARCELABLE);
            }

            // receives and handles all media button key events and forwards
            // to the media session which deals with them in its callback methods
            MediaButtonReceiver.handleIntent(mediaSession, intent);
        }

        //playbackManager = new PlaybackManager(this);
        //playbackManager.play(station);

        return START_NOT_STICKY;
    }


    // central point for controlling your media player
    final MediaSessionCompat.Callback sessionCallback = new MediaSessionCompat.Callback() {
        @Override
        public void onPlay() {
            // set the session as the preferred media button receiver
            Log.i(LOG_TAG, "Launching onPlay");
            mediaSession.setActive(true);
            playbackManager.play(station);

            // TODO set metadata - artist/album/image, used by notification controls - NotificationCompat.MediaStyle
        }

        @Override
        public void onStop() {
            mediaSession.setActive(false);
            stopSelf();
        }

    };


    @Override
    public void onCreate() {
        super.onCreate();

        // instantiate and configure the session object
        mediaSession = new MediaSessionCompat(this, LOG_TAG);
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        );
        mediaSession.setCallback(sessionCallback);

        // instantiate the playback manager
        playbackManager = new PlaybackManager(this, new PlaybackManager.PlaybackStateCallback() {
            @Override
            public void onPlaybackStateChange(PlaybackStateCompat state) {
                mediaSession.setPlaybackState(state);
                if(L) Log.i(LOG_TAG, "Updating playback state on session");
            }
        });

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if(L) Log.i(LOG_TAG, "Stopping playback service");
        playbackManager.stop();
        mediaSession.release();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
