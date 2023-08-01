package commandparser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BadUsageExceptionTest {

    @Test
    void createsCorrectMessage() {
        BadUsageException e = new BadUsageException("-p","-p <port>");
        assertEquals("Bad usage of -p. Usage: -p <port>",e.getMessage());
    }
}