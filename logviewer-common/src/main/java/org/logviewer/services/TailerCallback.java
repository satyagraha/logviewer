package org.logviewer.services;

/**
 * Handle tailer callback events. 
 */
public interface TailerCallback {

    /**
     * Process tailer line.
     * 
     * @param line
     */
    public void handleLine(String line);

    /**
     * Process tailer exception.
     * 
     * @param e
     */
    public void handleException(Exception e);

}