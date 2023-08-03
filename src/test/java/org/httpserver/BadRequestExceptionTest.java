package org.httpserver;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BadRequestExceptionTest {

    @Test
    void createsMessage() {
        BadRequestException b = new BadRequestException();
        assertEquals("HTTP/1.1 400 Bad Request\r\n\r\n",b.getMessage());
    }
}