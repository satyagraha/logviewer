package org.logviewer.core;

import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.collection.LambdaCollections.with;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.logviewer.core.LogMessage.Action;
import org.logviewer.services.LogConfig;
import org.logviewer.services.LogTailer;
import org.logviewer.services.MessageSender;
import org.logviewer.services.TailerCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides log management capabilities.
 */
public class LogManager implements TailerCallback {

    public static final String LOG_MANAGER_LOG_DIR_KEY = "LogManager.logDir";
    public static final String LOG_MANAGER_LOG_FILTER_KEY = "LogManager.logFilter";

    private static final Logger LOGGER = LoggerFactory.getLogger(LogManager.class);

    private final LogConfig logConfig;
    private final MessageSender messageSender;
    private final TailerFactory tailerFactory;

    private final ObjectMapper mapper = new ObjectMapper();

    private LogTailer logTailer;

    /**
     * Constructor.
     * 
     * @param logConfig
     * @param messageSender
     */
    public LogManager(LogConfig logConfig, MessageSender messageSender, TailerFactory tailerFactory) {
        LOGGER.debug("LogManager");
        this.logConfig = logConfig;
        this.messageSender = messageSender;
        this.tailerFactory = tailerFactory;
    }
    
    public LogManager(LogConfig logConfig, MessageSender messageSender) {
       this(logConfig, messageSender, new TailerFactory(logConfig));
    }

    /**
     * Process message as defined by action field.
     * 
     * @param messageString
     * @throws IOException
     * @throws URISyntaxException
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
        case PING_SERVER:
            pongClient();
            break;
        case GET_LOG_FILENAMES:
            getLogFilenames();
            break;
        case OPEN_LOG_LOCAL:
            openLogLocal(message);
            break;
        case OPEN_LOG_REMOTE:
            openLogRemote(message);
            break;
        case CLOSE_LOG:
            close();
            break;
        default:
            LOGGER.error("unexpected message action");
        }
    }

    private void openLogLocal(LogMessage message) throws IOException {
        String filename = message.filenames.get(0);
        LOGGER.debug("filename: {}", filename);

        File file = new File(getLogDir(), filename);
        URI uriLocal;
        try {
            uriLocal = new URI("file", "///" + file.getAbsolutePath(), null);
        } catch (URISyntaxException e) {
            handleException(e);
            throw new IOException(e);
        }
        logTailer = tailerFactory.logLocalTailer(uriLocal, this);
        logTailer.start();
    }

    private void openLogRemote(LogMessage message) throws IOException {
        String uriString = message.filenames.get(0);
        LOGGER.debug("uriString: {}", uriString);

        URI uriRemote;
        try {
            uriRemote = new URI(uriString);
        } catch (URISyntaxException e) {
            handleException(e);
            throw new IOException(e);
        }
        logTailer = tailerFactory.logRemoteTailer(uriRemote, message.password, message.passphrase, this);
        logTailer.start();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.logviewer.core.TailerCallback#handleLine(java.lang.String)
     */
    @Override
    public void handleLine(String line) {
        LOGGER.debug("handleLine: {}", line);
        sendLine(line);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.logviewer.core.TailerCallback#handleException(java.lang.Exception)
     */
    @Override
    public void handleException(Exception e) {
        LOGGER.debug("handleException", e);
        sendLine(String.format("[%s]", e.getMessage()));
        // tailer.stop();
    }

    private void pongClient() throws IOException {
        LOGGER.debug("pongClient");

        LogMessage response = new LogMessage();
        response.action = Action.PONG_CLIENT;
        messageSender.sendMessage(mapper.writeValueAsString(response));
    }

    private void getLogFilenames() throws IOException {
        LOGGER.debug("getLogFilenames");

        File logDir = getLogDir();
        FileFilter fileFilter = getLogFilter();
        File[] files = logDir.listFiles(fileFilter);
        List<String> filenames = with(files).extract(on(File.class).getName()).sort(on(String.class).toLowerCase());
        LOGGER.debug("filenames: {}", filenames);

        LogMessage response = new LogMessage();
        response.action = Action.GOT_LOG_FILENAMES;
        response.directory = logDir.getAbsolutePath();
        response.filenames = filenames;
        messageSender.sendMessage(mapper.writeValueAsString(response));
    }

    private File getLogDir() {
        String logDir = logConfig.getProperties().getProperty(LOG_MANAGER_LOG_DIR_KEY);
        return new File(logDir);
    }

    private WildcardFileFilter getLogFilter() {
        String logFilter = logConfig.getProperties().getProperty(LOG_MANAGER_LOG_FILTER_KEY);
        return new WildcardFileFilter(logFilter);
    }

    public void close() {
        LOGGER.debug("close");
        if (logTailer != null) {
            logTailer.stop();
        }
        logTailer = null;
    }

    private void sendLine(String line) {
        LogMessage response = new LogMessage();
        response.action = Action.LOG_UPDATED;
        response.content = Arrays.asList(line);

        try {
            messageSender.sendMessage(mapper.writeValueAsString(response));
        } catch (IOException e) {
            LOGGER.debug("exception", e);
            throw new RuntimeException(e);
        }
    }

}
