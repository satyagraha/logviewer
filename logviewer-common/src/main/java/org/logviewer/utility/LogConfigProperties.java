package org.logviewer.utility;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@SuppressWarnings("serial")
public class LogConfigProperties extends Properties {
    
    private static final String LOG_CONFIG_DEFAULT_PROPERTIES = "LogConfigDefault.properties";

    public LogConfigProperties() {
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
