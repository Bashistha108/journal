package com.journal.common;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {
    @Test
    void shouldReturnInternalErrorForUnhandledException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("requestId", "test-req-123");

        ResponseEntity<ApiError> response = handler.handleException(new RuntimeException("Oops"), request);

        assertEquals(500, response.getStatusCode().value());
        ApiError error = response.getBody();
        assertNotNull(error);
        assertEquals("INTERNAL_ERROR", error.code());
        assertEquals("test-req-123", error.requestId());
        assertEquals(500, error.status());
    }
}
