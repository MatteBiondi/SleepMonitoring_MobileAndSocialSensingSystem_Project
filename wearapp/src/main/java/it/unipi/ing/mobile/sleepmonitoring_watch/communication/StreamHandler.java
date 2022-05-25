package it.unipi.ing.mobile.sleepmonitoring_watch.communication;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Interface to implement used by StreamChannel as callback when the stream is ready
 */
public interface StreamHandler {
    void setInputStream(InputStream input_stream);
    void setOutputStream(OutputStream output_stream);
}
