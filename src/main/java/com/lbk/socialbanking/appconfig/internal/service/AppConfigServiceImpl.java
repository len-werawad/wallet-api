package com.lbk.socialbanking.appconfig.internal.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lbk.socialbanking.appconfig.internal.persistence.repo.AppConfigRepository;
import com.lbk.socialbanking.appconfig.web.AppConfigResponse;
import com.lbk.socialbanking.common.api.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
class AppConfigServiceImpl implements AppConfigService {

    private static final Logger log = LoggerFactory.getLogger(AppConfigServiceImpl.class);

    private final AppConfigRepository repo;
    private final ObjectMapper mapper = new ObjectMapper();

    AppConfigServiceImpl(AppConfigRepository repo) {
        this.repo = repo;
    }

    /**
     * Get app configuration by app version and platform.
     * Have no TTL for now, Invalidate cache on update this record resource action. like admin portal request update resource.
     *
     * @param environment
     * @param appVersion
     * @param platform
     * @return AppConfigResponse
     */
    @Override
    @Cacheable(cacheNames = "appConfig", key = "#environment + ':' + #appVersion + ':' + #platform")
    public AppConfigResponse getConfig(String environment, String appVersion, String platform) {
        log.info("Fetching app config for environment: {}, appVersion: {}, platform: {}",
                environment, appVersion, platform);

        var entity = repo.findTopByEnvironmentAndAppVersionAndPlatformOrderByUpdatedAtDesc(environment, appVersion, platform)
                .orElseThrow(() -> {
                    log.warn("App config not found for environment: {}, appVersion: {}, platform: {}",
                            environment, appVersion, platform);
                    return new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "App config not found");
                });

        Map<String, Object> toggles = Map.of();
        try {
            if (entity.getFeatureTogglesJson() != null) {
                toggles = mapper.readValue(entity.getFeatureTogglesJson(), new TypeReference<>() {
                });
                log.debug("Parsed {} feature toggles", toggles.size());
            }
        } catch (Exception ex) {
            log.error("Failed to parse feature toggles JSON for environment: {}, appVersion: {}, platform: {}",
                    environment, appVersion, platform, ex);
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "App config not found");
        }

        var response = new AppConfigResponse(
                entity.getEnvironment(),
                entity.isMaintenanceEnabled(),
                entity.getMaintenanceMessage(),
                entity.getRetryAfterSeconds(),
                entity.getMinSupportedVersion(),
                entity.getLatestVersion(),
                entity.isForceUpdate(),
                entity.getStoreUrl(),
                toggles,
                java.time.OffsetDateTime.now().toString()
        );

        log.info("App config retrieved successfully for environment: {}, appVersion: {}, platform: {} - maintenance: {}",
                environment, appVersion, platform, entity.isMaintenanceEnabled());

        return response;
    }
}
