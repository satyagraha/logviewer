package org.logviewer.utility;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Create daemon threads as required.
 */
public class DaemonThreadFactory implements ThreadFactory {

    private final ThreadFactory threadFactory;

    public DaemonThreadFactory() {
        this(Executors.defaultThreadFactory());
    }

    public DaemonThreadFactory(ThreadFactory threadFactory) {
        if (threadFactory == null) {
            throw new NullPointerException("factory cannot be null");
        }
        this.threadFactory = threadFactory;
    }

    @Override
    public Thread newThread(Runnable r) {
        final Thread t = threadFactory.newThread(r);
        t.setDaemon(true);
        return t;
    }
}