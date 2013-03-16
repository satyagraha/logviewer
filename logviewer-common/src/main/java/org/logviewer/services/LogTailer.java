package org.logviewer.services;

public interface LogTailer {

    /**
     * Commence log tailing.
     */
    public void start();

    /**
     * Cease log tailing.
     */
    public void stop();

}