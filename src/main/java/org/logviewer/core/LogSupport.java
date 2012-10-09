package org.logviewer.core;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;

public interface LogSupport {

    File getLogDir();

    String getLogFilter();
    
    void sendMessage(String messageString) throws IOException;

    Executor getExecutor();
}
