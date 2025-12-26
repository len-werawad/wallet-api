package com.lbk.socialbanking.account.api;

import com.lbk.socialbanking.account.api.dto.AccountSummary;
import com.lbk.socialbanking.account.api.dto.GoalItem;
import com.lbk.socialbanking.account.api.dto.LoanItem;
import com.lbk.socialbanking.account.api.dto.PayeeItem;
import com.lbk.socialbanking.common.api.dto.PageRequest;
import com.lbk.socialbanking.common.api.dto.PaginatedResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * AccountService defines the operations related to user accounts,
 * including listing accounts, goal accounts, loan accounts, quick payees,
 * and retrieving account balances.
 */
public interface AccountService {

    /**
     * List all account summaries for a given user.
     *
     * @param userId the ID of the user
     * @return a list of AccountSummary objects
     */
    List<AccountSummary> listAccounts(String userId);

    /**
     * List account summaries for a given user with pagination.
     *
     * @param userId      the ID of the user
     * @param pageRequest pagination details
     * @return a paginated response of AccountSummary objects
     */
    PaginatedResponse<AccountSummary> listAccounts(String userId, PageRequest pageRequest);

    /**
     * List goal accounts for a given user with pagination.
     *
     * @param userId      the ID of the user
     * @param pageRequest pagination details
     * @return a paginated response of GoalItem objects
     */
    PaginatedResponse<GoalItem> listGoalAccounts(String userId, PageRequest pageRequest);

    /**
     * List loan accounts for a given user with pagination.
     *
     * @param userId      the ID of the user
     * @param pageRequest pagination details
     * @return a paginated response of LoanItem objects
     */
    PaginatedResponse<LoanItem> listLoanAccounts(String userId, PageRequest pageRequest);

    /**
     * List quick payees for a given user up to a specified limit.
     *
     * @param userId the ID of the user
     * @param limit  the maximum number of payees to return
     * @return a list of PayeeItem objects
     */
    List<PayeeItem> listQuickPayees(String userId, int limit);

    /**
     * List quick payees for a given user with pagination.
     *
     * @param userId      the ID of the user
     * @param pageRequest pagination details
     * @return a paginated response of PayeeItem objects
     */
    PaginatedResponse<PayeeItem> listQuickPayees(String userId, PageRequest pageRequest);

    /**
     * Get account balances for a given user.
     *
     * @param userId the ID of the user
     * @return a map of account IDs to their respective balances
     */
    Map<String, BigDecimal> getBalancesByUserId(String userId);
}
