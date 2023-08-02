# HttpServer

Java HTTP Server from scratch (not using `java.net.http` or any external libraries)

To install, download the `HttpServer.jar` from releases and add it to your project

The source code includes an example app that I created in the `main` package and a commandline parser in the `commandparser` package.

The actual HTTP Server implementation is in `httpserver`.

A simple example app might look something like:
```java
package org.example;

import org.httpserver.HttpServer;
import org.httpserver.HttpMessage;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException {
        InetSocketAddress address = new InetSocketAddress("127.0.0.1",8081);
        HttpServer server = new HttpServer(address);
        server.initialize();

        server.onConnection((req)->{
            System.out.println("Got request:\n" + req);
            String body = "<h1>Welcome to my Http Server!</h1>";

            HttpMessage res = new HttpMessage();
            res.setStartLine(HttpMessage.HttpOK);
            res.putHeader("Content-Length", String.valueOf(body.length()));
            res.putHeader("Content-Type","text/html");
            res.setBody(body);
            return res;
        });

        server.run();
    }
}
```
