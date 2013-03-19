package org.logviewer.grizzly;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

public class LogViewerServerTest {

    private HttpServer server;

    @Before
    public void setup() throws IOException {
        server = new LogViewerHttpServer();
        server.start();
    }

    @After
    public void shutdown() {
        server.stop();
        server = null;
    }

    @Test
    public void test() {
        WebDriver driver = new HtmlUnitDriver(true);

        driver.get("http://localhost:8080/logviewer/display.html");

        String title = driver.getTitle();
        assertThat(title, is("Log File Viewer"));
    }

}
