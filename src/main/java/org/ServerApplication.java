package org;

import commandparser.BadUsageException;
import commandparser.CommandParser;
import httpserver.HttpMessage;
import httpserver.HttpServer;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Random;


public class ServerApplication {
    public CommandParser commandParser;
    public ServerApplication(String[] args, PrintStream printStream) throws IOException, BadUsageException {
        this.args = args;
        this.printStream = printStream;
        this.commandParser = new CommandParser();
        addCommands();
        commandParser.parseTokens(args);
        port = commandParser.getInt("-p");
        rootDirectory = commandParser.getString("-r");
        hostname = commandParser.getString("-h");
        repository = new FileRepository(rootDirectory);
        server = new HttpServer(new InetSocketAddress(hostname,port));
        server.setPrintStream(printStream);
        server.initialize();
        random = new Random();
    }
    public void run() {
        server.onConnection((req)-> {
            String uri = req.getURI();
            HttpMessage res = new HttpMessage();
            if (uri.equals("home"))
                buildWelcomeScreen(res);
            else if (uri.equals("ping"))
                buildPing(res);
            else if (uri.equals("guess"))
                buildGuess(res, req);
            else if (repository.isDirectory(uri))
                buildDirectory(res,uri);
            else if (repository.isFile(uri))
                buildFile(res,uri);
            return res;
        });
        new Thread(()->{
            while (server.isBound()) server.handleConnection();
        }).start();
    }
    public void addCommands() {
        commandParser.addCommand("-p",80);
        commandParser.addUsage("-p","-p <port>");
        commandParser.addCommand("-r",".");
        commandParser.addUsage("-r","-r <rootDirectory>");
        commandParser.addCommand("-h","127.0.0.1");
        commandParser.addUsage("-h","-h <hostname>");
    }
    public HttpServer getServer() {
        return server;
    }
    public static String formatInstant(Instant instant) {
        return DateTimeFormatter
                .ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
                .withZone(ZoneId.of("GMT"))
                .format(instant);
    }
    public void setRandom(Random random) {
        this.random = random;
    }
    private void buildWelcomeScreen(HttpMessage res) {
        String body = "<h1>Welcome</h1><p>This is an http server</p>";
        res.setStartLine("HTTP/1.1 200 OK");
        res.putHeader("Content-Length", String.valueOf(body.length()));
        res.putHeader("Content-Type","text/html");
        res.setBody(body);
    }
    private void buildPing(HttpMessage res) {
        try {
            Thread.sleep(1000);
        } catch (Exception ignored) {}
        String body = formatInstant(Instant.now());
        res.setStartLine("HTTP/1.1 200 OK");
        res.putHeader("Content-Length", String.valueOf(body.length()));
        res.putHeader("Content-Type","text/html");
        res.setBody(body);
    }
    private void buildDirectory(HttpMessage res, String uri) {
        String[] files = repository.fileNames(uri);
        String body = "";
        for (String file: files) {
            file = file.substring(rootDirectory.length());
            body += "<a href=\"" + file + "\">" + file + "</a>" + "<br>";
        }
        res.setStartLine("HTTP/1.1 200 OK");
        res.putHeader("Content-Length", String.valueOf(body.length()));
        res.putHeader("Content-Type","text/html");
        res.setBody(body);
    }
    private void buildFile(HttpMessage res, String uri) {
        Path p = repository.getPath(uri);
        byte[] body = repository.fileData(uri);
        res.setStartLine("HTTP/1.1 200 OK");
        res.putHeader("Content-Length", String.valueOf(body.length));
        try {
            res.putHeader("Content-Type", Files.probeContentType(p));
        } catch (IOException ignored) {}
        res.setBody(body);
    }
    private void buildGuess(HttpMessage res, HttpMessage req) {
        String method = req.getMethod();
        String body = "";
        if (method.equals("GET"))
            body = guessBody(random.nextInt(101)+1,0);
        else if (method.equals("POST")) {
            try {
                String str = new String(req.getBody());
                int guess = Integer.parseInt(parseValue("guess",str));
                int answer = Integer.parseInt(parseValue("answer",str));
                int tries = Integer.parseInt(parseValue("tries",str));
                if (guess == answer)
                    body = "You win!";
                else if (tries > 5)
                    body = "You lose!";
                else if (guess < answer)
                    body = guessBody(answer,tries+1) + " too low";
                else
                    body = guessBody(answer,tries+1) + " too high";
            } catch (Exception ignored) {}
        }
        res.setStartLine("HTTP/1.1 200 OK");
        res.putHeader("Content-Length", String.valueOf(body.length()));
        res.putHeader("Content-Type","text/html");
        res.setBody(body);
    }
    private String guessBody(int answer, int tries) {
        return "<form action=\"/guess\" method=\"post\">" +
                "<label for=\"guess\">Guess:</label>" +
                "<input type=\"number\" name=\"guess\">" +
                "<input type=\"hidden\" name=\"answer\" value=\"" +
                answer +
                "\">" +
                "<input type=\"hidden\" name=\"tries\" value=\"" +
                tries +
                "\">" +
                "<input type=\"submit\" value=\"Submit\">" +
                "</form>";
    }
    private String parseValue(String name, String string) {
        int i = string.indexOf(name);
        if (i == -1) return "";
        String s = string.substring(i);
        int k = s.indexOf('&');
        if (k == -1) return s.substring(name.length()+1);
        return s.substring(name.length()+1,k);
    }
    private int port;
    private String rootDirectory;
    private FileRepository repository;
    private HttpServer server;
    private String hostname;
    private Random random;
    private final String[] args;
    private final PrintStream printStream;
}
