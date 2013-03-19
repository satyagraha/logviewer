package org.logviewer.utility;

import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExecutorDefault extends ThreadPoolExecutor {

    public ExecutorDefault(Properties properties) {
        super(0, 100, 1, TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(100, true));
    }
    
}
