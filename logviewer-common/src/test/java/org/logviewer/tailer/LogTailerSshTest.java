package org.logviewer.tailer;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.logviewer.services.TailerCallback;
import org.mockito.runners.MockitoJUnitRunner;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@RunWith(MockitoJUnitRunner.class)
public class LogTailerSshTest {

    private Properties properties = mock(Properties.class); 

    @Test
    public void shouldInitialiseJsch() throws IOException, URISyntaxException, JSchException {
        
        // given
        File pkFile = new File(LogTailerSshTest.class.getClassLoader().getResource("test_private_key.ossh").toURI());
        String pkFilePath = pkFile.getCanonicalPath();
        given(properties.getProperty(LogTailerSsh.LOG_TAILER_SSH_PRIVATE_KEY_FILE_PATH_KEY)).willReturn(pkFilePath);
        String passphrase = "ok!";
        given(properties.getProperty(LogTailerSsh.LOG_TAILER_SSH_PASSPHRASE_KEY)).willReturn(passphrase);

        TailerCallback tailerCallback = mock(TailerCallback.class);
        URI uri = new URI("ssh://userid@hostname/path/to/abc.log");
        Executor executor = mock(Executor.class);;
        TailerSsh tailer = mock(TailerSsh.class);
        JSch jsch = mock(JSch.class);
        Session session = mock(Session.class);

        given(tailer.getJsch()).willReturn(jsch);
        given(tailer.getSession()).willReturn(session);
        
        // when
        LogTailerSsh logTailerSsh = new LogTailerSsh(tailerCallback, uri, executor, properties, tailer);
        logTailerSsh.start();

        // then
        verify(jsch).addIdentity(pkFilePath, passphrase);
        doNothing().when(executor).execute(any(Runnable.class));
    }

}
