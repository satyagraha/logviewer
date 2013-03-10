package org.logviewer.core;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.apache.commons.io.input.TailerListener;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.logviewer.core.LogMessage.Action;
import org.logviewer.tailer.LogTailerAbstract;
import org.logviewer.tailer.TailerCallback;
import org.mockito.ArgumentCaptor;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LogManagerTest {

    Properties logProperties = mock(Properties.class); 
    LogConfig logConfig = mock(LogConfig.class);
    MessageSender messageSender = mock(MessageSender.class);
    TailerFactory tailerFactory = mock(TailerFactory.class);
    
    LogManager logManager = new LogManager(logConfig, messageSender, tailerFactory);
    
    private final ObjectMapper mapper = new ObjectMapper();
    
    @Test
    public void shouldHandleLogLocalMessage() throws IOException {
        // given
        String filename = "abc.log";
        String openLogLocalMessage = makeOpenLogLocalMessage(filename);
        
        given(logProperties.getProperty(LogManager.LOG_MANAGER_LOG_DIR_KEY)).willReturn("/dir");
        given(logConfig.getProperties()).willReturn(logProperties);
        
        Executor executor = mock(Executor.class);
        given(logConfig.getExecutor()).willReturn(executor);

        LogTailerAbstract tailer = mock(LogTailerAbstract.class);
        given(tailerFactory.logLocalTailer(any(URI.class), any(TailerCallback.class))).willReturn(tailer);
        
        // when
        logManager.handleMessage(openLogLocalMessage);
        
        // then
        verify(tailerFactory).logLocalTailer(any(URI.class), any(TailerCallback.class));
        verify(tailer).start();
        
//        // then
//        ArgumentCaptor<Runnable> runnable = ArgumentCaptor.forClass(Runnable.class);
//        verify(executor).execute(runnable.capture());
//        TailerListener listener = (TailerListener)(Object) runnable.getValue();
//        
//        // and when
//        String newLine = "new line";
//        listener.handle(newLine);
//        
//        // then
//        ArgumentCaptor<String> responseString = ArgumentCaptor.forClass(String.class);
//        verify(messageSender).sendMessage(responseString.capture());
//        LogMessage sentMessage = mapper.readValue(responseString.getValue(), LogMessage.class);
//        assertThat(sentMessage.action, is(Action.LOG_UPDATED));
//        assertThat(sentMessage.content, is(Arrays.asList(newLine)));
    }
    
//    private String makeLogFilenamesMessage() throws IOException {
//        LogMessage message = new LogMessage();
//        message.action = Action.GET_LOG_FILENAMES;
//        return mapper.writeValueAsString(message);
//    }

    private String makeOpenLogLocalMessage(String filename) throws IOException {
        LogMessage message = new LogMessage();
        message.action = Action.OPEN_LOG_LOCAL;
        message.filenames = Arrays.asList(filename);
        return mapper.writeValueAsString(message);
    }
    
//    private String makeOpenLogRemoteMessage(String filename, String password, String passphrase) throws IOException {
//        LogMessage message = new LogMessage();
//        message.action = Action.OPEN_LOG_REMOTE;
//        message.filenames = Arrays.asList(filename);
//        message.password = password;
//        message.passphrase = passphrase;
//        return mapper.writeValueAsString(message);
//    }
    
}
