package org.logviewer.grizzly;

import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.logviewer.services.LogConfig;

public class LogViewerApplication extends WebSocketApplication {

    private final LogConfig logConfig;

    public LogViewerApplication(LogConfig logConfig) {
        this.logConfig = logConfig;
    }
    
    @Override
    public void onConnect(WebSocket socket) {
        System.out.println("LVA onConnect");
        new LogViewerHandler(logConfig, socket);
    }

    @Override
    public boolean isApplicationRequest(HttpRequestPacket request) {
        System.out.println("request: " + request);
        return request.getRequestURI().toString().endsWith("/logviewer/websocket/support");
    }

}