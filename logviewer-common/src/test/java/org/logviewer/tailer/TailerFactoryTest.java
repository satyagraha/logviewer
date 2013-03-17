package org.logviewer.tailer;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.logviewer.services.LogConfig;
import org.logviewer.services.LogTailer;
import org.logviewer.services.TailerCallback;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@RunWith(MockitoJUnitRunner.class)
public class TailerFactoryTest {

    @Mock
    private Executor executor;
    
    @Mock
    private LogConfig logConfig;
    
    @Mock
    private TailerCallback tailerCallback;
    
    @InjectMocks
    private TailerFactory tailerFactory;
    
    @Test
    public void logLocalTailerReturnsExpected() throws IOException {
        // given
        when(logConfig.getExecutor()).thenReturn(executor);
        
        // when
        URI uri = new File("abc.def").toURI();
        LogTailer logTailer = tailerFactory.logLocalTailer(uri, tailerCallback);
        
        // then
        assertThat(logTailer, instanceOf(LogTailerCommons.class));
    }

    @Test
    public void logRemoteTailerReturnsExpected() throws Exception {
        // given
        when(logConfig.getExecutor()).thenReturn(executor);
        when(logConfig.getProperties()).thenReturn(new Properties());
        
        // when
        URI uri = new URI("ssh://user@hostname/path/to/file.log");
        String password = null;
        String passphrase = null;
        LogTailer logTailer = tailerFactory.logRemoteTailer(uri, password, passphrase, tailerCallback);
        
        // then
        assertThat(logTailer, instanceOf(LogTailerSsh.class));
    }
    
}
