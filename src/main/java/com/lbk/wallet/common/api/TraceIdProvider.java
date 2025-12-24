package com.lbk.wallet.common.api;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TraceIdProvider {

    public static String getTraceId() {
        return Optional.ofNullable(MDC.get("traceId"))
                .orElse("N/A");
    }
}