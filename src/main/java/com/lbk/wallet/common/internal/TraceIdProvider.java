package com.lbk.wallet.common.internal;

import org.slf4j.MDC;

import java.util.Optional;

/**
 * Utility class to provide the current W3C trace-id from MDC.
 */
public final class TraceIdProvider {

    private TraceIdProvider() {
    }

    /**
     * Returns the current W3C trace-id from MDC, if present.
     * <p>
     * This is the 32-character hex trace-id part of the W3C Trace Context
     * ({@code traceparent}) header, not the full header value.
     * </p>
     */
    public static String getTraceId() {
        return Optional.ofNullable(MDC.get("traceId"))
                .orElse("N/A");
    }
}