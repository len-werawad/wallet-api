package com.lbk.wallet.appconfig.internal.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "app_config",
        uniqueConstraints = @UniqueConstraint(name = "uk_app_config_env_version_platform", columnNames = {"environment", "app_version", "platform"}))
@Getter
@Setter
@NoArgsConstructor
public class AppConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "config_id")
    private Long configId;

    @Column(nullable = false)
    private String environment;

    @Column(name = "app_version")
    private String appVersion;

    private String platform;

    @Column(name = "maintenance_enabled", nullable = false)
    private boolean maintenanceEnabled;

    @Column(name = "maintenance_message")
    private String maintenanceMessage;

    @Column(name = "retry_after_seconds")
    private Integer retryAfterSeconds;

    @Column(name = "min_supported_version", nullable = false)
    private String minSupportedVersion;

    @Column(name = "latest_version", nullable = false)
    private String latestVersion;

    @Column(name = "force_update", nullable = false)
    private boolean forceUpdate;

    @Column(name = "store_url")
    private String storeUrl;

    @Column(name = "feature_toggles", columnDefinition = "json")
    private String featureTogglesJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
