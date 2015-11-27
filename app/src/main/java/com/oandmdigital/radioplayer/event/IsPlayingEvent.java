package com.oandmdigital.radioplayer.event;

public class IsPlayingEvent {

    private boolean isPlaying;

    public IsPlayingEvent(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    public boolean isPlaying() {
        return isPlaying;
    }
}
