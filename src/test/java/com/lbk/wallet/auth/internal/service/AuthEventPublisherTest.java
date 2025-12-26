package com.lbk.wallet.auth.internal.service;

import com.lbk.wallet.auth.events.UserLoggedInEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthEventPublisherTest {

    private static final String USER_ID = "user123";

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AuthEventPublisher authEventPublisher;

    @Nested
    @DisplayName("publishUserLoggedIn method tests")
    class PublishUserLoggedInTests {

        @Test
        @DisplayName("should publish UserLoggedInEvent successfully")
        void publishUserLoggedIn_success() {
            authEventPublisher.publishUserLoggedIn(USER_ID);

            ArgumentCaptor<UserLoggedInEvent> eventCaptor = ArgumentCaptor.forClass(UserLoggedInEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            UserLoggedInEvent capturedEvent = eventCaptor.getValue();
            assertThat(capturedEvent).isNotNull();
            assertThat(capturedEvent.getUserId()).isEqualTo(USER_ID);
            assertThat(capturedEvent.getTimestamp()).isCloseTo(LocalDateTime.now(), within(1, java.time.temporal.ChronoUnit.SECONDS));
        }

        @Test
        @DisplayName("should handle different user IDs")
        void publishUserLoggedIn_differentUserIds() {
            String userId1 = "user-001";
            String userId2 = "user-002";

            authEventPublisher.publishUserLoggedIn(userId1);
            authEventPublisher.publishUserLoggedIn(userId2);

            ArgumentCaptor<UserLoggedInEvent> eventCaptor = ArgumentCaptor.forClass(UserLoggedInEvent.class);
            verify(eventPublisher, times(2)).publishEvent(eventCaptor.capture());

            var events = eventCaptor.getAllValues();
            assertThat(events).hasSize(2);
            assertThat(events.get(0).getUserId()).isEqualTo(userId1);
            assertThat(events.get(1).getUserId()).isEqualTo(userId2);
        }

        @Test
        @DisplayName("should handle null userId")
        void publishUserLoggedIn_nullUserId() {
            authEventPublisher.publishUserLoggedIn(null);

            ArgumentCaptor<UserLoggedInEvent> eventCaptor = ArgumentCaptor.forClass(UserLoggedInEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            UserLoggedInEvent event = eventCaptor.getValue();
            assertThat(event.getUserId()).isNull();
        }

        @Test
        @DisplayName("should handle empty userId")
        void publishUserLoggedIn_emptyUserId() {
            authEventPublisher.publishUserLoggedIn("");

            ArgumentCaptor<UserLoggedInEvent> eventCaptor = ArgumentCaptor.forClass(UserLoggedInEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            UserLoggedInEvent event = eventCaptor.getValue();
            assertThat(event.getUserId()).isEmpty();
        }
    }
}
