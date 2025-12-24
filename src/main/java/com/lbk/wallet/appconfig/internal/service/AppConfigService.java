package com.lbk.wallet.appconfig.internal.service;

import com.lbk.wallet.appconfig.web.AppConfigResponse;

public interface AppConfigService {
    AppConfigResponse getConfig(String environment, String appVersion, String platform);
}
