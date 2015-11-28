package com.oandmdigital.radioplayer.ui;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.oandmdigital.radioplayer.R;
import com.oandmdigital.radioplayer.common.LoggingFragment;
import com.oandmdigital.radioplayer.event.IsPlayingEvent;
import com.oandmdigital.radioplayer.event.LoadingCompleteEvent;
import com.oandmdigital.radioplayer.event.RadioServiceEvent;
import com.oandmdigital.radioplayer.model.Station;
import com.oandmdigital.radioplayer.service.RadioService;

import de.greenrobot.event.EventBus;

public class PlayerFragment extends Fragment implements View.OnClickListener{

    public static final String STATION_PARCELABLE = "station";
    private static final String IS_PLAYING = "isPlaying";
    private static final String LOADING_COMPLETE = "loadingComplete";
    private static final String LOG_TAG = PlayerFragment.class.getSimpleName();
    private Station stn;
    private ImageButton playStopBtn;
    private ProgressBar progressBar;
    private boolean isPlaying = false;
    private boolean loadingComplete = true;

    public static PlayerFragment newInstance(Station station) {

        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
        args.putParcelable(STATION_PARCELABLE, station);
        fragment.setArguments(args);

        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        stn = getArguments().getParcelable(STATION_PARCELABLE);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.player, container, false);
        TextView textView = (TextView) view.findViewById(R.id.station_name);
        playStopBtn = (ImageButton) view.findViewById(R.id.play_stop_button);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);

        // default setup on start
        textView.setText(stn.getName());
        playStopBtn.setOnClickListener(this);

        if(savedInstanceState != null) {
            isPlaying = savedInstanceState.getBoolean(IS_PLAYING);
            loadingComplete = savedInstanceState.getBoolean(LOADING_COMPLETE);
        }

        // set the appropriate playStopBtn & progress bar
        if(isPlaying)
            playStopBtn.setImageResource(R.drawable.action_stop);
        else
            playStopBtn.setImageResource(R.drawable.action_play);

        if(!loadingComplete)
            progressBar.setVisibility(View.VISIBLE); // service is being buffered
        else
            progressBar.setVisibility(View.INVISIBLE);

        return view;
    }


    @Override
    public void onClick(View v) {

        Intent intent = new Intent(getActivity(), RadioService.class);
        intent.putExtra(STATION_PARCELABLE, stn);

        if(!isPlaying) {
            playStopBtn.setImageResource(R.drawable.action_stop);
            progressBar.setVisibility(View.VISIBLE);
            Log.d(LOG_TAG, "Launching Radio Service");
            loadingComplete = false;
            // start Radio service
            getActivity().startService(intent);

        } else {
            playStopBtn.setImageResource(R.drawable.action_play);
            // stop Radio Service
            getActivity().stopService(intent);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_PLAYING, isPlaying);
        outState.putBoolean(LOADING_COMPLETE, loadingComplete);
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


    // handle the isPlaying event
    @SuppressWarnings("unused")
    public void onEventMainThread(IsPlayingEvent event) {
        if(event.isPlaying())
            isPlaying = true;
        else
            isPlaying = false;
    }


    // handle the loadComplete event
    @SuppressWarnings("unused")
    public void onEventMainThread(LoadingCompleteEvent event) {
        // hide the progress bar once buffering is complete and playback can start
        if(event.isLoadingComplete()) {
            loadingComplete = true;
            progressBar.setVisibility(View.INVISIBLE);
        }

    }

    // handle RadioService messages
    @SuppressWarnings("unused")
    public void onEventMainThread(RadioServiceEvent event) {
        switch (event.getMessage()) {
            case RadioServiceEvent.ERROR_BUFFERING_AUDIO:
                Toast.makeText(getActivity(), "Error buffering audio", Toast.LENGTH_SHORT).show();
                break;

            case RadioServiceEvent.ERROR_NO_STREAM_FOUND:
                Toast.makeText(getActivity(), "No stream found", Toast.LENGTH_SHORT).show();
                break;

            case RadioServiceEvent.ERROR_PLAYING_MEDIA:
                Toast.makeText(getActivity(), "Error playing media", Toast.LENGTH_SHORT).show();
                break;

            case RadioServiceEvent.EVENT_STREAM_FINISHED:
                Toast.makeText(getActivity(), "Stream finished", Toast.LENGTH_SHORT).show();
                break;
        }
    }


}
