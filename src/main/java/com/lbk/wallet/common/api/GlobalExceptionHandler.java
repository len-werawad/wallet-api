package com.lbk.wallet.common.api;

import com.lbk.wallet.common.api.dto.ErrorEnvelope;
import com.lbk.wallet.common.api.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorEnvelope> handle(ApiException ex, HttpServletRequest req) {
        if (ex.status().is5xxServerError()) {
            log.error("API Exception [{}]: {} - {} {}", ex.code(), ex.getMessage(), req.getMethod(), req.getRequestURI(), ex);
        } else {
            log.warn("API Exception [{}]: {} - {} {}", ex.code(), ex.getMessage(), req.getMethod(), req.getRequestURI());
        }

        var body = new ErrorEnvelope(new ErrorResponse(
                ex.status().value(),
                ex.code(),
                ex.getMessage()
        ));
        return ResponseEntity.status(ex.status()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorEnvelope> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        log.warn("Validation error - {} {}: {}", req.getMethod(), req.getRequestURI(), message);

        var body = new ErrorEnvelope(new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                message
        ));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorEnvelope> handleMissingHeader(MissingRequestHeaderException ex, HttpServletRequest req) {
        log.warn("Missing header error - {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());

        var body = new ErrorEnvelope(new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                "Missing required header."
        ));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorEnvelope> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .findFirst()
                .orElse("Validation failed");

        log.warn("Constraint violation - {} {}: {}", req.getMethod(), req.getRequestURI(), message);

        var body = new ErrorEnvelope(new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                message
        ));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorEnvelope> handleOther(Exception ex, HttpServletRequest req) {
        log.error("Unexpected error - {} {}", req.getMethod(), req.getRequestURI(), ex);
        var body = new ErrorEnvelope(new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_ERROR",
                "Unexpected error."
        ));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

}
