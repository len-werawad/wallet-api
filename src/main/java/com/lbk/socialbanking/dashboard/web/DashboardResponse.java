package com.lbk.socialbanking.dashboard.web;

import com.lbk.socialbanking.account.api.dto.AccountSummary;
import com.lbk.socialbanking.account.api.dto.PayeeItem;

import java.util.List;

public record DashboardResponse(
        String greeting,
        AccountSummary primaryAccount,
        List<AccountSummary> accounts,
        List<PayeeItem> quickPayees,
        List<GoalCard> goals,
        List<LoanCard> loans
) {
    public record GoalCard(String id, String title, String status, double amount) {
    }

    public record LoanCard(String id, String title, String status, double outstandingAmount) {
    }
}
