package org.logviewer.servlet;

import java.util.Properties;
import java.util.concurrent.Executor;

import javax.servlet.GenericServlet;

import org.logviewer.services.LogConfig;
import org.logviewer.utility.LogConfigProperties;

/**
 * Provides implementation of LogConfig in a servlet container.
 */
public class LogConfigServlet implements LogConfig {

    private final GenericServlet servlet;
    private final static Properties properties = new LogConfigProperties(); 

    /**
     * Constructor.
     * 
     * @param servlet
     */
    public LogConfigServlet(GenericServlet servlet) {
        this.servlet = servlet;
    }
    
    /* (non-Javadoc)
     * @see org.logviewer.services.LogConfig#getExecutor()
     */
    @Override
    public Executor getExecutor() {
        return (Executor) servlet.getServletContext().getAttribute("executor");
    }
    
    /* (non-Javadoc)
     * @see org.logviewer.services.LogConfig#getProperties()
     */
    @Override
    public Properties getProperties() {
        return properties;
    }

}
