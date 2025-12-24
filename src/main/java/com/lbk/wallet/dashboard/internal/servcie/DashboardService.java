package com.lbk.wallet.dashboard.internal.servcie;

import com.lbk.wallet.dashboard.web.dto.DashboardResponse;

public interface DashboardService {
    DashboardResponse getDashboard(String userId);
}
