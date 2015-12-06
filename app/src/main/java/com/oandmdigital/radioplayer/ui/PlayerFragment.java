package com.oandmdigital.radioplayer.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import com.oandmdigital.radioplayer.R;
import com.oandmdigital.radioplayer.event.LoadingCompleteEvent;
import com.oandmdigital.radioplayer.model.Station;
import com.oandmdigital.radioplayer.model.Stream;
import com.oandmdigital.radioplayer.playback.PlaybackService;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

public class PlayerFragment extends Fragment implements
        View.OnClickListener,
        ServiceConnection{

    public static final String STATION_PARCELABLE = "station";
    private static final String LOG_TAG = PlayerFragment.class.getSimpleName();
    private final boolean L = true;

    private Station stn;
    private ImageButton playStopBtn;
    private ProgressBar progressBar;
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
                case PlaybackStateCompat.STATE_BUFFERING:
                    playStopBtn.setImageResource(R.drawable.action_stop);
                    break;
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            //stnName.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
        }
    };



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        stn = getArguments().getParcelable(STATION_PARCELABLE);

        // create the intent used to both start & bind to the service
        Intent intent = new Intent(getActivity(), PlaybackService.class);
        getActivity().getApplicationContext().bindService(intent, this, 0);
        getActivity().startService(intent);

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.player, container, false);

        stnName = (TextView) view.findViewById(R.id.station_name);
        playStopBtn = (ImageButton) view.findViewById(R.id.play_stop_button);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);

        // default UI on start
        stnName.setText(stn.getName());
        playStopBtn.setImageResource(R.drawable.action_play);
        playStopBtn.setOnClickListener(this);
        progressBar.setVisibility(View.INVISIBLE);

        // set the visibility of the progressbar and playstop btn icon on device rotation
        if(savedInstanceState != null) {
            int state = mediaController.getPlaybackState().getState();
            if(state == PlaybackStateCompat.STATE_BUFFERING){
                playStopBtn.setImageResource(R.drawable.action_stop);
                progressBar.setVisibility(View.VISIBLE);
            }
            if(state == PlaybackStateCompat.STATE_PLAYING)
                playStopBtn.setImageResource(R.drawable.action_stop);

        }

        return view;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().getApplicationContext().unbindService(this);
        if(L) Log.i(LOG_TAG, "Unbinding Playback Service");
    }


    @Override
    public void onClick(View v) {
        int state = mediaController.getPlaybackState().getState();
        if(state == PlaybackStateCompat.STATE_NONE
              ||  state == PlaybackStateCompat.STATE_STOPPED) {

            if(stn != null) {
                Uri uri = Uri.parse(getStream(stn));
                Bundle bundle = new Bundle();
                bundle.putString(PlaybackService.STATION_NAME, stn.getName());

                //if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //    mediaController.getTransportControls().playFromUri(uri, bundle);
                //} else {
                //    bundle.putParcelable(PlaybackService.STATION_URI, uri);
                //    mediaController.getTransportControls().playFromSearch("", bundle);
                //}

                // works on API's 16 - 23 on emulators
                bundle.putParcelable(PlaybackService.STATION_URI, uri);
                mediaController.getTransportControls().playFromSearch("", bundle);

                // show the progress bar while buffering the audio stream
                progressBar.setVisibility(View.VISIBLE);
            }

        } else {
            mediaController.getTransportControls().stop();
        }

    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

        if (service instanceof PlaybackService.ServiceBinder) {
            try {
                mediaController = new MediaControllerCompat(getActivity(),
                        ((PlaybackService.ServiceBinder) service).getService().getMediaSessionToken());

                int state = mediaController.getPlaybackState().getState();
                if(state == PlaybackStateCompat.STATE_PLAYING)
                    playStopBtn.setImageResource(R.drawable.action_stop);

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


    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }


    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }


    // handle the loadComplete event
    @SuppressWarnings("unused")
    public void onEventMainThread(LoadingCompleteEvent event) {
        // hide the progress bar once buffering is complete and playback can start
        if(event.isLoadingComplete()) {
            progressBar.setVisibility(View.INVISIBLE);
        }
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
