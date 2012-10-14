package org.logviewer.core;

import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.collection.LambdaCollections.with;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.logviewer.core.LogMessage.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides log management capabilities. 
 */
public class LogManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogManager.class);

    private final LogSupport logSupport;
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Constructor.
     * 
     * @param logSupport
     */
    public LogManager(LogSupport logSupport) {
        LOGGER.debug("LogManager");
        this.logSupport = logSupport;
    }

    /**
     * Process message as defined by action field.
     * 
     * @param messageString
     * @throws IOException
     */
    public void handleMessage(String messageString) throws IOException {
        LOGGER.debug("messageString: " + messageString);
        LogMessage message;
        try {
            message = mapper.readValue(messageString, LogMessage.class);
        } catch (JsonProcessingException jpe) {
            LOGGER.error("message format error", jpe);
            throw jpe;
        }
        LOGGER.debug("message.action: " + message.action);
        switch (message.action) {
        case GET_LOG_FILENAMES:
            getLogFilenames();
            break;
        case OPEN_LOG:
            openLog(message.filenames.get(0));
            break;
        case CLOSE_LOG:
            closeLog();
            break;
        default:
            LOGGER.error("unexpected message action");
        }
    }

    private void getLogFilenames() throws IOException {
        LOGGER.debug("getLogFilenames");
        
        File logDir = logSupport.getLogDir();
        FileFilter fileFilter = new WildcardFileFilter(logSupport.getLogFilter());
        File[] files = logDir.listFiles(fileFilter);
        List<String> filenames = with(files).extract(on(File.class).getName()).sort(on(String.class).toLowerCase());
        LOGGER.debug("filenames: {}", filenames);
        
        LogMessage response = new LogMessage();
        response.action = Action.GOT_LOG_FILENAMES;
        response.directory = logDir.getAbsolutePath();
        response.filenames = filenames;
        logSupport.sendMessage(mapper.writeValueAsString(response));
    }

    /**
     * Provides log tailing facilities. 
     */
    private class LogTailer extends TailerListenerAdapter implements Runnable {

        final Tailer tailer;

        LogTailer(File file) {
            LOGGER.debug("LogTailer: {}", file);
            tailer = new Tailer(file, this);
        }

        @Override
        public void run() {
            LOGGER.debug("LogTailer.run");
            tailer.run();
        }

        /* (non-Javadoc)
         * @see org.apache.commons.io.input.TailerListenerAdapter#handle(java.lang.String)
         */
        @Override
        public void handle(String line) {
            LOGGER.debug("handle: {}", line);
            
            LogMessage response = new LogMessage();
            response.action = Action.LOG_UPDATED;
            response.content = Arrays.asList(line);
            
            try {
                logSupport.sendMessage(mapper.writeValueAsString(response));
            } catch (IOException e) {
                LOGGER.debug("exception", e);
                throw new RuntimeException(e);
            }
        }
        
        @Override
        public void handle(Exception e) {
            LOGGER.debug("handle", e);
            tailer.stop();
        }
    }

    private LogTailer logTailer;

    private void openLog(String filename) throws IOException {
        LOGGER.debug("openLog: {}", filename);

        logTailer = new LogTailer(new File(logSupport.getLogDir(), filename));
        logSupport.getExecutor().execute(logTailer);
    }

    private void closeLog() {
        LOGGER.debug("closeLog");
        if (logTailer != null) {
            logTailer.tailer.stop();
        }
        logTailer = null;
    }

}
