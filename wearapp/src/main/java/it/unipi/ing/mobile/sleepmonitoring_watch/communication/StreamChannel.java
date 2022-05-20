package it.unipi.ing.mobile.sleepmonitoring_watch.communication;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.ChannelClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamChannel extends ChannelClient.ChannelCallback {
    public final String TAG = "StreamChannel";
    private final ChannelClient channel_client;
    private ChannelClient.Channel stream_channel;
    private OutputStream out_stream;
    private InputStream in_stream;

    public StreamChannel(String node, String endpoint, ChannelClient channel_client)  {
        this.channel_client = channel_client;

        // Open channel
        Task<ChannelClient.Channel> channelTask = channel_client.openChannel(node, endpoint);

        channelTask.addOnSuccessListener(channel -> {
            // Set opened channel
            stream_channel = channel;

            // Get output stream
            Task<OutputStream> out_stream_task = channel_client.getOutputStream(channel);
            out_stream_task.addOnSuccessListener(outputStream -> out_stream = outputStream);


            // Get input stream
            Task<InputStream> in_stream_task = channel_client.getInputStream(channel);
            in_stream_task.addOnSuccessListener(stream -> in_stream = stream);

            channel_client.registerChannelCallback(stream_channel, this);
        });
    }

    public OutputStream getOutStream() throws IOException {
        if (out_stream == null)
            throw new IOException("Stream is null");
        return out_stream;
    }

    public InputStream getInStream() throws IOException{
        if (in_stream == null)
            throw new IOException("Stream is null");
        return in_stream;
    }

    public boolean sendMessage(byte[] message) throws IOException {
        if (out_stream == null) {
            return false;
        }
        out_stream.write(message);
        out_stream.flush();

        Log.i(TAG, "Message sent");

        return true;
    }

    public int receiveMessage(byte[] buffer) throws IOException {
        if (in_stream == null) {
            return -1;
        }
        return in_stream.read(buffer, 0, buffer.length);
    }

    public void close(){
        if (stream_channel == null)
            return;
        try {
            in_stream.close();
            in_stream = null;
            out_stream.close();
            out_stream = null;
        }
        catch (IOException e) { e.printStackTrace(); }
        Task<Void> close_task = channel_client.close(stream_channel);
        close_task.addOnSuccessListener( _var -> Log.i(TAG, "Channel close successfully"));
        close_task.addOnFailureListener( _var -> Log.i(TAG, "Channel close failed"));
    }

    @Override
    public void onChannelClosed(@NonNull ChannelClient.Channel channel, int closeReason, int appSpecificErrorCode){
        super.onChannelClosed(channel, closeReason, appSpecificErrorCode);
        Log.i(TAG, "Channel closed");
        try {
            if (in_stream != null)
                in_stream.close();
            in_stream = null;
            if (out_stream != null)
                out_stream.close();
            out_stream = null;
        }
        catch (IOException e) { e.printStackTrace(); }
    }
}