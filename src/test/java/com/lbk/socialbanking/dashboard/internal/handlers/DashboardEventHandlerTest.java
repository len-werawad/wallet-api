package com.lbk.socialbanking.dashboard.internal.handlers;

import com.lbk.socialbanking.account.api.AccountService;
import com.lbk.socialbanking.auth.events.UserLoggedInEvent;
import com.lbk.socialbanking.dashboard.internal.servcie.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardEventHandlerTest {

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @Mock
    private AccountService accountService;

    @Mock
    private DashboardService dashboardService;

    private DashboardEventHandler eventHandler;

    @BeforeEach
    void setUp() {
        when(cacheManager.getCache(anyString())).thenReturn(cache);
        eventHandler = new DashboardEventHandler(cacheManager, dashboardService);
    }

    @Test
    @DisplayName("Should handle user logged in event and invalidate cache then pre-warm")
    void shouldHandleUserLoggedInEvent() throws Exception {
        // Given
        String userId = "user123";
        String sessionId = "session456";
        String ipAddress = "127.0.0.1";
        String userAgent = "test-agent";
        LocalDateTime timestamp = LocalDateTime.now();

        UserLoggedInEvent event = new UserLoggedInEvent(userId, timestamp);

        // When
        eventHandler.handleUserLoggedIn(event);

        // Then
        // Allow some time for async processing
        Thread.sleep(100);

        // Verify cache eviction was called (only dashboardData cache)
        verify(cache, atLeastOnce()).evict(userId);

        // Verify dashboardService.getDashboard was called for pre-warming
        verify(dashboardService).getDashboard(userId);
    }

    @Test
    @DisplayName("Should handle events gracefully when cache operations fail")
    void shouldHandleEventsWhenCacheOperationsFail() throws Exception {
        when(cacheManager.getCache(anyString())).thenReturn(null);
        String userId = "user123";
        LocalDateTime timestamp = LocalDateTime.now();

        UserLoggedInEvent event = new UserLoggedInEvent(userId, timestamp);

        eventHandler.handleUserLoggedIn(event);

        // Should not throw exception even when cache is null
        Thread.sleep(100);

        verify(cacheManager, atLeastOnce()).getCache(anyString());
    }
}
