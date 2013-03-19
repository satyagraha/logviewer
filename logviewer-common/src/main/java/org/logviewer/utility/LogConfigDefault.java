package org.logviewer.utility;

import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import org.logviewer.services.LogConfig;

public class LogConfigDefault implements LogConfig {
    
    private final static Properties properties = new LogConfigProperties();
    private final static ExecutorService executorService = new ExecutorDefault(properties);
    
    @Override
    public Executor getExecutor() {
        return executorService;
    }
    
    @Override
    public Properties getProperties() {
        return properties;
    }

}
