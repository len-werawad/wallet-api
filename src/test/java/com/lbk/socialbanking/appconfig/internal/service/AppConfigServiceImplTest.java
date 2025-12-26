package com.lbk.socialbanking.appconfig.internal.service;

import com.lbk.socialbanking.appconfig.internal.persistence.entity.AppConfigEntity;
import com.lbk.socialbanking.appconfig.internal.persistence.repo.AppConfigRepository;
import com.lbk.socialbanking.common.api.ApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppConfigServiceImplTest {

    @Mock
    private AppConfigRepository repo;

    @InjectMocks
    private AppConfigServiceImpl service;

    @Nonnull
    private static AppConfigEntity createAppConfigEntity() {
        AppConfigEntity entity = new AppConfigEntity();
        entity.setEnvironment("prod");
        entity.setAppVersion("1.1.0");
        entity.setPlatform("ios");
        entity.setMaintenanceEnabled(true);
        entity.setMaintenanceMessage("maint");
        entity.setRetryAfterSeconds(60);
        entity.setMinSupportedVersion("1.0.0");
        entity.setLatestVersion("1.2.0");
        entity.setForceUpdate(false);
        entity.setStoreUrl("https://store");
        entity.setFeatureTogglesJson("{\"featureA\":true}");
        return entity;
    }

    @Test
    @DisplayName("should return response when config found")
    void getConfig_success() {
        AppConfigEntity entity = createAppConfigEntity();

        when(repo.findTopByEnvironmentAndAppVersionAndPlatformOrderByUpdatedAtDesc("prod", "1.1.0", "ios"))
                .thenReturn(Optional.of(entity));

        var response = service.getConfig("prod", "1.1.0", "ios");

        assertThat(response.environment()).isEqualTo("prod");
        assertThat(response.maintenanceEnabled()).isTrue();
        assertThat(response.maintenanceMessage()).isEqualTo("maint");
        assertThat(response.retryAfterSeconds()).isEqualTo(60);
        assertThat(response.minSupportedVersion()).isEqualTo("1.0.0");
        assertThat(response.latestVersion()).isEqualTo("1.2.0");
        assertThat(response.forceUpdate()).isFalse();
        assertThat(response.storeUrl()).isEqualTo("https://store");
        assertThat(response.featureToggles()).isEqualTo(Map.of("featureA", true));
        assertThat(response.serverTime()).isNotBlank();
    }

    @Test
    @DisplayName("should throw when config missing")
    void getConfig_notFound() {
        when(repo.findTopByEnvironmentAndAppVersionAndPlatformOrderByUpdatedAtDesc("prod", "1.1.0", "ios"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getConfig("prod", "1.1.0", "ios"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("App config not found");
    }

    @Test
    @DisplayName("should throw when toggles JSON invalid")
    void getConfig_invalidJson() {
        AppConfigEntity entity = mock(AppConfigEntity.class);
        when(entity.getFeatureTogglesJson()).thenReturn("not-json");
        when(repo.findTopByEnvironmentAndAppVersionAndPlatformOrderByUpdatedAtDesc("prod", "1.1.0", "ios"))
                .thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> service.getConfig("prod", "1.1.0", "ios"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("App config not found");
    }

    @Test
    @DisplayName("should handle null feature toggles")
    void getConfig_nullFeatureToggles() {
        AppConfigEntity entity = mock(AppConfigEntity.class);
        when(entity.getFeatureTogglesJson()).thenReturn(null);
        when(entity.getEnvironment()).thenReturn("prod");
        when(repo.findTopByEnvironmentAndAppVersionAndPlatformOrderByUpdatedAtDesc("prod", "1.1.0", "ios"))
                .thenReturn(Optional.of(entity));

        var response = service.getConfig("prod", "1.1.0", "ios");

        assertThat(response.featureToggles()).isEmpty();
    }
}
