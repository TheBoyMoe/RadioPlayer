package com.oandmdigital.radioplayer.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.oandmdigital.radioplayer.R;

public class EmptyStationFragment extends Fragment {


    public static EmptyStationFragment newInstance() {
        return new EmptyStationFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        TextView text = (TextView) inflater.inflate(R.layout.empty_station_fragment, container, false);
        text.setText(R.string.empty_station_fragment_text);

        return text;
    }

}
