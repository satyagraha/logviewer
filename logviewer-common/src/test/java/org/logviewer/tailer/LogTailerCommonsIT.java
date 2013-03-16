package org.logviewer.tailer;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.PrintStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.junit.Test;
import org.logviewer.services.TailerCallback;
import org.logviewer.tailer.LogTailerCommons;
import org.logviewer.utility.DaemonThreadFactory;

public class LogTailerCommonsIT {
    
    private DaemonThreadFactory daemonThreadFactory = new DaemonThreadFactory();

    @Test
    public void shouldCallbackOnLineAdded() throws Exception {
        
        final List<String> lines = new CopyOnWriteArrayList<String>();
        final List<Exception> exceptions = new CopyOnWriteArrayList<Exception>();
        
        TailerCallback tailerCallback = new TailerCallback() {
            
            @Override
            public void handleLine(String line) {
                lines.add(line);
            }
            
            @Override
            public void handleException(Exception e) {
                exceptions.add(e);
            }
        };
        
        File tempFile = File.createTempFile("abc", "log");
        tempFile.deleteOnExit();
        URI uri = tempFile.toURI();
        
        Executor executor = getExecutor();
        
        LogTailerCommons logTailerCommons = new LogTailerCommons(tailerCallback, uri, executor);
        logTailerCommons.start();
        
        PrintStream printStream = new PrintStream(tempFile);
        
        printStream.println("hello");
        printStream.flush();

        Thread.sleep(2000);
        
        assertThat(lines, is(Arrays.asList("hello")));
        
        printStream.println("world");
        printStream.flush();
        
        Thread.sleep(2000);
        
        assertThat(lines, is(Arrays.asList("hello", "world")));
        
        assertThat(exceptions, empty());
        
        printStream.close();
        
        logTailerCommons.stop();
        
    }
    
    private Executor getExecutor() {
        return Executors.newFixedThreadPool(10, daemonThreadFactory);
    }

}
