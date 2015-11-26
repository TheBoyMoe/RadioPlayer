package com.oandmdigital.radioplayer.event;

import com.oandmdigital.radioplayer.model.Station;

import java.util.ArrayList;

public class DownloadStationsEvent {

    private ArrayList<Station> stationList;

    public DownloadStationsEvent(ArrayList<Station> stationList) {
        this.stationList = stationList;
    }

    public ArrayList<Station> getStationList() {
        return stationList;
    }
}
