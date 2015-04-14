# logviewer

## Overview

This project provides a capability to tail log files in a web browser. It uses the emerging 
[websockets](http://en.wikipedia.org/wiki/WebSocket) technology to stream new log lines to the browser for 
display in a scrollable text area. The log files tailed may be either on a filesystem locally mounted on 
the web application server, or on a remote server accessible via [ssh](http://en.wikipedia.org/wiki/Secure_Shell).

### Browser Compatibility

The client-side software is written in Javascript, and browser compatibility as at autumn 2012 is as follows:

- Firefox - excellent, tested
- Chrome - excellent, tested
- MS IE - limited
- Safari - believed to work, but not tested

A comprehensive table of browser websocket support is [here](http://caniuse.com/websockets).

### Server-side Support

The server-side implementation of websockets does not as yet have a standard Java API. Therefore
different web container providers require the use of container-specific classes to service websocket
actions.

The implementation provided here comes with:

- a common core log viewer service which is independent of the container
- a common [J2EE servlet](http://en.wikipedia.org/wiki/Java_Servlet) 2.5 abstraction module
- an [Apache Tomcat](http://tomcat.apache.org/) 7.0.30 servlet adapter using the two common modules
- a [codehaus Jetty](http://jetty.codehaus.org/jetty/) 7.6.7 servlet adapter using the two common modules
- a [Glassfish Grizzly](http://grizzly.java.net/) implementation using the common core service only

A separate [Play framework](http://www.playframework.com/) project using the common core is available [here]().

There is no reason implementations could not be provided for other containers without too much difficulty.
  
## Getting Started
 
- Ensure you have [Git](http://git-scm.com/) and [Maven](http://maven.apache.org/) installed on your system
- Copy the URL in the _Git Read-Only_ entry field at the top of this web page to the clipboard
- Change working directory to an appropriate location for the checkout, then execute: `git clone url`
- Change working directory to the newly created _logviewer_ subdirectory
- Edit the file `logviewer-common/src/main/resources/LogConfigDefault.properties` to adjust log
directory if required
- Ensure you have environment variable `JAVA_HOME` set to reference a Java 6 JDK (not JRE), e.g. on Windows:
 - `set JAVA_HOME=C:\Program Files\Java\jdk1.6.0_35`
- Build the complete system thus:
 - `mvn.bat clean install`
- For Jetty, execute:
  - `cd logviewer-webapp`
  - `mvn.bat -P jetty clean jetty:run`
- For Tomcat, execute:
 - `cd logviewer-webapp`
 - `mvn.bat -P tomcat clean tomcat7:run`
- For Grizzly, execute:
 - `cd logviewer-grizzly`
 - `mvn.bat exec:java` 
- Open web URL [http://localhost:8080/logviewer/display.html](http://localhost:8080/logviewer/display.html)
- The resulting web page should be visible in the usual way

### Tailing Server-local Files

Click the _pick log File_ pulldown to select a file to be tailed. 

### Tailing Server-remote Files

- Enter a URI in the entry field, typically in the form: `ssh://userid@hostname/path/to/file`
- Note that URI's need to be URL-encoded for embedded spaces or other special characters
- If using password authentication, add a password in the appropriate entry field
- If using passphrase authentication, add a passphrase in the appropriate entry field
- Click the _Tail_ button
- Defaults for various ssh parameters may be set via the properties file mentioned above

### IDE Users

Note that, at the time of writing, the standard Eclipse Maven plugin _m2e_ has an issue whereby
source code cannot be found when debugging with the above Jetty/Tomcat7 run configuratations: this
is due to dynamic code loading, see the [bug report](https://bugs.eclipse.org/bugs/show_bug.cgi?id=384065).
As an interim fix, an additional _m2e_ extension plugin may be installed from
[here](https://github.com/ifedorenko/com.ifedorenko.m2e.sourcelookup) which provides the currently
missing functionality. 
 
### Javadoc

Execution of the command:

- `mvn.bat javadoc:javadoc`

at base directory level will result in Javadoc being created under `javadoc/site/apidocs`. 
 
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

## Log Generator

A utility program named `LogGenerator` is provided: it writes to a log file on a regular basis to simulate what
would happen with a real log file, and is useful during testing. The log file location is configured in
`logviewer-common/src/main/resources/generator.log4j.properties`.

To run the generator, from the main working directory do the following:

 - `set JAVA_HOME=C:\Program Files\Java\jdk1.6.0_35`
 - `cd logviewer-common`
 - `mvn.bat -P generator exec:java -Dexec.args=100`

which generates 100 lines of output then exits. Omit the line limit number to make the program run indefinitely.

## Notes

- Developer contributions welcome
- Credit is due to JCraft Inc, developers of the [Jsch Java SSH library](http://www.jcraft.com/jsch/)

## License

[Apache V2.0](http://www.apache.org/licenses/)

## Revision History

- 0.0.5 - Support for Jetty 9.2.x
- 0.0.4 - Refactored and added Grizzly support
- 0.0.3 - Added ssh support
- 0.0.2 - Split into separate maven modules
- 0.0.1 - Initial version
  