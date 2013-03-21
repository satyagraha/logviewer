package org.logviewer.grizzly;

import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.logviewer.services.LogConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogViewerApplication extends WebSocketApplication {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(LogViewerApplication.class);

    private final LogConfig logConfig;

    public LogViewerApplication(LogConfig logConfig) {
        this.logConfig = logConfig;
    }
    
    @Override
    public void onConnect(WebSocket socket) {
        LOGGER.debug("onConnect: {}", socket);
        new LogViewerHandler(logConfig, socket);
    }

    @Override
    public boolean isApplicationRequest(HttpRequestPacket request) {
        LOGGER.debug("isApplicationRequest: {}", request);
        return request.getRequestURI().toString().endsWith("/logviewer/websocket/support");
    }

}