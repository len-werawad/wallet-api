package com.lbk.socialbanking.appconfig.internal.service;

import com.lbk.socialbanking.appconfig.web.AppConfigResponse;

public interface AppConfigService {
    AppConfigResponse getConfig(String environment, String appVersion, String platform);
}
