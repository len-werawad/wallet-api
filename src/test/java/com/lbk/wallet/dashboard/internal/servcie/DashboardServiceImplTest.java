package com.lbk.wallet.dashboard.internal.servcie;

import com.lbk.wallet.account.api.AccountService;
import com.lbk.wallet.account.api.dto.AccountSummary;
import com.lbk.wallet.account.api.dto.PayeeItem;
import com.lbk.wallet.dashboard.web.dto.DashboardResponse;
import com.lbk.wallet.customer.api.CustomerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private CustomerService customerService;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Nested
    @DisplayName("getDashboard")
    class GetDashboard {

        @Test
        @DisplayName("should build dashboard with primary saving, goals, loans and quick payees")
        void getDashboard_fullData() {
            String userId = "u1";
            when(customerService.getGreeting(userId)).thenReturn("Hello John");

            var acc1 = new AccountSummary("acc-saving", "SAVING", "THB", "123-456", "KBank", "#111", 1000.0, "ACTIVE");
            var acc2 = new AccountSummary("acc-goal", "GOAL", "THB", "999-111", "KBank", "#222", 200.0, "IN_PROGRESS");
            var acc3 = new AccountSummary("acc-loan", "LOAN", "THB", "555-666", "KBank", "#333", 5000.0, "ACTIVE");
            when(accountService.listAccounts(userId)).thenReturn(List.of(acc1, acc2, acc3));

            var p1 = new PayeeItem("p1", "Alice", "img1", true);
            var p2 = new PayeeItem("p2", "Bob", "img2", false);
            when(accountService.listQuickPayees(userId, 10)).thenReturn(List.of(p1, p2));

            DashboardResponse response = dashboardService.getDashboard(userId);

            assertThat(response.greeting()).isEqualTo("Hello John");
            assertThat(response.primaryAccount()).isNotNull();
            assertThat(response.primaryAccount().accountId()).isEqualTo("acc-saving");

            assertThat(response.accounts()).hasSize(3);
            assertThat(response.accounts().getFirst().accountId()).isEqualTo("acc-saving");

            assertThat(response.quickPayees()).hasSize(2);
            assertThat(response.quickPayees().getFirst().payeeId()).isEqualTo("p1");

            assertThat(response.goals()).hasSize(1);
            DashboardResponse.GoalCard goal = response.goals().getFirst();
            assertThat(goal.id()).isEqualTo("acc-goal");
            assertThat(goal.title()).isEqualTo("999-111");
            assertThat(goal.status()).isEqualTo("IN_PROGRESS");
            assertThat(goal.amount()).isEqualTo(200.0);

            assertThat(response.loans()).hasSize(1);
            DashboardResponse.LoanCard loan = response.loans().getFirst();
            assertThat(loan.id()).isEqualTo("acc-loan");
            assertThat(loan.title()).isEqualTo("Credit Loan");
            assertThat(loan.status()).isEqualTo("ACTIVE");
            assertThat(loan.outstandingAmount()).isEqualTo(5000.0);
        }

        @Test
        @DisplayName("should handle no accounts")
        void getDashboard_noAccounts() {
            String userId = "u2";
            when(customerService.getGreeting(userId)).thenReturn("Hi");
            when(accountService.listAccounts(userId)).thenReturn(List.of());
            when(accountService.listQuickPayees(userId, 10)).thenReturn(List.of());

            DashboardResponse response = dashboardService.getDashboard(userId);

            assertThat(response.greeting()).isEqualTo("Hi");
            assertThat(response.primaryAccount()).isNull();
            assertThat(response.accounts()).isEmpty();
            assertThat(response.goals()).isEmpty();
            assertThat(response.loans()).isEmpty();
            assertThat(response.quickPayees()).isEmpty();
        }

        @Test
        @DisplayName("should pick first account as primary when no saving type")
        void getDashboard_noSavingType() {
            String userId = "u3";
            when(customerService.getGreeting(userId)).thenReturn("Hi");

            var acc1 = new AccountSummary("acc-1", "GOAL", "THB", "111-222", "KBank", "#111", 100.0, "IN_PROGRESS");
            var acc2 = new AccountSummary("acc-2", "LOAN", "THB", "333-444", "KBank", "#222", 200.0, "ACTIVE");
            when(accountService.listAccounts(userId)).thenReturn(List.of(acc1, acc2));
            when(accountService.listQuickPayees(userId, 10)).thenReturn(List.of());

            DashboardResponse response = dashboardService.getDashboard(userId);

            assertThat(response.primaryAccount()).isNotNull();
            assertThat(response.primaryAccount().accountId()).isEqualTo("acc-1");
            assertThat(response.goals()).hasSize(1);
            assertThat(response.loans()).hasSize(1);
        }

        @Test
        @DisplayName("should filter multiple goals and loans correctly")
        void getDashboard_multipleGoalsAndLoans() {
            String userId = "u4";
            when(customerService.getGreeting(userId)).thenReturn("Yo");

            var acc1 = new AccountSummary("acc-saving", "SAVING", "THB", "000-000", "KBank", "#000", 10.0, "ACTIVE");
            var acc2 = new AccountSummary("goal-1", "GOAL", "THB", "111-111", "KBank", "#111", 100.0, "IN_PROGRESS");
            var acc3 = new AccountSummary("goal-2", "GOAL", "THB", "222-222", "KBank", "#222", 200.0, "COMPLETED");
            var acc4 = new AccountSummary("loan-1", "LOAN", "THB", "333-333", "KBank", "#333", 300.0, "ACTIVE");
            var acc5 = new AccountSummary("loan-2", "LOAN", "THB", "444-444", "KBank", "#444", 400.0, "ACTIVE");
            when(accountService.listAccounts(userId)).thenReturn(List.of(acc1, acc2, acc3, acc4, acc5));
            when(accountService.listQuickPayees(userId, 10)).thenReturn(List.of());

            DashboardResponse response = dashboardService.getDashboard(userId);

            assertThat(response.goals()).hasSize(2);
            assertThat(response.loans()).hasSize(2);
        }
    }
}

