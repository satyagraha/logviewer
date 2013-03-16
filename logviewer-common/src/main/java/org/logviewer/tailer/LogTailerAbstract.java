package org.logviewer.tailer;

import java.net.URI;
import java.util.concurrent.Executor;

import org.logviewer.services.LogTailer;
import org.logviewer.services.TailerCallback;

/**
 * Declares essential tailer facilities. 
 *
 */
public abstract class LogTailerAbstract implements LogTailer {
    
    protected final TailerCallback tailerCallback;
    protected final URI uri;
    protected final Executor executor;

    /**
     * Constructor.
     * 
     * @param tailerCallback
     * @param uri
     * @param executor
     */
    public LogTailerAbstract(TailerCallback tailerCallback, URI uri, Executor executor) {
        this.tailerCallback = tailerCallback;
        this.uri = uri;
        this.executor = executor;
    }

}
