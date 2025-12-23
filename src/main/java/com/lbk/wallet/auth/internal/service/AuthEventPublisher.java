package com.lbk.wallet.auth.internal.service;

import com.lbk.wallet.auth.events.UserLoggedInEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AuthEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(AuthEventPublisher.class);

    private final ApplicationEventPublisher eventPublisher;

    public AuthEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Async("dashboardEventExecutor")
    public void publishUserLoggedIn(String userId) {
        try {
            log.debug("Publishing user logged in event for user: {}", userId);

            UserLoggedInEvent event = new UserLoggedInEvent(userId, LocalDateTime.now());

            eventPublisher.publishEvent(event);
            log.trace("Successfully published user logged in event for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to publish user logged in event for user: {}", userId, e);
        }
    }
}
