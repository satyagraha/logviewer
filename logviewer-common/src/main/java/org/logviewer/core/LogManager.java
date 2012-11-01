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
import java.util.Properties;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.logviewer.core.LogMessage.Action;
import org.logviewer.tailer.LogTailerAbstract;
import org.logviewer.tailer.LogTailerCommons;
import org.logviewer.tailer.LogTailerSsh;
import org.logviewer.tailer.TailerCallback;
import org.logviewer.tailer.TailerSsh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSch;

/**
 * Provides log management capabilities.
 */
public class LogManager implements TailerCallback {

    public static final String LOG_MANAGER_LOG_DIR_KEY = "LogManager.logDir";
    public static final String LOG_MANAGER_LOG_FILTER_KEY = "LogManager.logFilter";

    private static final Logger LOGGER = LoggerFactory.getLogger(LogManager.class);
    
    private final LogConfig logConfig;
    private final MessageSender messageSender;
    
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Constructor.
     * 
     * @param logConfig
     * @param messageSender
     */
    public LogManager(LogConfig logConfig, MessageSender messageSender) {
        LOGGER.debug("LogManager");
        this.logConfig = logConfig;
        this.messageSender = messageSender;
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
            openLogLocal(message.filenames.get(0));
            break;
        case OPEN_LOG_REMOTE:
            openLogRemote(message.filenames.get(0), message.password, message.passphrase);
            break;
        case CLOSE_LOG:
            closeLog();
            break;
        default:
            LOGGER.error("unexpected message action");
        }
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
    
    private LogTailerAbstract logTailer;

    private void openLogLocal(String filename) throws IOException {
        LOGGER.debug("filename: {}", filename);

        File file = new File(getLogDir(), filename);
        URI uri;
        try {
             uri = new URI("file", "///" + file.getAbsolutePath(), null);
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
        LOGGER.debug("uri: {}", uri);

        logTailer = new LogTailerCommons(this, uri, logConfig.getExecutor());
        logTailer.start();
    }

    private void openLogRemote(String uri_string, String password, String passphrase) throws IOException {
        LOGGER.debug("uri_string: {}", uri_string);
        
        URI uri;
        try {
            uri = new URI(uri_string);
        } catch (URISyntaxException e) {
            handleException(e);
            throw new IOException(e);
        }
        LOGGER.debug("uri: {}", uri);
        
        Properties properties = new Properties(logConfig.getProperties());
        if (StringUtils.isNotEmpty(password)) {
            properties.put(LogTailerSsh.LOG_TAILER_SSH_PASSWORD_KEY, password);
        }
        if (StringUtils.isNotEmpty(passphrase)) {
            properties.put(LogTailerSsh.LOG_TAILER_SSH_PASSPHRASE_KEY, passphrase);
        }

        JSch jsch = new JSch();
        TailerSsh tailer = new TailerSsh(jsch, uri, this, logConfig.getExecutor());
        logTailer = new LogTailerSsh(this, uri, logConfig.getExecutor(), properties, tailer);
        logTailer.start();
    }
    
    private void closeLog() {
        LOGGER.debug("closeLog");
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
