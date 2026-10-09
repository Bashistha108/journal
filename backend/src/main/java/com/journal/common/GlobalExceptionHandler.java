package com.journal.common;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleException(Exception ex, HttpServletRequest request) {
        String requestId = (String) request.getAttribute("requestId");
        if (requestId == null) {
            requestId = "unknown";
        }
        
        log.error("Unhandled exception [requestId: {}]", requestId, ex);

        ApiError error = new ApiError(
                ErrorCode.INTERNAL_ERROR.getStatus(),
                ErrorCode.INTERNAL_ERROR.name(),
                "An unexpected error occurred.",
                requestId,
                List.of()
        );

        return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.getStatus()).body(error);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiError> handleValidationException(ValidationException ex, HttpServletRequest request) {
        String requestId = (String) request.getAttribute("requestId");
        if (requestId == null) {
            requestId = "unknown";
        }
        
        ApiError error = new ApiError(
                ErrorCode.VALIDATION_FAILED.getStatus(),
                ErrorCode.VALIDATION_FAILED.name(),
                "Validation failed.",
                requestId,
                ex.getFieldErrors()
        );

        return ResponseEntity.status(ErrorCode.VALIDATION_FAILED.getStatus()).body(error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        String requestId = (String) request.getAttribute("requestId");
        if (requestId == null) {
            requestId = "unknown";
        }
        
        ErrorCode errorCode;
        try {
            errorCode = ErrorCode.valueOf(ex.getCode());
        } catch (IllegalArgumentException e) {
            errorCode = ErrorCode.INTERNAL_ERROR;
        }

        ApiError error = new ApiError(
                errorCode.getStatus(),
                errorCode.name(),
                ex.getMessage(),
                requestId,
                List.of()
        );

        return ResponseEntity.status(errorCode.getStatus()).body(error);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException ex, HttpServletRequest request) {
        String requestId = (String) request.getAttribute("requestId");
        if (requestId == null) {
            requestId = "unknown";
        }
        
        ErrorCode errorCode;
        try {
            errorCode = ErrorCode.valueOf(ex.getCode());
        } catch (IllegalArgumentException e) {
            errorCode = ErrorCode.INTERNAL_ERROR;
        }

        ApiError error = new ApiError(
                errorCode.getStatus(),
                errorCode.name(),
                ex.getMessage(),
                requestId,
                List.of()
        );

        return ResponseEntity.status(errorCode.getStatus()).body(error);
    }
}
