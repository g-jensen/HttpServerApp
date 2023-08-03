package org.httpserver;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class HttpServerTest {

    @Test
    void initializes() throws IOException {
        ByteArrayOutputStream o1 = new ByteArrayOutputStream();
        InetSocketAddress a1 = new InetSocketAddress("127.0.0.1",8081);
        HttpServer s1 = new HttpServer(a1);
        s1.setPrintStream(new PrintStream(o1));

        s1.initialize();
        assertTrue(s1.isBound());
        assertEquals(a1,s1.socketAddress());
        assertEquals(s1.listeningString()+"\n",o1.toString());
    }

    @Test
    void failsToInitializeOnUsedPort() throws IOException {
        ByteArrayOutputStream o1 = new ByteArrayOutputStream();
        InetSocketAddress a1 = new InetSocketAddress("127.0.0.1",1);
        HttpServer s1 = new HttpServer(a1);
        s1.setPrintStream(new PrintStream(o1));

        s1.initialize();
        assertFalse(s1.isBound());
        assertNotEquals(a1,s1.socketAddress());
        assertEquals("Permission denied (Bind failed)\n",o1.toString());
        s1.stop();
    }

    @Test
    void listensForRequest() throws IOException, InterruptedException {
        InetSocketAddress a1 = new InetSocketAddress("127.0.0.1",8082);
        HttpServer s1 = new HttpServer(a1);
        s1.setPrintStream(new PrintStream(new ByteOutputStream()));
        s1.initialize();

        byte[] hello = "hello".getBytes();
        byte[] bye = "bye".getBytes();
        byte[] b1 = new byte[5];
        byte[] b2 = new byte[3];
        Thread t1 = new Thread(()-> {
            try {
                s1.listen().getInputStream().read(b1);
                s1.listen().getInputStream().read(b2);
            } catch (IOException ignore) {}
        });
        t1.start();
        Socket socket1 = new Socket();
        socket1.connect(a1);
        socket1.getOutputStream().write(hello);

        Socket socket2 = new Socket();
        socket2.connect(a1);
        socket2.getOutputStream().write(bye);
        t1.join();

        assertArrayEquals(hello,b1);
        assertArrayEquals(bye,b2);
        s1.stop();
    }

    @Test
    void callsFunctionOnConnection() throws IOException, BadRequestException {
        InetSocketAddress a1 = new InetSocketAddress("127.0.0.1",8083);
        HttpServer s1 = new HttpServer(a1);
        s1.setPrintStream(new PrintStream(new ByteOutputStream()));
        s1.initialize();

        final int[] a = {1};
        s1.onConnection((m) -> {
            a[0] = 2;
            try {
                return new HttpMessage("HTTP/1.1 200 OK\r\n\r\n");
            } catch (BadRequestException e) {
                throw new RuntimeException(e);
            }
        });
        s1.run();

        Socket socket1 = new Socket();
        socket1.connect(a1);
        socket1.getOutputStream().write("GET / HTTP/1.1\r\nHost: me\r\n\r\n".getBytes());
        HttpMessage res = new HttpMessage(socket1.getInputStream());

        assertEquals(2,a[0]);
        s1.stop();
    }

    @Test
    void handlesConnectionsAsynchronously() throws IOException {
        InetSocketAddress a1 = new InetSocketAddress("127.0.0.1",8102);
        HttpServer s1 = new HttpServer(a1);
        s1.setPrintStream(new PrintStream(new ByteOutputStream()));
        s1.initialize();
        s1.onConnection((req)-> {
            try {Thread.sleep(1000);} catch (Exception ignored) {}
            return new HttpMessage();
        });
        s1.run();

        Instant start = Instant.now();
        for (int i = 0; i < 5; i++) {
            Socket socket1 = new Socket();
            socket1.connect(a1);
            socket1.getOutputStream().write("GET / HTTP/1.1\r\n\r\n".getBytes());
        }
        Instant end = Instant.now();

        assertTrue(Duration.between(start,end).getSeconds() < 5);
        s1.stop();
    }

    @Test
    void sendsReturnValueOfOnConnectionWhenHandlingConnection() throws IOException, BadRequestException {
        InetSocketAddress a1 = new InetSocketAddress("127.0.0.1", 8084);
        HttpServer s1 = new HttpServer(a1);
        s1.setPrintStream(new PrintStream(new ByteOutputStream()));
        s1.initialize();

        HttpMessage m1 = new HttpMessage("HTTP/1.1 200 OK\r\n\r\n");
        s1.onConnection((s) -> m1);
        s1.run();

        Socket socket1 = new Socket();
        socket1.connect(a1);
        socket1.getOutputStream().write("GET / HTTP/1.1\r\nHost: me\r\n\r\n".getBytes());

        HttpMessage m2 = new HttpMessage(socket1.getInputStream());
        assertEquals(m1.toString(), m2.toString());
        s1.stop();
    }

    @Test
    void handlesBadRequest() throws IOException, BadRequestException {
        InetSocketAddress a1 = new InetSocketAddress("127.0.0.1",8101);
        HttpServer s1 = new HttpServer(a1);
        s1.setPrintStream(new PrintStream(new ByteOutputStream()));
        s1.initialize();

        HttpMessage m1 = new HttpMessage("HTTP/1.1 400 Bad Request\r\n\r\n");
        s1.onConnection((s) -> m1);
        s1.run();

        Socket socket1 = new Socket();
        socket1.connect(a1);
        socket1.getOutputStream().write("GET / HTTP/1.1\r\n\r\n".getBytes());

        HttpMessage m2 = new HttpMessage(socket1.getInputStream());
        assertEquals(m1.toString(),m2.toString());
        s1.stop();
    }

    @Test
    void getsListeningString() throws IOException {
        InetSocketAddress a1 = new InetSocketAddress("127.0.0.1",80);
        HttpServer s1 = new HttpServer(a1);
        assertEquals(
                "Listening at localhost on port 80",
                s1.listeningString());

        InetSocketAddress a2 = new InetSocketAddress("192.225.81.49",8081);
        HttpServer s2 = new HttpServer(a2);
        assertEquals(
                "Listening at 192.225.81.49 on port 8081",
                s2.listeningString());
    }
}