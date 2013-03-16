package org.logviewer.tailer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.logviewer.services.TailerCallback;
import org.logviewer.utility.DaemonThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * Provides remote tailing facilities via ssh.
 */
public class TailerSsh {

    private static final Logger LOGGER = LoggerFactory.getLogger(TailerSsh.class);
    private static final String DEFAULT_TAIL_COMMAND = "tail -F";

    private final TailerCallback tailerListener;
    private final Executor executor;
    private final JSch jsch;
    private final CountDownLatch completed;

    private String tailCommand;
    private Session session;
    private String filePath;
    private volatile boolean stopping;
    private volatile Thread inputThread;
    private volatile Thread errorThread;

    /**
     * Constructor.
     * 
     * @param jsch
     * @param uri
     * @param listener
     * @param executor
     * @throws IOException
     */
    public TailerSsh(JSch jsch, URI uri, TailerCallback listener, Executor executor) throws IOException {
        this.jsch = jsch;
        this.tailerListener = listener;
        this.executor = executor;
        completed = new CountDownLatch(1);
        
        tailCommand = DEFAULT_TAIL_COMMAND;
        stopping = false;
        
        LOGGER.debug("uri: {}", uri);
        String user = uri.getUserInfo();
        LOGGER.debug("user: {}", user);
        String host = uri.getHost();
        LOGGER.debug("host: {}", host);
        String uriPath = uri.getPath();
        LOGGER.debug("uriPath: {}", uriPath);
        filePath = new File(uri.getPath()).getCanonicalPath();
        LOGGER.debug("filePath: {}", filePath);
        
        try {
            session = jsch.getSession(user, host);
        } catch (JSchException e) {
            throw new IOException(e);
        }
    }

    /**
     * Accessor for JSch instance.
     * 
     * @return
     */
    public JSch getJsch() {
        return jsch;
    }

    /**
     * Accessor for Session instance.
     * 
     * @return
     */
    public Session getSession() {
        return session;
    }
    
    /**
     * Set remote tail command.
     * 
     * @param tailCommand
     */
    public void setTailCommand(String tailCommand) {
        this.tailCommand = tailCommand;
    }

    /**
     * Commence tailing.
     */
    public void start() {
        LOGGER.debug("start");
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                inputThread = Thread.currentThread();
                remoteTailWrapped();
            }
        };
        executor.execute(runnable);
    }
    
    /**
     * Cease tailing.
     */
    public void stop() {
        LOGGER.debug("stop");
        stopping = true;
        if (errorThread != null) {
            errorThread.interrupt();
        }
        if (inputThread != null) {
            inputThread.interrupt();
        }
    }
    
    /**
     * Wait for tail thread to terminate.
     * 
     * @throws InterruptedException
     */
    public void await() throws InterruptedException {
        completed.await();
    }
    
    private void remoteTailWrapped() {
        LOGGER.debug("start remoteTailWrapped");
        try {
            remoteTail();
        } catch (Exception e) {
            LOGGER.debug("exception", e);
            if (!stopping) {
                tailerListener.handleException(e);
            }
        } finally {
            completed.countDown();
        }
        LOGGER.debug("end remoteTailWrapped");
    }

    private void remoteTail() throws Exception {
        session.connect();
        
        ChannelExec channel = (ChannelExec) session.openChannel("exec");

        String command = String.format("%s %s", tailCommand, filePath);
        LOGGER.debug("command: {}", command);
        channel.setCommand(command);

        channel.setInputStream(null);

        InputStream errStream = channel.getErrStream();
        InputStream inputStream = channel.getInputStream();

        channel.connect();
        LOGGER.debug("channel connected");
        
        drainErrorStream(errStream);
        drainInputStream(inputStream);

        channel.disconnect();
        session.disconnect();
    }

    private void drainInputStream(final InputStream inputStream) throws IOException {
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(inputStream));
        while (!stopping) {
            String line = inputReader.readLine();
            if (line == null) {
                break;
            }
            tailerListener.handleLine(line);
        }
        LOGGER.debug("input stream drained");
    }
    
    private void drainErrorStream(final InputStream errStream) {
        
        Runnable drainer = new Runnable() {

            @Override
            public void run() {
                errorThread = Thread.currentThread();
                BufferedReader errReader = new BufferedReader(new InputStreamReader(errStream));

                while (true) {
                    String line;
                    try {
                        line = errReader.readLine();
                    } catch (IOException e) {
                        LOGGER.debug("exception", e);
                        return;
                    }
                    if (line == null) {
                        break;
                    }
                    LOGGER.error("remote process error: {}", line);
                    tailerListener.handleLine(String.format("[%s]", line));
                }
                LOGGER.debug("error stream drained");
            }
            
        } ;
        
        executor.execute(drainer);
    }
    
    /**
     * Simple command-line tester.
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        LOGGER.info("main");
        ExecutorService executor = Executors.newFixedThreadPool(10, new DaemonThreadFactory()) ;
        
        String privateKeyFilePath = args[0];
        String passphrase = args[1];
        String tailCommand = args[2];
        String remoteUri = args[3];
        int delay = Integer.parseInt(args[4]);
        
        TailerCallback listener = new TailerCallback() {
            
            @Override
            public void handleLine(String line) {
                LOGGER.info("log line: {}", line);
            }
            
            @Override
            public void handleException(Exception ex) {
                LOGGER.error("Unexpected exception", ex);
            }
        };

        JSch jsch = new JSch();
        URI uri = new URI(remoteUri) ;
        TailerSsh tailer = new TailerSsh(jsch, uri, listener, executor);
        tailer.getJsch().addIdentity(privateKeyFilePath, passphrase);
        tailer.getSession().setConfig("StrictHostKeyChecking", "no");
        tailer.setTailCommand(tailCommand);

        tailer.start();
        Thread.sleep(delay);
        tailer.stop();
        tailer.await();
        LOGGER.debug("shutdown");
        executor.shutdown();
        LOGGER.debug("awaitTermination");
        executor.awaitTermination(1, TimeUnit.MINUTES);
        LOGGER.info("normal termination");
    }

}
