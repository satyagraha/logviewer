package org.logviewer.servlet.tomcat;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.concurrent.Executor;

import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.apache.catalina.websocket.WsOutbound;
import org.logviewer.core.LogManager;
import org.logviewer.core.LogSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides servlet for use in Tomcat 7.
 */
public class LogViewerServlet extends WebSocketServlet {

    private static final long serialVersionUID = -1419426879051383553L;

    private static final Logger LOGGER = LoggerFactory.getLogger(LogViewerServlet.class);

    private class LogMessageWebSocket extends MessageInbound implements LogSupport {

        private final LogManager logManager = new LogManager(this);
        
        @Override
        protected void onOpen(WsOutbound outbound) {
            LOGGER.debug("onOpen");
        }

        @Override
        protected void onClose(int status) {
            LOGGER.debug("onClose");
        }

        @Override
        protected void onBinaryMessage(ByteBuffer message) throws IOException {
            throw new UnsupportedOperationException("Binary message not supported.");
        }

        @Override
        protected void onTextMessage(CharBuffer messageBuffer) throws IOException {
            String messageString = messageBuffer.toString();
            LOGGER.debug("onTextMessage: {}", messageString);
            logManager.handleMessage(messageString);
        }

        ///////////////////////////////////////////////////////////////////////
        
        @Override
        public java.io.File getLogDir() {
            String logDir = getServletConfig().getInitParameter("logDir");
            LOGGER.debug("logDir: {}", logDir);
            return new File(logDir);
        };
        
        @Override
        public String getLogFilter() {
            String logFilter = getServletConfig().getInitParameter("logFilter");
            LOGGER.debug("logFilter: {}", logFilter);
            return logFilter;
        }
        
        @Override
        public void sendMessage(String messageString) throws IOException {
            LOGGER.debug("sending message: {}", messageString);
            CharBuffer messageBuffer = CharBuffer.wrap(messageString);
            getWsOutbound().writeTextMessage(messageBuffer);
            
        }

        @Override
        public Executor getExecutor() {
            return (Executor) getServletContext().getAttribute("executor");
        }
    };

    @Override
    protected StreamInbound createWebSocketInbound(String subProtocol, HttpServletRequest request) {
        LOGGER.debug("creating websocket: " + subProtocol);
        return new LogMessageWebSocket();
    }

    
}
