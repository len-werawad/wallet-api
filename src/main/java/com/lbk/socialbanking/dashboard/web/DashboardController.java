package com.lbk.socialbanking.dashboard.web;

import com.lbk.socialbanking.dashboard.internal.servcie.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirement(name = "ฺBearer Token")
@Tag(name = "Dashboards", description = "Endpoints for retrieving user dashboard information")
@RestController
@RequestMapping("/v1/dashboards")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Operation(summary = "Get dashboard data", description = "Aggregate greeting, accounts, payees, goals, and loans data for landing page")
    @GetMapping
    public DashboardResponse get(Authentication auth) {
        return dashboardService.getDashboard(auth.getName());
    }
}
