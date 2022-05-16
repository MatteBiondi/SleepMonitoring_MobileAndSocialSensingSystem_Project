package it.unipi.ing.mobile.sleepmonitoring_smartphone;

import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.wearable.ChannelClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
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

    @Override
    public void onChannelOpened(@NonNull ChannelClient.Channel channel) {
        Wearable.getChannelClient(getApplicationContext()).getInputStream(channel).addOnCompleteListener(task -> {
            Intent serviceIntent = new Intent(getApplicationContext(), WorkerService.class);
            serviceIntent.setAction("/start-service");
            //stream = task.getResult();
            startForegroundService(serviceIntent);
        });
    }

    @Override
    public void onChannelClosed(@NonNull ChannelClient.Channel channel, int i, int i1) {
        Intent serviceIntent = new Intent(this, WorkerService.class);
        serviceIntent.setAction("/stop-service");
        startForegroundService(serviceIntent);

    }
}