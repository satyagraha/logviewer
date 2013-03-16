package org.logviewer.services;

import java.io.IOException;

/**
 * Defines required capabilities for use of the LogManager class. 
 */
public interface MessageSender {

    /**
     * Send message back to client websocket.
     * 
     * @param messageString message text to send.
     * @throws IOException
     */
    void sendMessage(String messageString) throws IOException;

}
