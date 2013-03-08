package org.logviewer.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.Executor;

import javax.servlet.GenericServlet;

import org.logviewer.core.LogConfig;

public class LogConfigDefault implements LogConfig {

//    private static final Logger LOGGER = LoggerFactory.getLogger(LogConfigDefault.class);

    @SuppressWarnings("serial")
    private static class LogConfigProperties extends Properties {
        
        private static final String LOG_CONFIG_DEFAULT_PROPERTIES = "LogConfigDefault.properties";

        private LogConfigProperties() {
            super();
            Properties defaultProperties = new Properties();
            try {
                InputStream defaultPropertiesStream = getClass().getClassLoader().getResourceAsStream(LOG_CONFIG_DEFAULT_PROPERTIES);
                defaultProperties.load(defaultPropertiesStream);
                defaultPropertiesStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            putAll(System.getProperties());
            defaults = defaultProperties;
        }
        
    }
    
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
