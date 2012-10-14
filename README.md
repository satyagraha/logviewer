# logviewer

## Overview

This project provides a capability to tail log files in a web browser. It uses the emerging 
[websockets](http://en.wikipedia.org/wiki/WebSocket) technology to stream new log lines to the browser for 
display in a scrollable text area.

### Browser Compatibility

The client-side software is written in Javascript, and browser compatibility as at autumn 2012 is as follows:

- Firefox - excellent, tested
- Chrome - excellent, tested
- MS IE - limited
- Safari - believed to work, but not tested

A comprehensive table of browser websocket support is [here](http://caniuse.com/websockets).

### Server-side Support

The server-side implementation of websockets does not as yet have a standard Java servlet API. Therefore
different web container providers require the use of container-specific classes to service websocket
actions.

The implementation provided here comes with:

- a common core which is independent of the container
- a Tomcat 7.0.30 servlet adapter using the common core
- a Jetty 7.6.7 servlet adapter using the common core

There is no reason these could not be further extended to other J2EE containers without too much difficulty.
  
## Getting Started
 
- Ensure you have [Git](http://git-scm.com/) and [Maven](http://maven.apache.org/) installed on your system
- Copy the URL in the _Git Read-Only_ entry field at the top of this web page to the clipboard
- Change working directory to an appropriate location for the checkout, then execute: `git clone url`
- Change working directory to the newly created _logviewer_ subdirectory
- Ensure you have environment variable `JAVA_HOME` set to reference a Java 6 JDK (not JRE), e.g. on Windows:
 - `set JAVA_HOME=C:\Program Files\Java\jdk1.6.0_35`
- Build the complete system thus:
 - `mvn.bat clean install`
- Change to the webapp module directory thus:
 - `cd logviewer-webapp` 
- For Jetty, execute:
  - `mvn.bat -P jetty clean jetty:run`
- For Tomcat, execute:
 - `mvn.bat -P tomcat clean tomcat7:run`
- Open web URL [http://localhost:8080/logviewer/display.html](http://localhost:8080/logviewer/display.html)
- The resulting web page should be visible in the usual way
- You may configure the base log directory and the log wild-card filename matching via the file
`/src/main/webapp/WEB-INF/web.xml` by changing the two `init-params` there and re-running the Maven actions.

### IDE Users

TBD.

## Principles of Operation
 
Communication between client- and server-side is very simple, using one JSON-encoded object, declared as a `LogMessage`
in the Java codebase. On loading the web page the user is presented with a list of available log files, and
when one is selected then updates are sent to refresh the web page text area. The updating is continuous, unless
paused via the provided checkbox.
  
The following additional Javascript libraries are used:

- [JQuery](http://jquery.com/) - comprehensive DOM manipulation capabilities
- [jquery-json](http://code.google.com/p/jquery-json/) - JSON encoding and decoding  
- [Underscore](http://documentcloud.github.com/underscore/) - functional programming

The server-side component implements the appropriate container servlet and responds to the messages as necessary. We use the
Apache [CommonsIO Tailer](http://commons.apache.org/io/api-release/index.html?org/apache/commons/io/input/Tailer.html)
to perform the tracking of the log file.

A simple implementation of the standard Java [Executor](http://docs.oracle.com/javase/6/docs/api/index.html?java/util/concurrent/Executor.html)
interface is provided to manage the necessary thread pool.

A utility main program class named `LogGenerator` is provided, it writes to a log file on a regular basis to simulate what would
happen with a real log file, and is useful during testing.

## Notes

- Developer contributions welcome.

## Revision History

- 0.0.1 - Initial version
- 0.0.2 - Split into separate maven modules
  