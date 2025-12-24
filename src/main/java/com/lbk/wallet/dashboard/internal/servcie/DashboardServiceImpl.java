package com.lbk.wallet.dashboard.internal.servcie;

import com.lbk.wallet.account.api.AccountService;
import com.lbk.wallet.account.api.dto.AccountSummary;
import com.lbk.wallet.account.api.dto.PayeeItem;
import com.lbk.wallet.customer.api.CustomerService;
import com.lbk.wallet.dashboard.web.dto.DashboardResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardServiceImpl.class);

    private final CustomerService customerService;
    private final AccountService accountsService;
    private final DelegatingSecurityContextExecutorService executorService;

    public DashboardServiceImpl(CustomerService customerService, AccountService accounts) {
        this.customerService = customerService;
        this.accountsService = accounts;
        this.executorService = new DelegatingSecurityContextExecutorService(
                Executors.newFixedThreadPool(3)
        );
    }

    @Cacheable(value = "dashboardData", key = "#userId", cacheManager = "dashboardCacheManager")
    public DashboardResponse getDashboard(String userId) {
        log.info("Fetching dashboard data for user: {}", userId);
        long startTime = System.currentTimeMillis();

        CompletableFuture<String> greetingFuture = CompletableFuture.supplyAsync(() -> {
            log.debug("Fetching greeting for user: {}", userId);
            return customerService.getGreeting(userId);
        }, executorService);

        CompletableFuture<List<AccountSummary>> accountsFuture =
                CompletableFuture.supplyAsync(() -> {
                    log.debug("Fetching accounts for user: {}", userId);
                    return accountsService.listAccounts(userId);
                }, executorService);

        CompletableFuture<List<PayeeItem>> payeesFuture =
                CompletableFuture.supplyAsync(() -> {
                    log.debug("Fetching quick payees for user: {}", userId);
                    return accountsService.listQuickPayees(userId, 10);
                }, executorService);

        CompletableFuture<Void> all = CompletableFuture.allOf(greetingFuture, accountsFuture, payeesFuture);

        all.join();

        String greeting = greetingFuture.join();
        var accountList = accountsFuture.join();
        var quickPayees = payeesFuture.join();

        var primary = accountList.stream()
                .filter(a -> "SAVING".equalsIgnoreCase(a.type()))
                .findFirst()
                .orElse(accountList.isEmpty() ? null : accountList.getFirst());

        var goals = accountList.stream()
                .filter(a -> "GOAL".equalsIgnoreCase(a.type()))
                .map(a -> new DashboardResponse.GoalCard(a.accountId(), a.accountNumber(), "IN_PROGRESS", a.amount()))
                .toList();

        var loans = accountList.stream()
                .filter(a -> "LOAN".equalsIgnoreCase(a.type()))
                .map(a -> new DashboardResponse.LoanCard(a.accountId(), "Credit Loan", "ACTIVE", a.amount()))
                .toList();

        long duration = System.currentTimeMillis() - startTime;
        log.info("Dashboard data retrieved for user: {} in {}ms - {} accounts, {} payees, {} goals, {} loans",
                userId, duration, accountList.size(), quickPayees.size(), goals.size(), loans.size());

        return new DashboardResponse(greeting, primary, accountList, quickPayees, goals, loans);
    }
}
