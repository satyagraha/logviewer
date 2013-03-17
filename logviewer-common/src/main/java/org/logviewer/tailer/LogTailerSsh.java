package org.logviewer.tailer;

import java.io.IOException;
import java.net.URI;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.logviewer.services.TailerCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSchException;

/**
 * Tailer using LogTailerSsh tailer class (for server-remote log files).
 */
public class LogTailerSsh extends LogTailerAbstract {

    public static final String LOG_TAILER_SSH_PRIVATE_KEY_FILE_PATH_KEY = "LogTailerSsh.privateKeyFilePath";
    public static final String LOG_TAILER_SSH_PASSWORD_KEY = "LogTailerSsh.password";
    public static final String LOG_TAILER_SSH_PASSPHRASE_KEY = "LogTailerSsh.passphrase";
    public static final String LOG_TAILER_SSH_TAIL_COMMAND_KEY = "LogTailerSsh.tailCommand";

    private static final Logger LOGGER = LoggerFactory.getLogger(LogTailerSsh.class);

    private final TailerSsh tailer;

    public LogTailerSsh(TailerCallback tailerCallback, URI uri, Executor executor, Properties properties, TailerSsh tailer) throws IOException {
        super(tailerCallback, uri, executor);
        this.tailer = tailer;
        
        String privateKeyFilePath = properties.getProperty(LOG_TAILER_SSH_PRIVATE_KEY_FILE_PATH_KEY);
        LOGGER.debug("privateKeyFilePath: {}", privateKeyFilePath);
        
        if (privateKeyFilePath != null) {
            String passphrase = properties.getProperty(LOG_TAILER_SSH_PASSPHRASE_KEY);
            LOGGER.debug("passphrase found: {}", passphrase != null);
            try {
                if (passphrase != null) {
                    tailer.getJsch().addIdentity(privateKeyFilePath, passphrase);
                } else {
                    tailer.getJsch().addIdentity(privateKeyFilePath);
                }
            } catch (JSchException e) {
                throw new IOException(e);
            }
        }
        
        String password = properties.getProperty(LOG_TAILER_SSH_PASSWORD_KEY);
        LOGGER.debug("password found: {}", password != null);
        if (password != null) {
            tailer.getSession().setPassword(password);
        }
        
        tailer.getSession().setConfig("StrictHostKeyChecking", "no");
        
        String tailCommand = properties.getProperty(LOG_TAILER_SSH_TAIL_COMMAND_KEY);
        if (tailCommand != null) {
            LOGGER.debug("tailCommand: {}", tailCommand);
            tailer.setTailCommand(tailCommand);
        }
    }

    @Override
    public void start() {
        tailer.start();
    }

    @Override
    public void stop() {
        tailer.stop();
    }
}
