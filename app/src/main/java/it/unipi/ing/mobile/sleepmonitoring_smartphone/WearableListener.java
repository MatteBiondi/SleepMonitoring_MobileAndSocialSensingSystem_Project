package it.unipi.ing.mobile.sleepmonitoring_smartphone;

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
        // TODO
        Toast.makeText(this, new String(messageEvent.getData()), Toast.LENGTH_SHORT).show();
    }
    public void onChannelOpened(ChannelClient.Channel channel) {
        // TODO
        Toast.makeText(this, channel.toString(), Toast.LENGTH_SHORT).show();
    }
}