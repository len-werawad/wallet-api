package com.lbk.socialbanking.transaction.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionCompletedEvent(String transactionId, String userId, String fromAccountId, String toAccountId,
                                        BigDecimal amount, String currency, String transactionType,
                                        LocalDateTime timestamp, String description) {
}
