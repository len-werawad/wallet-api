package com.lbk.wallet.common.api.dto;

public record ErrorResponse(
        // HTTP status code, Including the status field in the error response body improves clarity and reliability,
        // especially when HTTP headers are unavailable or lost. As stated in RFC 9457 name-status, the status is advisory and helps consumers interpret the error even outside the HTTP context.
        int status,
        String code,
        String message
) {
}
