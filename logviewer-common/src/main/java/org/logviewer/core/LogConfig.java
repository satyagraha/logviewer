package org.logviewer.core;

import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * Defines required capabilities for use of the LogManager class. 
 */
public interface LogConfig {

    /**
     * Get an executor for use by log tailer.
     * 
     * @return an executor.
     */
    Executor getExecutor();

    /**
     * Get additional optional configuration properties.
     * 
     * @return properties.
     */
    Properties getProperties();
}
