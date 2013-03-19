package org.logviewer.grizzly;

import java.io.IOException;

import org.glassfish.grizzly.websockets.DefaultWebSocket;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.WebSocketListener;

public class LogViewerWebSocket extends DefaultWebSocket {

    public LogViewerWebSocket(ProtocolHandler protocolHandler, WebSocketListener... listeners) {
        super(protocolHandler, listeners);
    }

    public void sendMessage(String messageText) throws IOException {
        send(messageText);
    }
    
}



