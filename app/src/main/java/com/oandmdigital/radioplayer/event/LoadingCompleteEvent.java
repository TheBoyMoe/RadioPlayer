package com.oandmdigital.radioplayer.event;

public class LoadingCompleteEvent {

    private boolean loadingComplete;

    public LoadingCompleteEvent(boolean loadingComplete) {
        this.loadingComplete = loadingComplete;
    }

    public boolean isLoadingComplete() {
        return loadingComplete;
    }

}
