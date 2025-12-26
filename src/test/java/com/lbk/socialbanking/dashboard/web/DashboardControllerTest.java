package com.lbk.socialbanking.dashboard.web;

import com.lbk.socialbanking.account.api.dto.AccountSummary;
import com.lbk.socialbanking.account.api.dto.PayeeItem;
import com.lbk.socialbanking.common.api.JwtService;
import com.lbk.socialbanking.dashboard.internal.servcie.DashboardService;
import com.lbk.socialbanking.dashboard.web.dto.DashboardResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private DashboardService dashboardService;

    @Nested
    @DisplayName("GET /v1/dashboards")
    class GetDashboard {

        @Test
        @WithMockUser(username = "u1")
        @DisplayName("should return dashboard data for authenticated user")
        void getDashboard_success() throws Exception {
            String userId = "u1";
            var primary = new AccountSummary("acc-1", "SAVING", "THB", "123-456", "KBank", "#111", 1000.0, "ACTIVE");
            var acc2 = new AccountSummary("acc-2", "GOAL", "THB", "999-111", "KBank", "#222", 200.0, "ACTIVE");
            var p1 = new PayeeItem("p1", "Alice", "img1", true);
            var p2 = new PayeeItem("p2", "Bob", "img2", false);
            var goal1 = new DashboardResponse.GoalCard("g1", "Trip", "IN_PROGRESS", 100.0);
            var loan1 = new DashboardResponse.LoanCard("l1", "Loan", "ACTIVE", 5000.0);
            var response = new DashboardResponse(
                    "Hello u1",
                    primary,
                    List.of(primary, acc2),
                    List.of(p1, p2),
                    List.of(goal1),
                    List.of(loan1)
            );

            given(dashboardService.getDashboard(userId)).willReturn(response);

            mockMvc.perform(get("/v1/dashboards"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data.greeting").value("Hello u1"))
                    .andExpect(jsonPath("$.data.primaryAccount.accountId").value("acc-1"))
                    .andExpect(jsonPath("$.data.accounts.length()").value(2))
                    .andExpect(jsonPath("$.data.quickPayees.length()").value(2))
                    .andExpect(jsonPath("$.data.goals.length()").value(1))
                    .andExpect(jsonPath("$.data.loans.length()").value(1));
        }

        @Test
        @DisplayName("should return 401 when user not authenticated")
        void getDashboard_unauthenticated() throws Exception {
            mockMvc.perform(get("/v1/dashboards"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
