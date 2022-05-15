package it.unipi.ing.mobile.sleepmonitoring_smartphone;

import android.content.Intent;
import android.widget.Toast;

import com.google.android.gms.wearable.ChannelClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * The event from paired wearable app trigger the execution of the correspondent method defined in
 * in this class. The method is invoked only for those events thar are defined in the manifest file
 */

public class WearableListener extends WearableListenerService {
    public void onMessageReceived (MessageEvent messageEvent) {
        /* The message path can be '/start' or '/stop', it is used to control the lifecycle of
         * foreground service that implements the data receiver for wearable app
         */
        Intent serviceIntent = new Intent(this, WorkerService.class);
        serviceIntent.setAction(messageEvent.getPath());
        startForegroundService(serviceIntent);
    }

    public void onChannelOpened(ChannelClient.Channel channel) {
        // TODO
        Toast.makeText(this, channel.toString(), Toast.LENGTH_SHORT).show();
    }
}