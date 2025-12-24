package com.lbk.wallet.appconfig.web;

import java.util.Map;

public record AppConfigResponse(
        String environment,
        boolean maintenanceEnabled,
        String maintenanceMessage,
        Integer retryAfterSeconds,
        String minSupportedVersion,
        String latestVersion,
        boolean forceUpdate,
        String storeUrl,
        Map<String, Object> featureToggles,
        String serverTime
) {
}
