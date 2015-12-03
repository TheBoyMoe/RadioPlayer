package com.oandmdigital.radioplayer.event;

public class PlaybackServiceEvent {


    public static final String ERROR_NO_STREAM_FOUND = "com.oandmdigital.radioplayer.event.ERROR_NO_STREAM_FOUND";
    public static final String ERROR_BUFFERING_AUDIO = "com.oandmdigital.radioplayer.event.ERROR_BUFFERING_AUDIO";
    public static final String ERROR_PLAYING_MEDIA = "com.oandmdigital.radioplayer.event.ERROR_PLAYING_MEDIA";
    public static final String EVENT_STREAM_FINISHED = "com.oandmdigital.radioplayer.event.EVENT_STREAM_FINISHED";
    public static final String EVENT_CANNOT_GAIN_FOCUS = "com.oandmdigital.radioplayer.event.EVENT_CANNOT_GAIN_FOCUS";
    public static final String EVENT_LOST_FOCUS = "com.oandmdigital.radioplayer.event.EVENT_LOST_FOCUS";
    public static final String EVENT_GAINED_FOCUS = "com.oandmdigital.radioplayer.event.EVENT_GAINED_FOCUS";

    private String message;

    public PlaybackServiceEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
