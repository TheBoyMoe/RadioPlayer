package com.oandmdigital.radioplayer.event;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.greenrobot.event.EventBus;

public class MusicIntentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
            // forward the event to the PlayerFragment via the event bus
            EventBus.getDefault().post(new PlaybackServiceEvent(PlaybackServiceEvent.EVENT_BECOMING_NOISY));
        }
    }
}
