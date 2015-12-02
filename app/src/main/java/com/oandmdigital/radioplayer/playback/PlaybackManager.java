package com.oandmdigital.radioplayer.playback;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.PowerManager;
import android.util.Log;

import com.oandmdigital.radioplayer.event.IsPlayingEvent;
import com.oandmdigital.radioplayer.event.LoadingCompleteEvent;
import com.oandmdigital.radioplayer.event.RadioServiceEvent;
import com.oandmdigital.radioplayer.model.Station;
import com.oandmdigital.radioplayer.model.Stream;

import java.io.IOException;
import java.util.ArrayList;

import de.greenrobot.event.EventBus;

public class PlaybackManager implements
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener {

    private static final String LOG_TAG = "PlaybackManager";
    //private Station stn;
    private boolean isPlaying;
    private MediaPlayer mediaPlayer;
    private Context context;

//    @Override
//    public void onCreate() {
//        super.onCreate();
//
//        // initialize and prepare the media mediaPlayer
//        initMusicPlayer();
//    }

    public PlaybackManager(Context context) {
        this.context = context;
        initMusicPlayer();
    }


    private void initMusicPlayer() {

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setLooping(false);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);

        // tell the system it needs to stay on
        mediaPlayer.setWakeMode(context.getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
    }


//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//
//        // retrieve the stn obj from the intent
//        stn = intent.getParcelableExtra(PlayerFragment.STATION_PARCELABLE);
//        Log.i(LOG_TAG, stn.toString());
//        play(); // start mediaplayer
//        return START_NOT_STICKY;
//    }



//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }


    public void play(Station stn) {
        if(!isPlaying && mediaPlayer != null) {

            // ensure there is only one media player
            stop();

            String url = getStream(stn);
            if(url != null) {
                try {
                    // download and buffer the stream on a background thread
                    mediaPlayer.setDataSource(url);
                    mediaPlayer.prepareAsync();
                    Log.i(LOG_TAG, "Buffering audio");

                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error buffering audio");
                    EventBus.getDefault().post(new RadioServiceEvent(RadioServiceEvent.ERROR_BUFFERING_AUDIO));
                }
            } else {
                Log.i(LOG_TAG, "No stream found");
                EventBus.getDefault().post(new RadioServiceEvent(RadioServiceEvent.ERROR_NO_STREAM_FOUND));
            }
        }
    }


    public void stop() {
        if(isPlaying && mediaPlayer != null) {

            // release the media mediaPlayer's resources
            mediaPlayer.release();
            mediaPlayer = null;

            //  post isPlaying event
            isPlaying = false;
            Log.i(LOG_TAG, "Player stopped, posting event");
            EventBus.getDefault().post(new IsPlayingEvent(false));
        }

    }


//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        stop();
//    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        stop();
        Log.i(LOG_TAG, "Stream has finished");
        EventBus.getDefault().post(new RadioServiceEvent(RadioServiceEvent.EVENT_STREAM_FINISHED));
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        stop();
        Log.i(LOG_TAG, "Media player has thrown an error");
        EventBus.getDefault().post(new RadioServiceEvent(RadioServiceEvent.ERROR_PLAYING_MEDIA));
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

        // buffering complete
        Log.i(LOG_TAG, "Buffering complete, mediaPlayer started, posting event");
        EventBus.getDefault().post(new LoadingCompleteEvent(true));

        mediaPlayer.start();

        // post isPlaying event
        isPlaying = true;
        EventBus.getDefault().post(new IsPlayingEvent(true));
    }


    private String getStream(Station stn) {

        String url = null;
        ArrayList<Stream> streams = (ArrayList<Stream>) stn.getStreams();
        int status;
        for (Stream stream : streams) {
            status = stream.getStatus();
            if(status >= 0) {
                url = stream.getStream();
                if(url != null && !url.isEmpty())
                    break;
            }
        }
        return url;
    }


}
