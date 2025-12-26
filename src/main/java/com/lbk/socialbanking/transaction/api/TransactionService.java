package com.lbk.socialbanking.transaction.api;

import java.util.List;

/**
 * TransactionService defines the operations related to user transactions,
 * including listing transactions and transaction summaries.
 */
public interface TransactionService {

    /**
     * List transactions for a given user and account with pagination.
     *
     * @param userId    the ID of the user
     * @param accountId the ID of the account
     * @param cursor    the pagination cursor
     * @param limit     the maximum number of transactions to return
     * @return a page of TransactionItem objects along with the next cursor
     */
    TransactionsPage listTransactions(String userId, String accountId, String cursor, int limit);

    /**
     * List transaction summaries for a given user.
     *
     * @param userId the ID of the user
     * @return a list of TransactionSummary objects
     */
    List<TransactionSummary> listTransactionSummaries(String userId);

    record TransactionsPage(List<TransactionItem> items, String nextCursor) {
    }

    record TransactionItem(String transactionId, String name, String image, Boolean isBank) {
    }

    record TransactionSummary(String transactionId, String name, String image) {
    }
}
