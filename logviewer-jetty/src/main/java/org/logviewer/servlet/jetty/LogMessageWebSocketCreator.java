package org.logviewer.servlet.jetty;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.logviewer.services.LogConfig;

/**
 * Created by arul on 4/6/15.
 */
public class LogMessageWebSocketCreator implements WebSocketCreator {

    private LogMessageWebSocket logMessageWebSocket;

    public LogMessageWebSocketCreator(LogConfig logConfig) {
        this.logMessageWebSocket = new LogMessageWebSocket(logConfig);
    }

    @Override
    public Object createWebSocket(ServletUpgradeRequest servletUpgradeRequest, ServletUpgradeResponse servletUpgradeResponse) {
        return logMessageWebSocket;
    }
}
