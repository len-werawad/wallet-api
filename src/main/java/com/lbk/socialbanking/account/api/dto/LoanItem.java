package com.lbk.socialbanking.account.api.dto;

public record LoanItem(
        String loanId,
        String name,
        String status,
        double outstandingAmount
) {
}
