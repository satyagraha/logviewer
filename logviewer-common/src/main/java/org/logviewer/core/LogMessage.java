package org.logviewer.core;

import java.util.List;

/**
 * Encodes communication actions between client and server processes.
 */
public class LogMessage {
    
    enum Action {
        PING_SERVER,
        PONG_CLIENT,
        GET_LOG_FILENAMES,
        GOT_LOG_FILENAMES,
        OPEN_LOG_LOCAL,
        OPEN_LOG_REMOTE,
        LOG_UPDATED,
        CLOSE_LOG,
    }

    public Action action;
    public String directory;
    public String password;
    public String passphrase;
    public List<String> filenames;
    public List<String> content;

}