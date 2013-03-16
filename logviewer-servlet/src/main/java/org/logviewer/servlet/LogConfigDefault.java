package org.logviewer.servlet;

import java.util.Properties;
import java.util.concurrent.Executor;

import javax.servlet.GenericServlet;

import org.logviewer.services.LogConfig;
import org.logviewer.utility.LogConfigProperties;

public class LogConfigDefault implements LogConfig {

    private final GenericServlet servlet;

    public LogConfigDefault(GenericServlet servlet) {
        this.servlet = servlet;
    }
    
    @Override
    public Executor getExecutor() {
        return (Executor) servlet.getServletContext().getAttribute("executor");
    }
    
    @Override
    public Properties getProperties() {
        return new LogConfigProperties();
    }

}
