package com.lab.notes.data.remote;

/**
 * Signals that communication with the cloud storage failed.
 */
public class RemoteSyncException extends Exception {

    public RemoteSyncException(String message, Throwable cause) {
        super(message, cause);
    }
}