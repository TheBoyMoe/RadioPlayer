package com.oandmdigital.radioplayer.event;

import com.oandmdigital.radioplayer.model.Station;

public class StationOnClickEvent {

    private Station station;


    public StationOnClickEvent(Station station) {
        this.station = station;
    }

    public Station getStation() {
        return station;
    }
}
