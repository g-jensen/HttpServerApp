package httpserver;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

public class HttpParser {
    public HttpParser(HttpMessage message) {
        this.message = message;
    }
    public void parseFromStream(InputStream stream) throws BadRequestException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        try {
            message.setStartLine(reader.readLine());
            message.isRequest = !message.getStartLine().startsWith("HTTP");
            if (message.isRequest) {
                parseURI(message.getStartLine());
                parseMethod(message.getStartLine());
            }

            String line = reader.readLine();
            while (!line.isEmpty()) {
                parseHeader(line);
                line = reader.readLine();
            }
            if (!message.getHeaderFields().containsKey("Host") && message.isRequest)
                throw new BadRequestException();
            parseBody(reader);
        } catch (Exception e) {
            throw new BadRequestException();
        }
    }
    private void parseURI(String startLine) {
        int i = startLine.indexOf('/');
        if (i == -1) return;
        String s = startLine.substring(i+1);
        int k = s.indexOf(' ');
        message.setURI(s.substring(0,k));
    }
    private void parseMethod(String startLine) {
        int i = startLine.indexOf(' ');
        if (i == -1) return;
        message.setMethod(startLine.substring(0,i));
    }
    private void parseHeader(String line) throws BadRequestException {
        if (line.startsWith(" ")) throw new BadRequestException();
        int i = line.indexOf(':');
        if (i < 1) return;
        if (line.charAt(i-1) == ' ') throw new BadRequestException();
        String key = line.substring(0,i);
        String value = line.substring(i+1).trim();
        message.putHeader(key,value);
    }
    private void parseBody(BufferedReader reader) throws BadRequestException {
        if (!message.getHeaderFields().containsKey("Content-Length"))
            return;
        try {
            int k = Integer.parseInt(message.getHeaderFields().get("Content-Length"));
            char[] bytes = new char[k];
            reader.read(bytes,0,k);
            message.setBody(new String(bytes).getBytes());
        } catch (Exception e) {
            throw new BadRequestException();
        }
    }
    private HttpMessage message;
}
