package com.lbk.socialbanking.common.internal;

import com.lbk.socialbanking.common.api.ApiException;
import com.lbk.socialbanking.common.api.dto.ErrorEnvelope;
import com.lbk.socialbanking.common.api.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
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
                ex.getMessage(),
                TraceIdProvider.getTraceId()
        ));
        return ResponseEntity.status(ex.status()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ApiResponse(
            responseCode = "400",
            description = "Bad Request - Validation error",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorEnvelope.class),
                    examples = @ExampleObject(value = """
                            {
                              "error": {
                                "status": 400,
                                "code": "VALIDATION_ERROR",
                                "message": "Invalid input parameters",
                                "traceId": "abc123def456"
                              }
                            }
                            """)
            )
    )
    public ResponseEntity<ErrorEnvelope> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        log.warn("Validation error - {} {}: {}", req.getMethod(), req.getRequestURI(), message);

        var body = new ErrorEnvelope(new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                message,
                TraceIdProvider.getTraceId()
        ));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorEnvelope> handleMissingHeader(MissingRequestHeaderException ex, HttpServletRequest req) {
        log.warn("Missing header error - {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());

        var body = new ErrorEnvelope(new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                "Missing required header.",
                TraceIdProvider.getTraceId()
        ));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorEnvelope> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .findFirst()
                .orElse("Validation failed");

        log.warn("Constraint violation - {} {}: {}", req.getMethod(), req.getRequestURI(), message);

        var body = new ErrorEnvelope(new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                message,
                TraceIdProvider.getTraceId()
        ));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorEnvelope.class),
                    examples = @ExampleObject(value = """
                            {
                              "error": {
                                "status": 500,
                                "code": "INTERNAL_ERROR",
                                "message": "Unexpected error",
                                "traceId": "abc123def456"
                              }
                            }
                            """)
            )
    )
    public ResponseEntity<ErrorEnvelope> handleOther(Exception ex, HttpServletRequest req) {
        log.error("Unexpected error - {} {}", req.getMethod(), req.getRequestURI(), ex);
        var body = new ErrorEnvelope(new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_ERROR",
                "Unexpected error.",
                TraceIdProvider.getTraceId()
        ));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

}
