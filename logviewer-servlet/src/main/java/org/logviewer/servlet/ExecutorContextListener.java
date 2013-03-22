package org.logviewer.servlet;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.logviewer.utility.DaemonThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide an executor usable from web container.
 */
public class ExecutorContextListener implements ServletContextListener {
    static final Logger LOGGER = LoggerFactory.getLogger(ExecutorContextListener.class);
    
    private ExecutorService executor;

    /* (non-Javadoc)
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        LOGGER.debug("contextInitialized");
        ServletContext context = servletContextEvent.getServletContext();
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

    /* (non-Javadoc)
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        LOGGER.debug("contextDestroyed");
        executor.shutdownNow();
    }

}
