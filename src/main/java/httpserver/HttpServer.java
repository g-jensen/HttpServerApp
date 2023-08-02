package httpserver;

import java.io.*;
import java.net.*;
import java.util.Objects;
import java.util.function.Function;

public class HttpServer {
    public HttpServer(InetSocketAddress address) throws IOException {
        this.address = address;
        this.server = new ServerSocket();
        this.printStream = System.out;
    }
    public void initialize() {
        try {
            server.bind(address);
            printStream.println(listeningString());
        } catch (Exception e) {
            printStream.println(e.getMessage());
        }
    }
    public void run() {
        new Thread(()->{
            while (isRunning()) handleConnection();
        }).start();
    }
    public void stop() {
        try {
            server.close();
        } catch (IOException ignored) {}
    }
    public Socket listen() {
        try {
            return server.accept();
        } catch (IOException ignore) {return null;}
    }
    public void send(Socket s, byte[] bytes) {
        try {
            s.getOutputStream().write(bytes);
        } catch (Exception ignore) {}
    }
    public InputStream getInput(Socket s) {
        try {
            return s.getInputStream();
        } catch (Exception ignore) {
            return new ByteArrayInputStream(new byte[0]);
        }
    }
    public void handleConnection() {
        Socket s = listen();
        new Thread(()-> {
            try {
                HttpMessage req = new HttpMessage(getInput(s));
                HttpMessage res = action.apply(req);
                send(s,res.getStartLineAndHeaders().getBytes());
                byte[] body = res.getBody();
                if (Objects.nonNull(body))
                    send(s,body);
            } catch (BadRequestException e) {
                send(s,e.getMessage().getBytes());
            }
        }).start();
    }
    public void onConnection(Function<HttpMessage, HttpMessage> action) {
        this.action = action;
    }
    public boolean isRunning() {return isBound() && !server.isClosed();}
    public boolean isBound() {
        return server.isBound();
    }
    public SocketAddress socketAddress() {
        return server.getLocalSocketAddress();
    }
    public void setPrintStream(PrintStream printStream) {
        this.printStream = printStream;
    }
    public String listeningString() {
        return "Listening at " + address.getHostName() + " on port " + address.getPort();
    }
    public ServerSocket getServer() {
        return server;
    }
    private Function<HttpMessage, HttpMessage> action;
    private PrintStream printStream;
    private final ServerSocket server;
    private final InetSocketAddress address;
}
