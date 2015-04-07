package org.logviewer.servlet.jetty;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.logviewer.services.LogConfig;
import org.logviewer.servlet.LogConfigServlet;

/**
 * Provides servlet for use in Jetty 9.2.x.
 */
public class LogViewerServlet extends WebSocketServlet {

    private static final long serialVersionUID = -1419426879051383553L;

    private final LogConfig logConfig = new LogConfigServlet(this);

    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.setCreator(new LogMessageWebSocketCreator(logConfig));
    }
}
