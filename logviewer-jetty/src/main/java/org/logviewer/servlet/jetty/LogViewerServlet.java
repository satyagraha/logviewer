package org.logviewer.servlet.jetty;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;
import org.logviewer.core.LogManager;
import org.logviewer.services.LogConfig;
import org.logviewer.services.MessageSender;
import org.logviewer.servlet.LogConfigDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides servlet for use in Jetty 7.
 */
public class LogViewerServlet extends WebSocketServlet {

    private static final long serialVersionUID = -1419426879051383553L;

    private static final Logger LOGGER = LoggerFactory.getLogger(LogViewerServlet.class);
    
    private final LogConfig logConfig = new LogConfigDefault(this);

    private class LogMessageWebSocket implements WebSocket, WebSocket.OnTextMessage, MessageSender {

        private final LogManager logManager = new LogManager(logConfig, this);
        private WebSocket.Connection connection;
        
        @Override
        public void onOpen(WebSocket.Connection connection) {
            LOGGER.debug("onOpen");
            this.connection = connection;
        }

        @Override
        public void onClose(int closeCode, String message) {
            LOGGER.debug("onClose");
        }

        ///////////////////////////////////////////////////////////////////////
        
        @Override
        public void onMessage(String messageString) {
            LOGGER.debug("onTextMessage: {}", messageString);
            try {
                logManager.handleMessage(messageString);
            } catch (IOException e) {
                LOGGER.debug("exception", e);
            }
        }

        ///////////////////////////////////////////////////////////////////////
        
        @Override
        public void sendMessage(String messageString) throws IOException {
            LOGGER.debug("sending message: {}", messageString);
            connection.sendMessage(messageString);
        }

    };

    @Override
    public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
        LOGGER.debug("creating websocket: " + protocol);
        return new LogMessageWebSocket();
    }
}
