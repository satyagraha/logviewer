package org.logviewer.core;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Executor;

import org.apache.commons.io.input.TailerListener;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.logviewer.core.LogMessage.Action;
import org.mockito.ArgumentCaptor;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LogManagerTest {
    
    LogSupport logSupport = mock(LogSupport.class);
    
    LogManager logManager = new LogManager(logSupport);
    
    private final ObjectMapper mapper = new ObjectMapper();
    
    @Test
    public void shouldHandleFilenameProcessing() throws IOException {
        // given
        String logFilenamesMessage = makeLogFilenamesMessage();
        File logDir = mock(File.class);
        given(logSupport.getLogDir()).willReturn(logDir);
        given(logSupport.getLogFilter()).willReturn("*.log");
        String logFilename = "abc.log";
        given(logDir.listFiles(any(FileFilter.class))).willReturn(new File[]{ new File(logFilename) });
        String logDirPath = "/the/dir";
        given(logDir.getAbsolutePath()).willReturn(logDirPath);
        
        // when
        logManager.handleMessage(logFilenamesMessage);
        
        // then
        ArgumentCaptor<String> responseString = ArgumentCaptor.forClass(String.class);
        verify(logSupport).sendMessage(responseString.capture());
        LogMessage sentMessage = mapper.readValue(responseString.getValue(), LogMessage.class);
        assertThat(sentMessage.action, is(Action.GOT_LOG_FILENAMES));
        assertThat(sentMessage.directory, is(logDirPath));
        assertThat(sentMessage.filenames, is(Arrays.asList(logFilename)));
    }

    @Test
    public void shouldHandleTailProcessing() throws IOException {
        // given
        String filename = "abc.log";
        String openLogMessage = makeOpenLogMessage(filename);
        File logDir = new File("/the/dir");
        given(logSupport.getLogDir()).willReturn(logDir );
        Executor executor = mock(Executor.class);
        given(logSupport.getExecutor()).willReturn(executor);

        // when
        logManager.handleMessage(openLogMessage);
        
        // then
        ArgumentCaptor<Runnable> runnable = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(runnable.capture());
        TailerListener listener = (TailerListener)(Object) runnable.getValue();
        
        // and when
        String newLine = "new line";
        listener.handle(newLine);
        
        // then
        ArgumentCaptor<String> responseString = ArgumentCaptor.forClass(String.class);
        verify(logSupport).sendMessage(responseString.capture());
        LogMessage sentMessage = mapper.readValue(responseString.getValue(), LogMessage.class);
        assertThat(sentMessage.action, is(Action.LOG_UPDATED));
        assertThat(sentMessage.content, is(Arrays.asList(newLine)));
    }
    
    private String makeLogFilenamesMessage() throws IOException {
        LogMessage message = new LogMessage();
        message.action = Action.GET_LOG_FILENAMES;
        return mapper.writeValueAsString(message);
    }

    private String makeOpenLogMessage(String filename) throws IOException {
        LogMessage message = new LogMessage();
        message.action = Action.OPEN_LOG;
        message.filenames = Arrays.asList(filename);
        return mapper.writeValueAsString(message);
    }
    
}
