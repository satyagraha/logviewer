package org.logviewer.selenium;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.openqa.selenium.By.id;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class LogViewerServerST {

    private static WebDriver driver;
    
    @BeforeClass
    public static void setupClass() {
        driver = new FirefoxDriver();
        driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
    }
    
    @AfterClass
    public static void teardownClass() {
        driver.close();
    }
    
    @Before
    public void setup() throws IOException {
    }

    @After
    public void shutdown() {
    }

    @Test
    public void dirFieldIsPopulated() throws Exception {
        pause(2);
        
        driver.get("http://localhost:8080/logviewer/display.html");

        String title = driver.getTitle();
        assertThat(title, is("Log File Viewer"));
        
        pause(2);
        String dirField = driver.findElement(id("dir_field")).getAttribute("value");
        assertThat(dirField, not(isEmptyString()));
        
        pause(2);
    }
    
    private void pause(int secs) {
        try {
            Thread.sleep(secs * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
