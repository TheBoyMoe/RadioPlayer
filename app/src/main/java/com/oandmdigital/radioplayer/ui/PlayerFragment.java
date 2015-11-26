package com.oandmdigital.radioplayer.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.oandmdigital.radioplayer.R;
import com.oandmdigital.radioplayer.common.LoggingFragment;
import com.oandmdigital.radioplayer.model.Station;

public class PlayerFragment extends LoggingFragment{

    public static final String STATION_PARCELABLE = "station";
    private Station stn;

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
        textView.setText(stn.getName());

        return view;
    }
}
