package commandparser;

public class BadUsageException extends Exception {
    public BadUsageException(String token, String correctUsage) {
        super("Bad usage of " + token + ". Usage: " + correctUsage);
    }
}
