package com.lbk.wallet.appconfig.internal.persistence.repo;

import com.lbk.wallet.appconfig.internal.persistence.entity.AppConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppConfigRepository extends JpaRepository<AppConfigEntity, Long> {
    Optional<AppConfigEntity> findTopByEnvironmentAndAppVersionAndPlatformOrderByUpdatedAtDesc(String environment, String appVersion, String platform);
}
