package com.oandmdigital.radioplayer.playback;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.PowerManager;
import android.util.Log;

import com.oandmdigital.radioplayer.event.IsPlayingEvent;
import com.oandmdigital.radioplayer.event.LoadingCompleteEvent;
import com.oandmdigital.radioplayer.event.PlaybackServiceEvent;
import com.oandmdigital.radioplayer.model.Station;
import com.oandmdigital.radioplayer.model.Stream;

import java.io.IOException;
import java.util.ArrayList;

import de.greenrobot.event.EventBus;

@SuppressWarnings("ALL")
public class PlaybackManager implements
        AudioManager.OnAudioFocusChangeListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener {

    private static final String LOG_TAG = "PlaybackManager";
    private final boolean L = true;
    private boolean isPlaying;
    // private boolean playOnFocusGain;
    // private boolean isBuffering;
    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private Context context;


    public PlaybackManager(Context context) {
        this.context = context;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        initMusicPlayer();
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
        mediaPlayer.setWakeMode(context.getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
    }


    public void play(Station stn) {
        if(!isPlaying && mediaPlayer != null) {

            // TODO ?
            // stop();

            String url = getStream(stn);
            if(url != null) {
                try {
                    // download and buffer the stream on a background thread
                    mediaPlayer.setDataSource(url);
                    mediaPlayer.prepareAsync();
                    //isBuffering = true;
                    if(L) Log.i(LOG_TAG, "Buffering audio");

                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error buffering audio");
                    EventBus.getDefault().post(new PlaybackServiceEvent(PlaybackServiceEvent.ERROR_BUFFERING_AUDIO));
                }
            } else {
                if(L) Log.i(LOG_TAG, "No stream found");
                EventBus.getDefault().post(new PlaybackServiceEvent(PlaybackServiceEvent.ERROR_NO_STREAM_FOUND));
            }

        }
    }


    public void stop() {
        if(mediaPlayer != null) {

            // give up audio focus
            audioManager.abandonAudioFocus(this);
            EventBus.getDefault().post(new PlaybackServiceEvent(PlaybackServiceEvent.EVENT_LOST_FOCUS));

            // release the media mediaPlayer's resources
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;

            //  post isPlaying event
            isPlaying = false;
            if(L) Log.i(LOG_TAG, "Player stopped, abandoned focus");
            EventBus.getDefault().post(new IsPlayingEvent(false));
        }

    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        stop();
        if(L) Log.i(LOG_TAG, "Stream has finished");
        EventBus.getDefault().post(new PlaybackServiceEvent(PlaybackServiceEvent.EVENT_STREAM_FINISHED));
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        stop();
        Log.e(LOG_TAG, "Media player has thrown an error");
        EventBus.getDefault().post(new PlaybackServiceEvent(PlaybackServiceEvent.ERROR_PLAYING_MEDIA));
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

        // buffering complete
        //isBuffering = false;
        if(L) Log.i(LOG_TAG, "Buffering complete, mediaPlayer started");
        EventBus.getDefault().post(new LoadingCompleteEvent(true));


        // every things good to go, get audio focus to start playing
        int gotFocus = audioManager.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
        );

        // we've got focus so can start playing
        if(gotFocus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            if(L) Log.i(LOG_TAG, "Gained focus, starting playback");
            EventBus.getDefault().post(PlaybackServiceEvent.EVENT_GAINED_FOCUS);
            mediaPlayer.start();
            // playOnFocusGain = false;
            isPlaying = true;
            EventBus.getDefault().post(new IsPlayingEvent(true));
        } else {
            if(L) Log.i(LOG_TAG, "Can not gain focus");
            EventBus.getDefault().post(new PlaybackServiceEvent(PlaybackServiceEvent.EVENT_CANNOT_GAIN_FOCUS));
            // playOnFocusGain = true;
        }
    }


    // called by AudioManager when audio focus changes
    @Override
    public void onAudioFocusChange(int focusChange) {

        // if we've lost focus, stop playback

        //boolean gotFocus = false;
        //boolean canDuck = false;

        // if we've gained focus, play
//        if(focusChange == AudioManager.AUDIOFOCUS_GAIN) {
//            gotFocus = true;
//        }
        // otherwise try and duck, if not simply stop
        if(focusChange == AudioManager.AUDIOFOCUS_LOSS ||
                focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
                focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK){

            //canDuck = focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;
            // when losing focus, the UI needs updating
            if(L) Log.i(LOG_TAG, "Lost focus, stopping playback");
            EventBus.getDefault().post(new PlaybackServiceEvent(PlaybackServiceEvent.EVENT_LOST_FOCUS));
            stop();
        }

//        if(gotFocus || canDuck) {
//            // if we can duck, turn the volume down
//            if(mediaPlayer != null) {
//                if(playOnFocusGain) {
//                    playOnFocusGain = false;
//                    mediaPlayer.start();
//                    isPlaying = true;
//                    EventBus.getDefault().post(new IsPlayingEvent(true));
//
//                 }
//                float volume = canDuck ? 0.2f : 1.0F;
//                mediaPlayer.setVolume(volume, volume);
//            }
//
//        }
//        else {
//            // lost focus and can't duck, stop playback
//            stop();
//            Log.i(LOG_TAG, "Losing focus, stopping playback");
//        }

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
