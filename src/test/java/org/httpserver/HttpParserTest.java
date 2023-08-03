package org.httpserver;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class HttpParserTest {

    @Test
    void parsesMessageWithASingleHeader() throws BadRequestException {
        HttpMessage m = new HttpMessage();
        HttpParser p = new HttpParser(m);

        String s1 = "GET /hello HTTP/1.1\r\n" +
                "Host: Goodbye\r\n" +
                "\r\n";
        InputStream in1 = new ByteArrayInputStream(s1.getBytes());
        p.parseFromStream(in1);
        assertEquals(1,m.getHeaderFields().size());
        assertEquals("GET /hello HTTP/1.1",m.getStartLine());
        assertEquals("Goodbye",m.getHeaderFields().get("Host"));
    }

    @Test
    void parsesMessageWIthMultipleHeaders() throws BadRequestException {
        HttpMessage m = new HttpMessage();
        HttpParser p = new HttpParser(m);

        String s1 = "GET /hello HTTP/1.1\r\n" +
                "Host: Goodbye\r\n" +
                "Greetings: Java\r\n" +
                "Greg:Hello\r\n" +
                "\r\n";
        InputStream in1 = new ByteArrayInputStream(s1.getBytes());
        p.parseFromStream(in1);
        assertEquals(3,m.getHeaderFields().size());
        assertEquals("GET /hello HTTP/1.1",m.getStartLine());
        assertEquals("Goodbye",m.getHeaderFields().get("Host"));
        assertEquals("Java",m.getHeaderFields().get("Greetings"));
        assertEquals("Hello",m.getHeaderFields().get("Greg"));
    }

    @Test
    void parsesMessageWithBody() throws BadRequestException {
        HttpMessage m = new HttpMessage();
        HttpParser p = new HttpParser(m);

        String s1 = "GET /hello HTTP/1.1\r\n" +
                "Host: Goodbye\r\n" +
                "Content-Length: 5\r\n" +
                "\r\n" +
                "hello";
        InputStream in1 = new ByteArrayInputStream(s1.getBytes());
        p.parseFromStream(in1);
        assertEquals("hello",new String(m.getBody()));
    }

    @Test
    void parsesBodyWithCRLF() throws BadRequestException {
        HttpMessage m = new HttpMessage();
        HttpParser p = new HttpParser(m);

        String s1 = "GET /hello HTTP/1.1\r\n" +
                "Host: Goodbye\r\n" +
                "Content-Length: 10\r\n" +
                "\r\n" +
                "hello\r\nbye";
        InputStream in1 = new ByteArrayInputStream(s1.getBytes());
        p.parseFromStream(in1);
        assertEquals("hello\r\nbye",new String(m.getBody()));
    }

    @Test
    void determinesIfMessageIsRequest() throws BadRequestException {
        HttpMessage m1 = new HttpMessage();
        HttpParser p1 = new HttpParser(m1);

        String s1 = "GET /hello HTTP/1.1\r\nHost: Goodbye\r\n\r\n";
        InputStream in1 = new ByteArrayInputStream(s1.getBytes());
        p1.parseFromStream(in1);
        assertTrue(m1.isRequest);


        HttpMessage m2 = new HttpMessage();
        HttpParser p2 = new HttpParser(m2);

        String s2 = "HTTP/1.1 200 OK\r\n\r\n";
        InputStream in2 = new ByteArrayInputStream(s2.getBytes());
        p2.parseFromStream(in2);
        assertFalse(m2.isRequest);
    }

    @Test
    void parsesURI() throws BadRequestException {
        HttpMessage m = new HttpMessage();
        HttpParser p = new HttpParser(m);

        String s1 = "GET /hello HTTP/1.1\r\n" +
                "Host: Goodbye\r\n" +
                "\r\n";
        InputStream in1 = new ByteArrayInputStream(s1.getBytes());
        p.parseFromStream(in1);
        assertEquals("hello",m.getURI());
    }

    @Test
    void parsesMethod() throws BadRequestException {
        HttpMessage m = new HttpMessage();
        HttpParser p = new HttpParser(m);

        String s1 = "GET /hello HTTP/1.1\r\n" +
                "Host: Goodbye\r\n" +
                "\r\n";
        InputStream in1 = new ByteArrayInputStream(s1.getBytes());
        p.parseFromStream(in1);
        assertEquals("GET",m.getMethod());
    }

    @Test
    void throwsBadRequestIfInvalidContentLength() {
        HttpMessage m = new HttpMessage();
        HttpParser p = new HttpParser(m);

        String s1 = "GET /hello HTTP/1.1\r\n" +
                "Host: me\r\n" +
                "Content-Length: g\r\n" +
                "\r\n" +
                "hello";
        InputStream in1 = new ByteArrayInputStream(s1.getBytes());
        assertThrows(BadRequestException.class,()->p.parseFromStream(in1));
    }

    @Test
    void throwsBadRequestIfWhitespaceBeforeColon() {
        HttpMessage m = new HttpMessage();
        HttpParser p = new HttpParser(m);

        String s1 = "GET /hello HTTP/1.1\r\n" +
                "Hello : Goodbye\r\n" +
                "\r\n";
        InputStream in1 = new ByteArrayInputStream(s1.getBytes());
        assertThrows(BadRequestException.class,()->p.parseFromStream(in1));
    }

    @Test
    void throwsBadRequestForObsoleteLineFold() {
        HttpMessage m = new HttpMessage();
        HttpParser p = new HttpParser(m);

        String s1 = "GET /hello HTTP/1.1\r\n" +
                "Hello :\r\n" +
                " Goodbye\r\n" +
                "\r\n";
        InputStream in1 = new ByteArrayInputStream(s1.getBytes());
        assertThrows(BadRequestException.class,()->p.parseFromStream(in1));
    }

    @Test
    void throwsBadRequestForNoHostHeader() {
        HttpMessage m = new HttpMessage();
        HttpParser p = new HttpParser(m);

        String s1 = "GET /hello HTTP/1.1\r\n" +
                "Content-Length: 5\r\n" +
                "\r\n" +
                "hello";
        InputStream in1 = new ByteArrayInputStream(s1.getBytes());
        assertThrows(BadRequestException.class,()->p.parseFromStream(in1));
    }

    @Test
    void throwsForInvalidHttp() {
        HttpMessage m = new HttpMessage();
        HttpParser p = new HttpParser(m);

        InputStream in1 = new ByteArrayInputStream("g".getBytes());
        assertThrows(BadRequestException.class,()->p.parseFromStream(in1));
    }
}