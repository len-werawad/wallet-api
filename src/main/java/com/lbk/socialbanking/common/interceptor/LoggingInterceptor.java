package com.lbk.socialbanking.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            MDC.put("userId", auth.getName());
        }

        log.debug("Incoming request method: {} uri: {} traceId: {}",
                request.getMethod(),
                request.getRequestURI(),
                MDC.get("traceId"));

        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        try {
            long startTime = (Long) request.getAttribute("startTime");
            long duration = System.currentTimeMillis() - startTime;

            // Log response
            log.debug("Completed request: {} {} - Status: {} - Duration: {}ms traceId: {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    duration, MDC.get("traceId"));

            if (ex != null) {
                log.error("Request failed with exception: {} {} traceId: {}",
                        request.getMethod(),
                        request.getRequestURI(),
                        MDC.get("traceId"),
                        ex);
            }
        } finally {
            // Clean up MDC
            MDC.clear();
        }
    }
}
