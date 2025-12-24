package com.lbk.wallet.appconfig.web;

import com.lbk.wallet.appconfig.internal.service.AppConfigService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/apps")
@Validated
@Tag(name = "Application Config", description = "Endpoints for retrieving application configuration settings")
public class AppConfigController {

    private final AppConfigService appConfigService;

    public AppConfigController(AppConfigService appConfigService) {
        this.appConfigService = appConfigService;
    }

    @GetMapping("/config")
    public AppConfigResponse getConfig(
            @RequestParam @NotBlank(message = "environment is required") String environment,
            @RequestParam @NotBlank(message = "app version is required") String appVersion,
            @RequestParam @NotBlank(message = "platform is required") String platform
    ) {
        return appConfigService.getConfig(environment, appVersion, platform);
    }
}
