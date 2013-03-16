package org.logviewer.core;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.logviewer.core.LogMessage.Action;
import org.logviewer.services.LogConfig;
import org.logviewer.services.MessageSender;
import org.logviewer.tailer.LogTailerAbstract;
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
    public void shouldHandleGetLogFilenamesMessage() throws IOException {
        // given
        String getLogFilenamesMessage = makeGetLogFilenamesMessage();

        String tempDirPath = System.getProperty("java.io.tmpdir");
        given(logProperties.getProperty(LogManager.LOG_MANAGER_LOG_DIR_KEY)).willReturn(tempDirPath);
        given(logConfig.getProperties()).willReturn(logProperties);

        given(logProperties.getProperty(LogManager.LOG_MANAGER_LOG_FILTER_KEY)).willReturn("*.log");
        String logFileName = System.currentTimeMillis() + ".log";
        File logFile = new File(tempDirPath, logFileName);
        boolean createdLogFile = logFile.createNewFile();
        assertTrue(createdLogFile);

        // when
        logManager.handleMessage(getLogFilenamesMessage);

        // then
        ArgumentCaptor<String> responseString = ArgumentCaptor.forClass(String.class);
        verify(messageSender).sendMessage(responseString.capture());
        LogMessage sentMessage = mapper.readValue(responseString.getValue(), LogMessage.class);
        assertThat(sentMessage.action, is(Action.GOT_LOG_FILENAMES));
        assertThat(sentMessage.filenames, hasItem(logFileName));

        // clean up
        logFile.delete();
    }

    @Test
    public void shouldHandleOpenLogLocalMessage() throws IOException {
        // given
        String filename = "abc.log";
        String openLogLocalMessage = makeOpenLogLocalMessage(filename);

        given(logProperties.getProperty(LogManager.LOG_MANAGER_LOG_DIR_KEY)).willReturn("/dir");
        given(logConfig.getProperties()).willReturn(logProperties);

        Executor executor = mock(Executor.class);
        given(logConfig.getExecutor()).willReturn(executor);

        LogTailerAbstract tailer = mock(LogTailerAbstract.class);
        given(tailerFactory.logLocalTailer(any(URI.class), eq(logManager))).willReturn(tailer);

        // when
        logManager.handleMessage(openLogLocalMessage);

        // then
        verify(tailer).start();

        // and when
        String messageLine = Long.toString(System.currentTimeMillis());
        logManager.handleLine(messageLine);

        // then
        ArgumentCaptor<String> responseString = ArgumentCaptor.forClass(String.class);
        verify(messageSender).sendMessage(responseString.capture());
        LogMessage sentMessage = mapper.readValue(responseString.getValue(), LogMessage.class);
        assertThat(sentMessage.action, is(Action.LOG_UPDATED));
        assertThat(sentMessage.content, is(Arrays.asList(messageLine)));

        // and given
        String closeLogMessage = makeCloseLogMessage();

        // when
        logManager.handleMessage(closeLogMessage);

        // and when
        verify(tailer).stop();
    }

    @Test
    public void shouldHandleOpenLogRemoteMessage() throws IOException {
        // given
        String uriString = "ssh://hostname.com/path/filename.log";
        String password = null;
        String passphrase = "let me in";
        String openLogRemoteMessage = makeOpenLogRemoteMessage(uriString, password, passphrase);
        
        LogTailerAbstract tailer = mock(LogTailerAbstract.class);
        given(tailerFactory.logRemoteTailer(any(URI.class), eq(password), eq(passphrase), eq(logManager))).willReturn(tailer);
        
        // when
        logManager.handleMessage(openLogRemoteMessage);
        
        // then
        verify(tailer).start();
        
        // and when
        String messageLine = Long.toString(System.currentTimeMillis());
        logManager.handleLine(messageLine);
        
        // then
        ArgumentCaptor<String> responseString = ArgumentCaptor.forClass(String.class);
        verify(messageSender).sendMessage(responseString.capture());
        LogMessage sentMessage = mapper.readValue(responseString.getValue(), LogMessage.class);
        assertThat(sentMessage.action, is(Action.LOG_UPDATED));
        assertThat(sentMessage.content, is(Arrays.asList(messageLine)));
        
        // and given
        String closeLogMessage = makeCloseLogMessage();
        
        // when
        logManager.handleMessage(closeLogMessage);
        
        // and when
        verify(tailer).stop();
    }
    
    @Test
    public void shouldHandlePingServerMessage() throws IOException {
        // given
        String pingServerMessage = makePingServerMessage();

        // when
        logManager.handleMessage(pingServerMessage);

        // then
        ArgumentCaptor<String> responseString = ArgumentCaptor.forClass(String.class);
        verify(messageSender).sendMessage(responseString.capture());
        LogMessage sentMessage = mapper.readValue(responseString.getValue(), LogMessage.class);
        assertThat(sentMessage.action, is(Action.PONG_CLIENT));
    }

    private String makeGetLogFilenamesMessage() throws IOException {
        LogMessage message = new LogMessage();
        message.action = Action.GET_LOG_FILENAMES;
        return mapper.writeValueAsString(message);
    }

    private String makeOpenLogLocalMessage(String filename) throws IOException {
        LogMessage message = new LogMessage();
        message.action = Action.OPEN_LOG_LOCAL;
        message.filenames = Arrays.asList(filename);
        return mapper.writeValueAsString(message);
    }

    private String makeOpenLogRemoteMessage(String uriString, String password, String passphrase) throws IOException {
        LogMessage message = new LogMessage();
        message.action = Action.OPEN_LOG_REMOTE;
        message.filenames = Arrays.asList(uriString);
        message.password = password;
        message.passphrase = passphrase;
        return mapper.writeValueAsString(message);
    }
    
    private String makePingServerMessage() throws IOException {
        LogMessage message = new LogMessage();
        message.action = Action.PING_SERVER;
        return mapper.writeValueAsString(message);
    }

    private String makeCloseLogMessage() throws IOException {
        LogMessage message = new LogMessage();
        message.action = Action.CLOSE_LOG;
        return mapper.writeValueAsString(message);
    }
    
}
