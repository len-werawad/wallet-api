package com.lbk.wallet.account.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountBalanceChangedEvent(String accountId, String userId, BigDecimal previousBalance,
                                         BigDecimal newBalance, BigDecimal changeAmount, String currency,
                                         String changeReason, String relatedTransactionId, LocalDateTime timestamp) {
}
