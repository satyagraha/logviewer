package org.logviewer.grizzly;

import java.io.IOException;

import org.glassfish.grizzly.websockets.DataFrame;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketAdapter;
import org.logviewer.core.LogManager;
import org.logviewer.services.LogConfig;
import org.logviewer.services.MessageSender;

public class LogViewerHandler extends WebSocketAdapter implements MessageSender {
    
    private WebSocket socket;
    private LogManager logManager;

    public LogViewerHandler(LogConfig logConfig, WebSocket socket) {
        this.socket = socket;
        logManager = new LogManager(logConfig, this);
        socket.add(this);
    }

    @Override
    public void onMessage(WebSocket socket, String messageText) {
        System.out.println("LVH onMessage: " + messageText);
        try {
            logManager.handleMessage(messageText);
        } catch (IOException e) {
            logManager.handleException(e);
        }
    }
    
    @Override
    public void onClose(WebSocket socket, DataFrame frame) {
        System.out.println("LVH onClose");
        socket.remove(this);
        logManager.close();
        socket = null;
        logManager = null;
    }
    
    @Override
    public void sendMessage(String messageString) throws IOException {
        if (socket != null) {
            socket.send(messageString);
        }
    }

}
