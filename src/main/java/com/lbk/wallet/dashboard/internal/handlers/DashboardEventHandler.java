package com.lbk.wallet.dashboard.internal.handlers;

import com.lbk.wallet.auth.events.UserLoggedInEvent;
import com.lbk.wallet.dashboard.internal.servcie.DashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class DashboardEventHandler {

    private static final String DASHBOARD_CACHE = "dashboardData";

    private final CacheManager cacheManager;
    private final DashboardService dashboardService;

    public DashboardEventHandler(@Qualifier("dashboardCacheManager") CacheManager cacheManager,
                                 DashboardService dashboardService) {
        this.cacheManager = cacheManager;
        this.dashboardService = dashboardService;
    }

    /**
     * Handle user login events - invalidate cache and pre-warm with getDashboard()
     */
    @EventListener
    @Async("dashboardEventExecutor")
    @Transactional
    public void handleUserLoggedIn(UserLoggedInEvent event) {
        log.info("User logged in - updating dashboard cache for user: {}", event.getUserId());

        try {
            invalidateDashboardCacheForUser(event.getUserId());

            preWarmDashboardCaches(event.getUserId());

            log.info("Successfully processed user login event for user: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Failed to handle user login event for user: {}", event.getUserId(), e);
        }
    }

    //TODO: Handle transaction completion events - update dashboard data
    //TODO: Handle account balance change events - update balance information

    /**
     * Cache invalidation logic - Clear only dashboardData cache that getDashboard() uses
     */
    private void invalidateDashboardCacheForUser(String userId) {
        try {
            log.debug("Invalidating dashboardData cache for user: {}", userId);

            evictUserCache(userId);

            log.debug("DashboardData cache invalidated for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to invalidate dashboard cache for user: {}", userId, e);
        }
    }

    /**
     * Pre-warm dashboard cache by calling getDashboard()
     * This ensures all dashboard data is cached and ready for quick access
     */
    private void preWarmDashboardCaches(String userId) {
        CompletableFuture.runAsync(() -> {
            try {
                log.debug("Pre-warming dashboard cache by calling getDashboard() for user: {}", userId);

                // Call getDashboard() to populate the cache with complete dashboard data
                dashboardService.getDashboard(userId);

                log.debug("Dashboard cache pre-warmed successfully for user: {}", userId);
            } catch (Exception e) {
                log.error("Failed to pre-warm dashboard cache for user: {}", userId, e);
            }
        });
    }

    private void evictUserCache(String userId) {
        try {
            Cache cache = cacheManager.getCache(DashboardEventHandler.DASHBOARD_CACHE);
            if (cache != null) {
                cache.evict(userId);
                log.trace("Evicted cache '{}' for user: {}", DashboardEventHandler.DASHBOARD_CACHE, userId);
            }
        } catch (Exception e) {
            log.warn("Failed to evict cache '{}' for user: {}", DashboardEventHandler.DASHBOARD_CACHE, userId, e);
        }
    }
}
