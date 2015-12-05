package com.oandmdigital.radioplayer.ui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.oandmdigital.radioplayer.R;
import com.oandmdigital.radioplayer.event.IsPlayingEvent;
import com.oandmdigital.radioplayer.event.LoadingCompleteEvent;
import com.oandmdigital.radioplayer.event.PlaybackServiceEvent;
import com.oandmdigital.radioplayer.model.Station;
import com.oandmdigital.radioplayer.model.Stream;
import com.oandmdigital.radioplayer.playback.PlaybackService;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

public class PlayerFragment extends Fragment implements
        View.OnClickListener,
        ServiceConnection{

    public static final String STATION_PARCELABLE = "station";
    private static final String IS_PLAYING = "isPlaying";
    private static final String LOADING_COMPLETE = "loadingComplete";
    private static final String LOG_TAG = PlayerFragment.class.getSimpleName();
    private final boolean L = true;

    private Station stn;
    private ImageButton playStopBtn;
    private ProgressBar progressBar;
    private boolean isPlaying = false;
    private boolean loadingComplete = true;
    private TextView stnName;
    private MediaControllerCompat mediaController;


    public static PlayerFragment newInstance(Station station) {

        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
        args.putParcelable(STATION_PARCELABLE, station);
        fragment.setArguments(args);

        return fragment;
    }


    private MediaControllerCompat.Callback mediaControllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {

            switch (state.getState()) {
                case PlaybackStateCompat.STATE_NONE:
                case PlaybackStateCompat.STATE_STOPPED:
                    playStopBtn.setImageResource(R.drawable.action_play);
                    break;
                case PlaybackStateCompat.STATE_PLAYING:
                    playStopBtn.setImageResource(R.drawable.action_stop);
                    break;
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            stnName.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
        }
    };



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        stn = getArguments().getParcelable(STATION_PARCELABLE);

        // create the intent used to bind to the service
        Intent intent = new Intent(getActivity(), PlaybackService.class);

        //playbackIntent.putExtra(STATION_PARCELABLE, stn);
        getActivity().getApplicationContext().bindService(intent, this, 0);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.player, container, false);

        TextView stnName = (TextView) view.findViewById(R.id.station_name);
        playStopBtn = (ImageButton) view.findViewById(R.id.play_stop_button);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);

        // default setup on start
        stnName.setText(stn.getName());
        playStopBtn.setImageResource(R.drawable.action_play);
        playStopBtn.setOnClickListener(this);

        // TODO show/hide progress bar on buffering/completion
        progressBar.setVisibility(View.INVISIBLE);


        ///////////////////////////////////////////////////////

//        if(savedInstanceState != null) {
//            isPlaying = savedInstanceState.getBoolean(IS_PLAYING);
//            loadingComplete = savedInstanceState.getBoolean(LOADING_COMPLETE);
//        }
//
//        // set the appropriate playStopBtn & progress bar
//        if(isPlaying || !loadingComplete)
//            playStopBtn.setImageResource(R.drawable.action_stop);
//        else
//            playStopBtn.setImageResource(R.drawable.action_play);
//
//        if(!loadingComplete)
//            progressBar.setVisibility(View.VISIBLE); // playback is being buffered
//        else
//            progressBar.setVisibility(View.INVISIBLE);

        ///////////////////////////////////////////////////////////


        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(getActivity(), PlaybackService.class);
        getActivity().startService(intent);
    }


    @Override
    public void onClick(View v) {

//        playbackIntent = new Intent(getActivity(), PlaybackService.class);
//        playbackIntent.putExtra(STATION_PARCELABLE, stn);

        Log.d(LOG_TAG, "media controller " + mediaController);
        if(mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_NONE ||
                mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_STOPPED) {

            Uri uri = Uri.parse(getStream(stn));
            Bundle bundle = new Bundle();
            bundle.putString(PlaybackService.STATION_NAME, stn.getName());

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mediaController.getTransportControls().playFromUri(uri, bundle);
            } else {
                bundle.putParcelable(PlaybackService.STATION_URI, uri);
                mediaController.getTransportControls().playFromSearch("", bundle);
            }

        } else {
            mediaController.getTransportControls().stop();
        }



        //////////////////////////////////////////////////////////////

//        if(!loadingComplete){
//            // stop playback service if still buffering
//            playStopBtn.setImageResource(R.drawable.action_play);
//            getActivity().stopService(playbackIntent);
//            loadingComplete = true; // default
//
//            // hide the progressbar if the user hits stop before the audio is buffered
//            if(progressBar.getVisibility() == View.VISIBLE)
//                progressBar.setVisibility(View.INVISIBLE);
//
//        }
//        else if(isPlaying) {
//            playStopBtn.setImageResource(R.drawable.action_play);
//            getActivity().stopService(playbackIntent);
//        }
//        else {
//            // neither buffering or playing
//            playStopBtn.setImageResource(R.drawable.action_stop);
//            progressBar.setVisibility(View.VISIBLE);
//            Toast.makeText(getActivity(), "Buffering audio", Toast.LENGTH_SHORT).show();
//            loadingComplete = false;
//            // start audio playback
//            getActivity().startService(playbackIntent);
//
//        }

        /////////////////////////////////////////////////////////////
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

        if(L) Log.d(LOG_TAG, "onServiceConnected() called");
        if (service instanceof PlaybackService.ServiceBinder) {
            try {
                mediaController = new MediaControllerCompat(getActivity(),
                        ((PlaybackService.ServiceBinder) service).getService().getMediaSessionToken());

            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mediaController.registerCallback(mediaControllerCallback);
            if(L) Log.i(LOG_TAG, "Connected to Playback Service");
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        if(L) Log.i(LOG_TAG, "Disconnected from Playback Service");
    }


    // retrieve the stream url from the station object
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


    //////////////////////////////////////////////////////////////////////////////

//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putBoolean(IS_PLAYING, isPlaying);
//        outState.putBoolean(LOADING_COMPLETE, loadingComplete);
//    }
//
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        EventBus.getDefault().register(this);
//    }
//
//
//    @Override
//    public void onPause() {
//        EventBus.getDefault().unregister(this);
//        super.onPause();
//    }
//
//
//    // handle the isPlaying event
//    @SuppressWarnings("unused")
//    public void onEventMainThread(IsPlayingEvent event) {
//        //noinspection RedundantIfStatement
//        if(event.isPlaying())
//            isPlaying = true;
//        else
//            isPlaying = false;
//    }
//
//
//    // handle the loadComplete event
//    @SuppressWarnings("unused")
//    public void onEventMainThread(LoadingCompleteEvent event) {
//        // hide the progress bar once buffering is complete and playback can start
//        if(event.isLoadingComplete()) {
//            loadingComplete = true;
//            progressBar.setVisibility(View.INVISIBLE);
//        }
//    }
//
//
//    // handle PlaybackManager messages
//    @SuppressWarnings("unused")
//    public void onEventMainThread(PlaybackServiceEvent event) {
//        switch (event.getMessage()) {
//
//            case PlaybackServiceEvent.ERROR_BUFFERING_AUDIO:
//                Toast.makeText(getActivity(), "Error buffering audio", Toast.LENGTH_SHORT).show();
//                break;
//
//            case PlaybackServiceEvent.ERROR_NO_STREAM_FOUND:
//                Toast.makeText(getActivity(), "No stream found", Toast.LENGTH_SHORT).show();
//                break;
//
//            case PlaybackServiceEvent.ERROR_PLAYING_MEDIA:
//                Toast.makeText(getActivity(), "Error playing media", Toast.LENGTH_SHORT).show();
//                break;
//
//            case PlaybackServiceEvent.EVENT_STREAM_FINISHED:
//                Toast.makeText(getActivity(), "Stream finished", Toast.LENGTH_SHORT).show();
//                break;
//
//            case PlaybackServiceEvent.EVENT_GAINED_FOCUS:
//                // hasFocus = true;
//                break;
//
//            case PlaybackServiceEvent.EVENT_CANNOT_GAIN_FOCUS:
//                Toast.makeText(getActivity(), "Cannot gain exclusive use of device audio", Toast.LENGTH_SHORT).show();
//                // hasFocus = false;
//                break;
//
//            case PlaybackServiceEvent.EVENT_LOST_FOCUS:
//                // we've lost focus so reset the ui
//                // Toast.makeText(getActivity(), "Lost focus, stopping playback", Toast.LENGTH_SHORT).show();
//                // hasFocus = false;
//                playStopBtn.setImageResource(R.drawable.action_play);
//                break;
//
//            case PlaybackServiceEvent.EVENT_BECOMING_NOISY:
//                // stop playback service when Becoming_Noisy intent is received
//                Toast.makeText(getActivity(), "Audio output changed, stopping playback", Toast.LENGTH_SHORT).show();
//                getActivity().stopService(playbackIntent);
//                break;
//
//        }
//    }





}
