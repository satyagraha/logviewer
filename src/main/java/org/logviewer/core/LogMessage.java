package org.logviewer.core;

import java.util.List;

public class LogMessage {
    enum Action {
        GET_LOG_FILENAMES,
        GOT_LOG_FILENAMES,
        OPEN_LOG,
        LOG_UPDATED,
        CLOSE_LOG,
    }

    public Action action;
    public String directory;
    public List<String> filenames;
    public List<String> content;

}