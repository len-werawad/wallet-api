package com.lbk.wallet.transaction.internal.service;

import com.lbk.wallet.transaction.events.TransactionCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class TransactionEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(TransactionEventPublisher.class);

    private final ApplicationEventPublisher eventPublisher;

    public TransactionEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Async("dashboardEventExecutor")
    public void publishTransactionCompleted(String transactionId, String userId, String fromAccountId,
                                            String toAccountId, BigDecimal amount, String currency,
                                            String transactionType, String description) {
        try {
            TransactionCompletedEvent event = new TransactionCompletedEvent(
                    transactionId,
                    userId,
                    fromAccountId,
                    toAccountId,
                    amount,
                    currency,
                    transactionType,
                    LocalDateTime.now(),
                    description
            );

            eventPublisher.publishEvent(event);

        } catch (Exception e) {
            log.error("Failed to publish transaction completed event for transaction: {}", transactionId, e);
        }
    }
}
