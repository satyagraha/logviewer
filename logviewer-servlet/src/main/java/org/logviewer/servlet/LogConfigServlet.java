package org.logviewer.servlet;

import java.util.Properties;
import java.util.concurrent.Executor;

import javax.servlet.GenericServlet;

import org.logviewer.services.LogConfig;
import org.logviewer.utility.LogConfigProperties;

public class LogConfigServlet implements LogConfig {

    private final GenericServlet servlet;
    private final static Properties properties = new LogConfigProperties(); 

    public LogConfigServlet(GenericServlet servlet) {
        this.servlet = servlet;
    }
    
    @Override
    public Executor getExecutor() {
        return (Executor) servlet.getServletContext().getAttribute("executor");
    }
    
    @Override
    public Properties getProperties() {
        return properties;
    }

}
