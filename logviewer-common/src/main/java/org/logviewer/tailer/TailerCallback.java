package org.logviewer.tailer;

/**
 * Handle tailer callback events. 
 */
public interface TailerCallback {

    /**
     * Process tailer line.
     * 
     * @param line
     */
    public abstract void handleLine(String line);

    /**
     * Process tailer exception.
     * 
     * @param e
     */
    public abstract void handleException(Exception e);

}