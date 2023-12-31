package main;

import commandparser.BadUsageException;
import org.httpserver.BadRequestException;
import org.httpserver.HttpMessage;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class ServerApplicationTest {

    @Test
    void addsCommands() throws IOException, BadUsageException {
        PrintStream p = new PrintStream(new ByteArrayOutputStream());
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
        PrintStream p = new PrintStream(new ByteArrayOutputStream());
        ServerApplication s = new ServerApplication(new String[]{"-p","8087"},p);
        s.run();

        Socket socket1 = new Socket();
        socket1.connect(s.getServer().socketAddress());
        socket1.getOutputStream().write("GET /hello HTTP/1.1\r\nHost: me\r\n\n".getBytes());

        HttpMessage m = new HttpMessage(socket1.getInputStream());
        assertEquals("HTTP/1.1 200 OK",m.getStartLine());
        assertEquals("<h1>Welcome</h1><p>This is an http server</p>",new String(m.getBody()));
    }

    @Test
    void respondsWithPing() throws BadUsageException, IOException, BadRequestException {
        PrintStream p = new PrintStream(new ByteArrayOutputStream());
        ServerApplication s = new ServerApplication(new String[]{"-p","8088"},p);
        s.run();

        Socket socket1 = new Socket();
        socket1.connect(s.getServer().socketAddress());
        String time = ServerApplication.formatInstant(Instant.now().plusSeconds(1));
        socket1.getOutputStream().write("GET /ping HTTP/1.1\r\nHost: me\r\n\r\n".getBytes());

        HttpMessage m = new HttpMessage(socket1.getInputStream());
        assertEquals("HTTP/1.1 200 OK",m.getStartLine());
        assertEquals(time,new String(m.getBody()));
    }

    @Test
    void respondsWithFilesInDirectory() throws BadUsageException, IOException, BadRequestException {
        PrintStream p = new PrintStream(new ByteArrayOutputStream());
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
                "<a href=\"/resources/test.txt\">/resources/test.txt</a><br>" +
                "<a href=\"/resources/Isopoly_05.gif\">/resources/Isopoly_05.gif</a><br>";
        assertEquals(str1,new String(m.getBody()));
    }

    @Test
    void respondsWithFile() throws IOException, BadRequestException, BadUsageException {
        PrintStream p = new PrintStream(new ByteArrayOutputStream());
        ServerApplication s = new ServerApplication(new String[]{"-p","8091"},p);
        s.run();

        Socket socket1 = new Socket();
        socket1.connect(s.getServer().socketAddress());
        socket1.getOutputStream().write("GET /resources/test.txt HTTP/1.1\r\nHost: me\r\n\r\n".getBytes());

        HttpMessage m = new HttpMessage(socket1.getInputStream());
        assertEquals("HTTP/1.1 200 OK",m.getStartLine());
        assertEquals("Hello",new String(m.getBody()));
    }

    @Test
    void respondsWithTxtMimeType() throws BadUsageException, IOException, BadRequestException {
        PrintStream p = new PrintStream(new ByteArrayOutputStream());
        ServerApplication s = new ServerApplication(new String[]{"-p","8099"},p);
        s.run();

        Socket socket1 = new Socket();
        socket1.connect(s.getServer().socketAddress());
        socket1.getOutputStream().write("GET /resources/test.txt HTTP/1.1\r\nHost: me\r\n\r\n".getBytes());

        HttpMessage m = new HttpMessage(socket1.getInputStream());
        assertEquals("text/plain",m.getHeaderFields().get("Content-Type"));
    }

    @Test
    void respondsWithPngMimeType() throws BadUsageException, IOException, BadRequestException {
        PrintStream p = new PrintStream(new ByteArrayOutputStream());
        ServerApplication s = new ServerApplication(new String[]{"-p","8100"},p);
        s.run();

        Socket socket1 = new Socket();
        socket1.connect(s.getServer().socketAddress());
        socket1.getOutputStream().write("GET /resources/math-bridge.png HTTP/1.1\r\nHost: me\r\n\r\n".getBytes());
        HttpMessage m = new HttpMessage(socket1.getInputStream());
        assertEquals("image/png",m.getHeaderFields().get("Content-Type"));
    }

    @Test
    void respondsForNestedDirectories() throws BadUsageException, IOException, BadRequestException {
        PrintStream p = new PrintStream(new ByteArrayOutputStream());
        ServerApplication s = new ServerApplication(new String[]{"-p","8092"},p);
        s.run();

        Socket socket1 = new Socket();
        socket1.connect(s.getServer().socketAddress());
        socket1.getOutputStream().write("GET /resources/moreStuff HTTP/1.1\r\nHost: me\r\n\r\n".getBytes());

        HttpMessage m = new HttpMessage(socket1.getInputStream());
        assertEquals("HTTP/1.1 200 OK",m.getStartLine());
        String str1 = "<a href=\"/resources/moreStuff/page\">/resources/moreStuff/page</a><br>" +
                "<a href=\"/resources/moreStuff/greetings\">/resources/moreStuff/greetings</a><br>";
        assertEquals(str1,new String(m.getBody()));
    }

    @Test
    void respondsToGuessGet() throws BadUsageException, IOException, BadRequestException {
        PrintStream p = new PrintStream(new ByteArrayOutputStream());
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
        assertEquals(str,new String(m.getBody()));
    }

    @Test
    void respondsToWinningGuessPost() throws BadUsageException, IOException, BadRequestException {
        PrintStream p = new PrintStream(new ByteArrayOutputStream());
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
        assertEquals("You win!",new String(m.getBody()));
    }

    @Test
    void respondsToLosingGuessPost() throws BadUsageException, IOException, BadRequestException {
        PrintStream p = new PrintStream(new ByteArrayOutputStream());
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
        assertEquals("You lose!",new String(m.getBody()));
    }

    @Test
    void respondsToLowGuessPost() throws BadUsageException, IOException, BadRequestException {
        PrintStream p = new PrintStream(new ByteArrayOutputStream());
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
        assertEquals(str,new String(m.getBody()));
    }

    @Test
    void respondsToHighGuessPost() throws BadUsageException, IOException, BadRequestException {
        PrintStream p = new PrintStream(new ByteArrayOutputStream());
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
        assertEquals(str,new String(m.getBody()));
    }

    @Test
    void respondsWithIndexHTML() throws BadUsageException, IOException, BadRequestException {
        PrintStream p = new PrintStream(new ByteArrayOutputStream());
        ServerApplication s = new ServerApplication(new String[]{"-p","8098"},p);
        s.run();

        Socket socket1 = new Socket();
        socket1.connect(s.getServer().socketAddress());
        socket1.getOutputStream().write((
                "GET /resources/moreStuff/page HTTP/1.1\r\n" +
                "Host: me\r\n\r\n").getBytes());
        String str = "<h1>This is my page</h1>\n<p>In the file index.html</p>";
        HttpMessage m = new HttpMessage(socket1.getInputStream());
        assertEquals("HTTP/1.1 200 OK",m.getStartLine());
        assertEquals(str,new String(m.getBody()));
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