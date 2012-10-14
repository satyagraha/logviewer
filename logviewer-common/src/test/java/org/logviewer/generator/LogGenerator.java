package org.logviewer.generator;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Simple program to generate log file entries: runs indefinitely until interrupted.
 */
public class LogGenerator {

    public static void main(String[] args) throws Exception {

        PropertyConfigurator.configure(LogGenerator.class.getResource("/generator.log4j.properties"));
        Logger LOGGER = Logger.getLogger(LogGenerator.class);
        LOGGER.info("Entering application.");
        while (true) {
            LOGGER.info("time: " + System.currentTimeMillis());
            Thread.sleep(1000);
        }

    }

}
