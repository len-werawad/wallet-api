package com.lbk.socialbanking.dashboard.internal.servcie;

import com.lbk.socialbanking.dashboard.web.DashboardResponse;

public interface DashboardService {
    DashboardResponse getDashboard(String userId);
}
