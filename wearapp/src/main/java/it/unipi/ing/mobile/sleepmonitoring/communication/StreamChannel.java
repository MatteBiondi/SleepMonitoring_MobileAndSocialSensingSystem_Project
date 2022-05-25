package it.unipi.ing.mobile.sleepmonitoring.communication;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.ChannelClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The class wraps the Wearable API to set a communication channel between watch and mobile
 * The channel offers the possibility to open a bidirectional stream used to simplify the messages
 * exchange
 */
public class StreamChannel extends ChannelClient.ChannelCallback {
    public final String TAG = "STREAM_CHANNEL";
    private final ChannelClient channel_client;
    private ChannelClient.Channel stream_channel;
    private OutputStream out_stream;
    private InputStream in_stream;

    public StreamChannel(String node, String endpoint, ChannelClient channel_client, StreamHandler stream_handler)  {
        this.channel_client = channel_client;

        // Open channel
        Task<ChannelClient.Channel> channelTask = channel_client.openChannel(node, endpoint);

        channelTask.addOnSuccessListener(channel -> {
            // Set opened channel
            stream_channel = channel;

            // Get output stream
            Task<OutputStream> out_stream_task = channel_client.getOutputStream(channel);
            out_stream_task.addOnSuccessListener(outputStream -> {
                out_stream = outputStream;
                stream_handler.setOutputStream(out_stream);
            });


            // Get input stream
            Task<InputStream> in_stream_task = channel_client.getInputStream(channel);
            in_stream_task.addOnSuccessListener(input_stream -> {
                in_stream = input_stream;
                stream_handler.setInputStream(input_stream);
            });

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
            if(in_stream != null)
                in_stream.close();
            in_stream = null;
            if (out_stream != null)
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