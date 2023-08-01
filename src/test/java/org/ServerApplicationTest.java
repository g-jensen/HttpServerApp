package org;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import commandparser.BadUsageException;
import httpserver.BadRequestException;
import httpserver.HttpMessage;
import httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;


import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class ServerApplicationTest {

    void pingServer(HttpServer server) {
        Socket socket = new Socket();
        try {
            socket.connect(server.getServer().getLocalSocketAddress());
            socket.getOutputStream().write("GET /ping HTTP/1.1\r\nHost: me\r\n\r\n".getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void addsCommands() throws IOException, BadUsageException {
        PrintStream p = new PrintStream(new ByteOutputStream());
        ServerApplication s = new ServerApplication(new String[]{"-p","8086"},p);

        s.addCommands();
        assertEquals(80,s.commandParser.getInt("-p"));
        assertEquals("-p <port>",s.commandParser.getUsage("-p"));
        assertEquals(".",s.commandParser.getString("-r"));
        assertEquals("-r <rootDirectory>",s.commandParser.getUsage("-r"));
        assertEquals("127.0.0.1",s.commandParser.getString("-h"));
        assertEquals("-h <hostname>",s.commandParser.getUsage("-h"));
    }

    @Test
    void respondsWithWelcomeScreen() throws IOException, BadUsageException, BadRequestException {
        PrintStream p = new PrintStream(new ByteOutputStream());
        ServerApplication s = new ServerApplication(new String[]{"-p","8087"},p);
        s.run();

        Socket socket1 = new Socket();
        socket1.connect(s.getServer().socketAddress());
        socket1.getOutputStream().write("GET /home HTTP/1.1\r\nHost: me\r\n\n".getBytes());

        HttpMessage m = new HttpMessage(socket1.getInputStream());
        assertEquals("HTTP/1.1 200 OK",m.getStartLine());
        assertEquals("<h1>Welcome</h1><p>This is an http server</p>",m.getBody());
    }

    @Test
    void respondsWithPing() throws BadUsageException, IOException, BadRequestException {
        PrintStream p = new PrintStream(new ByteOutputStream());
        ServerApplication s = new ServerApplication(new String[]{"-p","8088"},p);
        s.run();

        Socket socket1 = new Socket();
        socket1.connect(s.getServer().socketAddress());
        String time = ServerApplication.formatInstant(Instant.now().plusSeconds(1));
        socket1.getOutputStream().write("GET /ping HTTP/1.1\r\nHost: me\r\n\r\n".getBytes());

        HttpMessage m = new HttpMessage(socket1.getInputStream());
        assertEquals("HTTP/1.1 200 OK",m.getStartLine());
        assertEquals(time,m.getBody());
    }

    @Test
    void respondsWithFilesInDirectory() throws BadUsageException, IOException, BadRequestException {
        PrintStream p = new PrintStream(new ByteOutputStream());
        ServerApplication s = new ServerApplication(new String[]{"-p","8089"},p);
        s.run();

        Socket socket1 = new Socket();
        socket1.connect(s.getServer().socketAddress());
        socket1.getOutputStream().write("GET /resources HTTP/1.1\r\nHost: me\r\n\r\n".getBytes());

        HttpMessage m = new HttpMessage(socket1.getInputStream());
        assertEquals("HTTP/1.1 200 OK",m.getStartLine());
        String str1 =
                "<a href=\"/resources/sloane.jpg\">/resources/sloane.jpg</a><br>" +
                "<a href=\"/resources/britain.pdf\">/resources/britain.pdf</a><br>" +
                "<a href=\"/resources/moreStuff\">/resources/moreStuff</a><br>" +
                "<a href=\"/resources/math-bridge.png\">/resources/math-bridge.png</a><br>" +
                "<a href=\"/resources/hello.md\">/resources/hello.md</a><br>" +
                "<a href=\"/resources/test.txt\">/resources/test.txt</a><br>";
        assertEquals(str1,m.getBody());
    }

    @Test
    void respondsWithFile() throws IOException, BadRequestException, BadUsageException {
        PrintStream p = new PrintStream(new ByteOutputStream());
        ServerApplication s = new ServerApplication(new String[]{"-p","8091"},p);
        s.run();

        Socket socket1 = new Socket();
        socket1.connect(s.getServer().socketAddress());
        socket1.getOutputStream().write("GET /resources/test.txt HTTP/1.1\r\nHost: me\r\n\r\n".getBytes());

        HttpMessage m = new HttpMessage(socket1.getInputStream());
        assertEquals("HTTP/1.1 200 OK",m.getStartLine());
        assertEquals("Hello",m.getBody());
    }

    @Test
    void respondsForNestedDirectories() throws BadUsageException, IOException, BadRequestException {
        PrintStream p = new PrintStream(new ByteOutputStream());
        ServerApplication s = new ServerApplication(new String[]{"-p","8092"},p);
        s.run();

        Socket socket1 = new Socket();
        socket1.connect(s.getServer().socketAddress());
        socket1.getOutputStream().write("GET /resources/moreStuff HTTP/1.1\r\nHost: me\r\n\r\n".getBytes());

        HttpMessage m = new HttpMessage(socket1.getInputStream());
        assertEquals("HTTP/1.1 200 OK",m.getStartLine());
        String str1 =
                "<a href=\"/resources/moreStuff/greetings\">" +
                "/resources/moreStuff/greetings</a><br>";
        assertEquals(str1,m.getBody());
    }

    @Test
    void respondsToGuessGet() throws BadUsageException, IOException, BadRequestException {
        PrintStream p = new PrintStream(new ByteOutputStream());
        ServerApplication s = new ServerApplication(new String[]{"-p","8093"},p);
        s.run();

        Random rand = mock(Random.class);
        Mockito.when(rand.nextInt(101)).thenReturn(14);
        s.setRandom(rand);

        Socket socket1 = new Socket();
        socket1.connect(s.getServer().socketAddress());
        socket1.getOutputStream().write("GET /guess HTTP/1.1\r\nHost: me\r\n\r\n".getBytes());
        String str = "<form action=\"/guess\" method=\"post\">" +
                "<label for=\"guess\">Guess:</label>" +
                "<input type=\"number\" name=\"guess\">" +
                "<input type=\"hidden\" name=\"answer\" value=\"15\">" +
                "<input type=\"hidden\" name=\"tries\" value=\"0\">" +
                "<input type=\"submit\" value=\"Submit\"></form>";
        HttpMessage m = new HttpMessage(socket1.getInputStream());
        assertEquals("HTTP/1.1 200 OK",m.getStartLine());
        assertEquals(str,m.getBody());
    }

    @Test
    void respondsToWinningGuessPost() throws BadUsageException, IOException, BadRequestException {
        PrintStream p = new PrintStream(new ByteOutputStream());
        ServerApplication s = new ServerApplication(new String[]{"-p","8094"},p);
        s.run();

        Socket socket1 = new Socket();
        socket1.connect(s.getServer().socketAddress());
        socket1.getOutputStream().write(("POST /guess HTTP/1.1\r\n" +
                "Host: me\r\n" +
                "Content-Length: 26\r\n\r\n" +
                "guess=15&answer=15&tries=1").getBytes());
        HttpMessage m = new HttpMessage(socket1.getInputStream());
        assertEquals("HTTP/1.1 200 OK",m.getStartLine());
        assertEquals("You win!",m.getBody());
    }

    @Test
    void respondsToLosingGuessPost() throws BadUsageException, IOException, BadRequestException {
        PrintStream p = new PrintStream(new ByteOutputStream());
        ServerApplication s = new ServerApplication(new String[]{"-p","8095"},p);
        s.run();

        Socket socket1 = new Socket();
        socket1.connect(s.getServer().socketAddress());
        socket1.getOutputStream().write(("POST /guess HTTP/1.1\r\n" +
                "Host: me\r\n" +
                "Content-Length: 26\r\n\r\n" +
                "guess=14&answer=15&tries=6").getBytes());
        HttpMessage m = new HttpMessage(socket1.getInputStream());
        assertEquals("HTTP/1.1 200 OK",m.getStartLine());
        assertEquals("You lose!",m.getBody());
    }

    @Test
    void respondsToLowGuessPost() throws BadUsageException, IOException, BadRequestException {
        PrintStream p = new PrintStream(new ByteOutputStream());
        ServerApplication s = new ServerApplication(new String[]{"-p","8096"},p);
        s.run();

        Socket socket1 = new Socket();
        socket1.connect(s.getServer().socketAddress());
        socket1.getOutputStream().write(("POST /guess HTTP/1.1\r\n" +
                "Host: me\r\n" +
                "Content-Length: 26\r\n\r\n" +
                "guess=14&answer=15&tries=2").getBytes());
        String str = "<form action=\"/guess\" method=\"post\">" +
                "<label for=\"guess\">Guess:</label>" +
                "<input type=\"number\" name=\"guess\">" +
                "<input type=\"hidden\" name=\"answer\" value=\"15\">" +
                "<input type=\"hidden\" name=\"tries\" value=\"3\">" +
                "<input type=\"submit\" value=\"Submit\"></form> too low";
        HttpMessage m = new HttpMessage(socket1.getInputStream());
        assertEquals("HTTP/1.1 200 OK",m.getStartLine());
        assertEquals(str,m.getBody());
    }

    @Test
    void respondsToHighGuessPost() throws BadUsageException, IOException, BadRequestException {
        PrintStream p = new PrintStream(new ByteOutputStream());
        ServerApplication s = new ServerApplication(new String[]{"-p","8097"},p);
        s.run();

        Socket socket1 = new Socket();
        socket1.connect(s.getServer().socketAddress());
        socket1.getOutputStream().write(("POST /guess HTTP/1.1\r\n" +
                "Host: me\r\n" +
                "Content-Length: 26\r\n\r\n" +
                "guess=16&answer=15&tries=2").getBytes());
        String str = "<form action=\"/guess\" method=\"post\">" +
                "<label for=\"guess\">Guess:</label>" +
                "<input type=\"number\" name=\"guess\">" +
                "<input type=\"hidden\" name=\"answer\" value=\"15\">" +
                "<input type=\"hidden\" name=\"tries\" value=\"3\">" +
                "<input type=\"submit\" value=\"Submit\"></form> too high";
        HttpMessage m = new HttpMessage(socket1.getInputStream());
        assertEquals("HTTP/1.1 200 OK",m.getStartLine());
        assertEquals(str,m.getBody());
    }

    @Test
    void handlesConnectionsAsynchronously() throws BadUsageException, IOException {
        PrintStream p = new PrintStream(new ByteOutputStream());
        ServerApplication s = new ServerApplication(new String[]{"-p","8090"},p);
        s.run();

        Instant start = Instant.now();
        for (int i = 0; i < 5; i++) {
            pingServer(s.getServer());
        }
        Instant end = Instant.now();
        assertTrue(Duration.between(start,end).getSeconds() < 5);
    }

    @Test
    void formatsAnInstant() {
        Instant i1 = Instant.EPOCH;
        assertEquals("Thu, 01 Jan 1970 00:00:00 GMT", ServerApplication.formatInstant(i1));

        Instant i2 = Instant.EPOCH.plus(1, ChronoUnit.SECONDS);
        assertEquals("Thu, 01 Jan 1970 00:00:01 GMT",ServerApplication.formatInstant(i2));

        Instant i3 = Instant.EPOCH.plus(1, ChronoUnit.MINUTES);
        assertEquals("Thu, 01 Jan 1970 00:01:00 GMT",ServerApplication.formatInstant(i3));

        Instant i4 = Instant.EPOCH.plus(1, ChronoUnit.HOURS);
        assertEquals("Thu, 01 Jan 1970 01:00:00 GMT",ServerApplication.formatInstant(i4));

        Instant i5 = Instant.EPOCH.plus(1, ChronoUnit.DAYS);
        assertEquals("Fri, 02 Jan 1970 00:00:00 GMT",ServerApplication.formatInstant(i5));

        Instant i6 = Instant.EPOCH.plus(365, ChronoUnit.DAYS);
        assertEquals("Fri, 01 Jan 1971 00:00:00 GMT",ServerApplication.formatInstant(i6));
    }
}