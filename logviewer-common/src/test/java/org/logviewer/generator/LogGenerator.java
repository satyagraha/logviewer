package org.logviewer.generator;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Simple program to generate log file entries.
 * Add optional command line argument to limit number of limes to emit.
 */
public class LogGenerator {

    public static void main(String[] args) throws Exception {

        int limit = Integer.MAX_VALUE;
        if (args.length >= 1) {
            limit = Integer.parseInt(args[0]);
        }
        
        PropertyConfigurator.configure(LogGenerator.class.getResource("/generator.log4j.properties"));
        Logger LOGGER = Logger.getLogger(LogGenerator.class);
        LOGGER.info("Entering application.");
        for (int count = 0 ; count < limit; count++) {
            LOGGER.info("time: " + System.currentTimeMillis());
            Thread.sleep(1000);
        }

    }

}
