package com.lbk.socialbanking.common.api;

import com.lbk.socialbanking.common.api.dto.ErrorDetail;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * ApiException is a custom runtime exception that represents API-related errors.
 * It includes an HTTP status, an error code, a message, and optional detailed error information.
 *
 * <p>Typical JSON error response produced by {@code GlobalExceptionHandler}:</p>
 *
 * <pre>{@code
 * {
 *   "status": 400,
 *   "code": "INVALID_REQUEST",
 *   "message": "The request parameters are invalid.",
 *   "traceId": "f41f2a13779e638f608870a11e394bd1",
 * }
 * }</pre>
 *
 * <p>
 * The {@code status} field in the response body is advisory and mirrors the HTTP status code.
 * Including it improves clarity when the error is consumed outside of an HTTP context.
 * </p>
 *
 */
public class ApiException extends RuntimeException {

    // Including the status field in the error response body improves clarity and reliability, especially when HTTP headers are unavailable or lost. As stated in RFC 9457 name-status,
    // the status is advisory and helps consumers interpret the error even outside the HTTP context.
    private final HttpStatus status;
    private final String code;
    private final List<ErrorDetail> details;

    public ApiException(HttpStatus status, String code, String message, List<ErrorDetail> details) {
        super(message);
        this.status = status;
        this.code = code;
        this.details = details == null ? List.of() : List.copyOf(details);
    }

    public ApiException(HttpStatus status, String code, String message) {
        this(status, code, message, List.of());
    }

    public HttpStatus status() {
        return status;
    }

    public String code() {
        return code;
    }

    public List<ErrorDetail> details() {
        return details;
    }
}
