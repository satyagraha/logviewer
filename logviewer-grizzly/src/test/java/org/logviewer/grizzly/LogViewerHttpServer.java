package org.logviewer.grizzly;

import java.io.IOException;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.glassfish.grizzly.websockets.WebSocketEngine;
import org.logviewer.services.LogConfig;
import org.logviewer.utility.LogConfigDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogViewerHttpServer extends HttpServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogViewerHttpServer.class);
    
    public LogViewerHttpServer() {
        super();

        LOGGER.info("commencing execution");
        
        LogConfig logConfig = new LogConfigDefault();
        
        NetworkListener networkListener = new NetworkListener("sample-listener", "localhost", 8080);
        addListener(networkListener);
                
        String contentPath = "../logviewer-webapp/src/main/webapp";
        getServerConfiguration().addHttpHandler(new StaticHttpHandler(contentPath ), "/logviewer");
        
        final WebSocketAddOn addon = new WebSocketAddOn();
        for (NetworkListener listener : getListeners()) {
            listener.registerAddOn(addon);
        }
        
        LogViewerApplication logViewerApplication = new LogViewerApplication(logConfig);
        WebSocketEngine.getEngine().register(logViewerApplication);
    }
    
    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        LogViewerHttpServer httpServer = new LogViewerHttpServer();
     
        httpServer.start();
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
        }
        httpServer.stop();
    }

}
