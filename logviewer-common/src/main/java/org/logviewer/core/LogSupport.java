package org.logviewer.core;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;

/**
 * Defines required capabilities for use of the LogManager class. 
 */
public interface LogSupport {

    /**
     * Get log directory.
     * 
     * @return base directory where logs are held.
     */
    File getLogDir();

    /**
     * Get simple shell wild-card log filter, e.g. *.log.
     * 
     * @return wild-card log pattern.
     */
    String getLogFilter();
    
    /**
     * Send message back to client websocket.
     * 
     * @param messageString message text to send.
     * @throws IOException
     */
    void sendMessage(String messageString) throws IOException;

    /**
     * Get an executor for use by log tailer.
     * 
     * @return an executor.
     */
    Executor getExecutor();
}
