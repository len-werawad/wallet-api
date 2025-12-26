package com.lbk.socialbanking.account.api.dto;

public record GoalItem(
        String goalId,
        String name,
        String status,
        String issuer,
        double amount
) {
}
