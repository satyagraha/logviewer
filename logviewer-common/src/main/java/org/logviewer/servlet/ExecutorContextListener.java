package org.logviewer.servlet;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide an executor usable from web container.
 */
public class ExecutorContextListener implements ServletContextListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorContextListener.class);
    
    private ExecutorService executor;

    /**
     * Create daemon threads as required.
     */
    private static class DaemonThreadFactory implements ThreadFactory {

        private final ThreadFactory factory;

        DaemonThreadFactory() {
            this(Executors.defaultThreadFactory());
        }

        DaemonThreadFactory(ThreadFactory factory) {
            if (factory == null)
                throw new NullPointerException("factory cannot be null");
            this.factory = factory;
        }

        @Override
        public Thread newThread(Runnable r) {
            LOGGER.debug("newThread");
            final Thread t = factory.newThread(r);
            t.setDaemon(true);
            return t;
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        LOGGER.debug("contextInitialized");
        ServletContext context = arg0.getServletContext();
        int executors = 1;
        ThreadFactory daemonFactory = new DaemonThreadFactory();
        try {
            executors = Integer.parseInt(context.getInitParameter("executors"));
        } catch (NumberFormatException ignore) {
        }
        LOGGER.debug("executors: {}", executors);

        if (executors <= 1) {
            executor = Executors.newSingleThreadExecutor(daemonFactory);
        } else {
            executor = Executors.newFixedThreadPool(executors, daemonFactory);
        }
        context.setAttribute("executor", executor);
    }

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        LOGGER.debug("contextDestroyed");
        executor.shutdownNow();
    }

}
