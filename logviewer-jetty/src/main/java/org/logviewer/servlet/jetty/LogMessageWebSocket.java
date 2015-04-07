package org.logviewer.servlet.jetty;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.logviewer.core.LogManager;
import org.logviewer.services.LogConfig;
import org.logviewer.services.MessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by arul on 4/6/15.
 */
public class LogMessageWebSocket implements WebSocketListener, MessageSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogViewerServlet.class);

    private final LogManager logManager;

    public LogMessageWebSocket(LogConfig logConfig) {
        logManager = new LogManager(logConfig, this);
    }

    private Session outbound;

    @Override
    public void onWebSocketConnect(Session outbound) {
        LOGGER.debug("onOpen");
        this.outbound = outbound;
    }

    @Override
    public void onWebSocketClose(int closeCode, String message) {
        LOGGER.debug("onClose");
    }

    @Override
    public void onWebSocketBinary(byte[] bytes, int i, int i2) {

    }

    @Override
    public void onWebSocketError(Throwable cause) {
        cause.printStackTrace(System.err);
    }

    ///////////////////////////////////////////////////////////////////////


    @Override
    public void onWebSocketText(String messageString) {
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
        outbound.getRemote().sendString(messageString);
    }

}

