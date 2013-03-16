package org.logviewer.tailer;

import java.io.File;
import java.net.URI;
import java.util.concurrent.Executor;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.logviewer.services.TailerCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tailer using Apache Commons tailer class (for server-local log files).
 */
public class LogTailerCommons extends LogTailerAbstract {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogTailerCommons.class);

    private class LogTailer extends TailerListenerAdapter implements Runnable {

        private final Tailer tailer;

        LogTailer(File file) {
            LOGGER.debug("LogTailer: {}", file);
            tailer = new Tailer(file, this);
        }

        @Override
        public void run() {
            LOGGER.debug("LogTailer.run");
            tailer.run();
        }

        @Override
        public void handle(String line) {
            tailerCallback.handleLine(line);
        }

        @Override
        public void handle(Exception e) {
            tailerCallback.handleException(e);
        }
    }

    private final LogTailer logTailer;

    public LogTailerCommons(TailerCallback tailerCallback, URI uri, Executor executor) {
        super(tailerCallback, uri, executor);
        String path = uri.getPath();
        LOGGER.debug("path: {}", path);
        File file = new File(path);
        LOGGER.debug("file: {}", file);
        logTailer = new LogTailer(file);
    }

    @Override
    public void start() {
        executor.execute(logTailer);
    }

    @Override
    public void stop() {
        logTailer.tailer.stop();
    }
}
