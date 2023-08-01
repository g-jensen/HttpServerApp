package httpserver;

public class BadRequestException extends Exception {
    BadRequestException() {
        super("HTTP/1.1 400 Bad Request\r\n\r\n");
    }
}
