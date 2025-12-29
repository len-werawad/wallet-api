package com.lbk.socialbanking.account.internal.service;

import com.lbk.socialbanking.account.api.dto.AccountSummary;
import com.lbk.socialbanking.account.api.dto.PayeeItem;
import com.lbk.socialbanking.account.internal.persistence.entity.AccountBalanceEntity;
import com.lbk.socialbanking.account.internal.persistence.entity.AccountDetailEntity;
import com.lbk.socialbanking.account.internal.persistence.entity.AccountEntity;
import com.lbk.socialbanking.account.internal.persistence.entity.AccountFlagEntity;
import com.lbk.socialbanking.account.internal.persistence.repo.AccountBalanceRepository;
import com.lbk.socialbanking.account.internal.persistence.repo.AccountDetailRepository;
import com.lbk.socialbanking.account.internal.persistence.repo.AccountFlagRepository;
import com.lbk.socialbanking.account.internal.persistence.repo.AccountRepository;
import com.lbk.socialbanking.common.api.dto.PageRequest;
import com.lbk.socialbanking.transaction.api.TransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    private static final String USER_ID = "user123";
    private static final String FAVORITE = "FAVORITE";

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountBalanceRepository balanceRepository;

    @Mock
    private AccountDetailRepository detailRepository;

    @Mock
    private TransactionService transactionService;

    @Mock
    private AccountFlagRepository flagRepository;

    @InjectMocks
    private AccountServiceImpl accountService;


    @Nested
    @DisplayName("listAccounts method tests")
    class ListAccountsTests {

        @Test
        @DisplayName("should return list of accounts with balances and details")
        void listAccounts_success() {

            AccountEntity account1 = newAccount("acc-1", USER_ID, "SAVING", "THB", "123-456", "KBank");
            AccountEntity account2 = newAccount("acc-2", USER_ID, "GOAL", "THB", "789-012", "SCB");

            AccountBalanceEntity balance1 = newBalance("acc-1", USER_ID, bd("1000.50"));
            AccountBalanceEntity balance2 = newBalance("acc-2", USER_ID, bd("500.00"));

            AccountDetailEntity detail1 = newDetail("acc-1", USER_ID, "#FF5733");
            AccountDetailEntity detail2 = newDetail("acc-2", USER_ID, "#3357FF");

            when(accountRepository.findByUserId(USER_ID)).thenReturn(List.of(account1, account2));
            when(balanceRepository.findByUserId(USER_ID)).thenReturn(List.of(balance1, balance2));
            when(detailRepository.findByUserId(USER_ID)).thenReturn(List.of(detail1, detail2));

            List<AccountSummary> result = accountService.listAccounts(USER_ID);

            assertThat(result).hasSize(2);

            AccountSummary summary1 = result.getFirst();
            assertThat(summary1.accountId()).isEqualTo("acc-1");
            assertThat(summary1.type()).isEqualTo("SAVING");
            assertThat(summary1.currency()).isEqualTo("THB");
            assertThat(summary1.accountNumber()).isEqualTo("123-456");
            assertThat(summary1.issuer()).isEqualTo("KBank");
            assertThat(summary1.color()).isEqualTo("#FF5733");
            assertThat(summary1.amount()).isEqualTo(1000.50);

            AccountSummary summary2 = result.get(1);
            assertThat(summary2.accountId()).isEqualTo("acc-2");
            assertThat(summary2.type()).isEqualTo("GOAL");
            assertThat(summary2.color()).isEqualTo("#3357FF");
            assertThat(summary2.amount()).isEqualTo(500.00);
        }

        @Test
        @DisplayName("should return empty list when user has no accounts")
        void listAccounts_emptyList() {
            String userId = "user-no-accounts";

            when(accountRepository.findByUserId(userId)).thenReturn(List.of());
            when(balanceRepository.findByUserId(userId)).thenReturn(List.of());
            when(detailRepository.findByUserId(userId)).thenReturn(List.of());

            List<AccountSummary> result = accountService.listAccounts(userId);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should handle multiple accounts with partial data (missing balance or detail)")
        void listAccounts_partialData() {
            AccountEntity account1 = newAccount("acc-1", USER_ID, "SAVING", "THB", "123-456", "KBank");
            AccountEntity account2 = newAccount("acc-2", USER_ID, "GOAL", "THB", "789-012", "SCB");
            AccountEntity account3 = newAccount("acc-3", USER_ID, "LOAN", "THB", "555-666", "BBL");

            AccountBalanceEntity balance1 = newBalance("acc-1", USER_ID, bd("1000.00"));
            AccountBalanceEntity balance3 = newBalance("acc-3", USER_ID, bd("5000.00"));

            AccountDetailEntity detail2 = newDetail("acc-2", USER_ID, "#3357FF");

            when(accountRepository.findByUserId(USER_ID)).thenReturn(List.of(account1, account2, account3));
            when(balanceRepository.findByUserId(USER_ID)).thenReturn(List.of(balance1, balance3));
            when(detailRepository.findByUserId(USER_ID)).thenReturn(List.of(detail2));

            List<AccountSummary> result = accountService.listAccounts(USER_ID);

            assertThat(result).hasSize(3);

            assertThat(result.getFirst().accountId()).isEqualTo("acc-1");
            assertThat(result.getFirst().amount()).isEqualTo(1000.00);
            assertThat(result.getFirst().color()).isNull();

            assertThat(result.get(1).accountId()).isEqualTo("acc-2");
            assertThat(result.get(1).amount()).isEqualTo(0.0);
            assertThat(result.get(1).color()).isEqualTo("#3357FF");

            assertThat(result.get(2).accountId()).isEqualTo("acc-3");
            assertThat(result.get(2).amount()).isEqualTo(5000.00);
            assertThat(result.get(2).color()).isNull();
        }

        @Test
        @DisplayName("should return paginated accounts with balances and details")
        void listAccountsPaginated_success() {
            var pageRequest = new PageRequest(1, 10);

            AccountEntity account1 = newAccount("acc-1", USER_ID, "SAVING", "THB", "123-456", "KBank");
            AccountEntity account2 = newAccount("acc-2", USER_ID, "GOAL", "THB", "789-012", "SCB");

            AccountBalanceEntity balance1 = newBalance("acc-1", USER_ID, bd("1000.50"));
            AccountBalanceEntity balance2 = newBalance("acc-2", USER_ID, bd("500.00"));

            AccountDetailEntity detail1 = newDetail("acc-1", USER_ID, "#FF5733");
            AccountDetailEntity detail2 = newDetail("acc-2", USER_ID, "#3357FF");

            when(accountRepository.findByUserId(USER_ID, pageRequest.toPageable()))
                    .thenReturn(new org.springframework.data.domain.PageImpl<>(
                            List.of(account1, account2),
                            pageRequest.toPageable(),
                            2
                    ));
            when(balanceRepository.findByUserId(USER_ID)).thenReturn(List.of(balance1, balance2));
            when(detailRepository.findByUserId(USER_ID)).thenReturn(List.of(detail1, detail2));

            var result = accountService.listAccounts(USER_ID, pageRequest);

            assertThat(result.data()).hasSize(2);

            AccountSummary summary1 = result.data().getFirst();
            assertThat(summary1.accountId()).isEqualTo("acc-1");
            assertThat(summary1.type()).isEqualTo("SAVING");
            assertThat(summary1.color()).isEqualTo("#FF5733");
            assertThat(summary1.amount()).isEqualTo(1000.50);

            AccountSummary summary2 = result.data().get(1);
            assertThat(summary2.accountId()).isEqualTo("acc-2");
            assertThat(summary2.type()).isEqualTo("GOAL");
            assertThat(summary2.color()).isEqualTo("#3357FF");
            assertThat(summary2.amount()).isEqualTo(500.00);
        }

        @Test
        @DisplayName("should return empty data when user has no accounts (paginated)")
        void listAccountsPaginated_emptyData() {
            var pageRequest = new PageRequest(1, 10);
            String userId = "user-no-accounts";

            when(accountRepository.findByUserId(userId, pageRequest.toPageable()))
                    .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of()));
            when(balanceRepository.findByUserId(userId)).thenReturn(List.of());
            when(detailRepository.findByUserId(userId)).thenReturn(List.of());

            var result = accountService.listAccounts(userId, pageRequest);

            assertThat(result.data()).isEmpty();
        }

        @Test
        @DisplayName("should include correct pagination info")
        void listAccountsPaginated_paginationInfo() {
            var pageRequest = new PageRequest(2, 5);

            AccountEntity account1 = newAccount("acc-1", USER_ID, "SAVING", "THB", "123-456", "KBank");
            AccountEntity account2 = newAccount("acc-2", USER_ID, "GOAL", "THB", "789-012", "SCB");

            when(accountRepository.findByUserId(USER_ID, pageRequest.toPageable()))
                    .thenReturn(new org.springframework.data.domain.PageImpl<>(
                            List.of(account1, account2),
                            pageRequest.toPageable(),
                            15
                    ));
            when(balanceRepository.findByUserId(USER_ID)).thenReturn(List.of());
            when(detailRepository.findByUserId(USER_ID)).thenReturn(List.of());

            var result = accountService.listAccounts(USER_ID, pageRequest);

            assertThat(result.data()).hasSize(2);
            assertThat(result.pagination().page()).isEqualTo(2);
            assertThat(result.pagination().limit()).isEqualTo(5);
            assertThat(result.pagination().total()).isEqualTo(15);
            assertThat(result.pagination().totalPages()).isEqualTo(3);
        }

        @Test
        @DisplayName("should handle account with progress-based status (paginated)")
        void listAccountsPaginated_withProgressStatus() {
            var pageRequest = new PageRequest(1, 10);

            AccountEntity account1 = newAccount("acc-1", USER_ID, "GOAL", "THB", "123-456", "KBank");
            AccountDetailEntity detail1 = newDetailWithProgress("acc-1", USER_ID, "#FF5733", 75);

            when(accountRepository.findByUserId(USER_ID, pageRequest.toPageable()))
                    .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(account1)));
            when(balanceRepository.findByUserId(USER_ID)).thenReturn(List.of());
            when(detailRepository.findByUserId(USER_ID)).thenReturn(List.of(detail1));

            var result = accountService.listAccounts(USER_ID, pageRequest);

            assertThat(result.data()).hasSize(1);
            assertThat(result.data().getFirst().status()).isEqualTo("IN_PROGRESS");
        }
    }

    @Nested
    @DisplayName("listQuickPayees method tests")
    class ListQuickPayeesTests {

        @Test
        @DisplayName("should return payees with favorite flags")
        void listQuickPayees_withFavorites() {
            int limit = 5;

            TransactionService.TransactionSummary tx1 = new TransactionService.TransactionSummary("tx-1", "John Doe", "img1.png");
            TransactionService.TransactionSummary tx2 = new TransactionService.TransactionSummary("tx-2", "Jane Smith", "img2.png");
            TransactionService.TransactionSummary tx3 = new TransactionService.TransactionSummary("tx-3", "Bob Wilson", "img3.png");

            AccountFlagEntity flag1 = newFlag("tx-1", USER_ID, FAVORITE, "true");
            AccountFlagEntity flag3 = newFlag("tx-3", USER_ID, FAVORITE, "true");

            when(transactionService.listTransactionSummaries(USER_ID)).thenReturn(List.of(tx1, tx2, tx3));
            when(flagRepository.findByUserIdAndFlagType(USER_ID, FAVORITE)).thenReturn(List.of(flag1, flag3));

            List<PayeeItem> result = accountService.listQuickPayees(USER_ID, limit);

            assertThat(result).hasSize(3);

            assertThat(result.getFirst().payeeId()).isEqualTo("tx-1");
            assertThat(result.getFirst().name()).isEqualTo("John Doe");
            assertThat(result.getFirst().image()).isEqualTo("img1.png");
            assertThat(result.getFirst().favorite()).isTrue();

            assertThat(result.get(1).payeeId()).isEqualTo("tx-2");
            assertThat(result.get(1).favorite()).isFalse();

            assertThat(result.get(2).payeeId()).isEqualTo("tx-3");
            assertThat(result.get(2).favorite()).isTrue();
        }

        @Test
        @DisplayName("should limit payees to specified limit")
        void listQuickPayees_respectsLimit() {
            int limit = 2;

            TransactionService.TransactionSummary tx1 = new TransactionService.TransactionSummary("tx-1", "John Doe", "img1.png");
            TransactionService.TransactionSummary tx2 = new TransactionService.TransactionSummary("tx-2", "Jane Smith", "img2.png");
            TransactionService.TransactionSummary tx3 = new TransactionService.TransactionSummary("tx-3", "Bob Wilson", "img3.png");

            when(transactionService.listTransactionSummaries(USER_ID)).thenReturn(List.of(tx1, tx2, tx3));
            when(flagRepository.findByUserIdAndFlagType(USER_ID, FAVORITE)).thenReturn(List.of());

            List<PayeeItem> result = accountService.listQuickPayees(USER_ID, limit);

            assertThat(result).hasSize(2);
            assertThat(result.getFirst().payeeId()).isEqualTo("tx-1");
            assertThat(result.get(1).payeeId()).isEqualTo("tx-2");
        }

        @Test
        @DisplayName("should return empty list when no transactions")
        void listQuickPayees_noTransactions() {

            String userId = "user-no-tx";
            int limit = 10;

            when(transactionService.listTransactionSummaries(userId)).thenReturn(List.of());
            when(flagRepository.findByUserIdAndFlagType(userId, FAVORITE)).thenReturn(List.of());

            List<PayeeItem> result = accountService.listQuickPayees(userId, limit);

            assertThat(result).isEmpty();
        }

        @ParameterizedTest(name = "limit={0} should return correct number of payees")
        @ValueSource(ints = {1, 2, 5})
        @DisplayName("should respect different limit values")
        void listQuickPayees_respectsVariousLimits(int limit) {
            TransactionService.TransactionSummary tx1 = new TransactionService.TransactionSummary("tx-1", "John Doe", "img1.png");
            TransactionService.TransactionSummary tx2 = new TransactionService.TransactionSummary("tx-2", "Jane Smith", "img2.png");
            TransactionService.TransactionSummary tx3 = new TransactionService.TransactionSummary("tx-3", "Bob Wilson", "img3.png");

            when(transactionService.listTransactionSummaries(USER_ID)).thenReturn(List.of(tx1, tx2, tx3));
            when(flagRepository.findByUserIdAndFlagType(USER_ID, FAVORITE)).thenReturn(List.of());

            List<PayeeItem> result = accountService.listQuickPayees(USER_ID, limit);

            assertThat(result).hasSize(Math.min(limit, 3));
            assertThat(result).allMatch(p -> !p.favorite()); // no favorites set
        }

        @Test
        @DisplayName("should return paginated payees with favorite flags")
        void listQuickPayeesPaginated_withFavorites() {
            var pageRequest = new PageRequest(1, 5);

            TransactionService.TransactionSummary tx1 = new TransactionService.TransactionSummary("tx-1", "John Doe", "img1.png");
            TransactionService.TransactionSummary tx2 = new TransactionService.TransactionSummary("tx-2", "Jane Smith", "img2.png");
            TransactionService.TransactionSummary tx3 = new TransactionService.TransactionSummary("tx-3", "Bob Wilson", "img3.png");

            AccountFlagEntity flag1 = newFlag("tx-1", USER_ID, FAVORITE, "true");
            AccountFlagEntity flag3 = newFlag("tx-3", USER_ID, FAVORITE, "true");

            when(transactionService.listTransactionSummaries(USER_ID)).thenReturn(List.of(tx1, tx2, tx3));
            when(flagRepository.findByUserIdAndFlagType(USER_ID, FAVORITE)).thenReturn(List.of(flag1, flag3));

            var result = accountService.listQuickPayees(USER_ID, pageRequest);

            assertThat(result.data()).hasSize(3);

            assertThat(result.data().getFirst().payeeId()).isEqualTo("tx-1");
            assertThat(result.data().getFirst().name()).isEqualTo("John Doe");
            assertThat(result.data().getFirst().image()).isEqualTo("img1.png");
            assertThat(result.data().getFirst().favorite()).isTrue();

            assertThat(result.data().get(1).payeeId()).isEqualTo("tx-2");
            assertThat(result.data().get(1).favorite()).isFalse();

            assertThat(result.data().get(2).payeeId()).isEqualTo("tx-3");
            assertThat(result.data().get(2).favorite()).isTrue();
        }

        @Test
        @DisplayName("should paginate payees correctly")
        void listQuickPayeesPaginated_pagination() {
            var pageRequest = new PageRequest(2, 2);

            TransactionService.TransactionSummary tx1 = new TransactionService.TransactionSummary("tx-1", "John Doe", "img1.png");
            TransactionService.TransactionSummary tx2 = new TransactionService.TransactionSummary("tx-2", "Jane Smith", "img2.png");
            TransactionService.TransactionSummary tx3 = new TransactionService.TransactionSummary("tx-3", "Bob Wilson", "img3.png");
            TransactionService.TransactionSummary tx4 = new TransactionService.TransactionSummary("tx-4", "Alice Brown", "img4.png");

            when(transactionService.listTransactionSummaries(USER_ID)).thenReturn(List.of(tx1, tx2, tx3, tx4));
            when(flagRepository.findByUserIdAndFlagType(USER_ID, FAVORITE)).thenReturn(List.of());

            var result = accountService.listQuickPayees(USER_ID, pageRequest);

            assertThat(result.data()).hasSize(2);
            assertThat(result.data().getFirst().payeeId()).isEqualTo("tx-3");
            assertThat(result.data().get(1).payeeId()).isEqualTo("tx-4");
        }


        @Test
        @DisplayName("should include correct pagination info")
        void listQuickPayeesPaginated_paginationInfo() {
            var pageRequest = new PageRequest(2, 3);

            TransactionService.TransactionSummary tx1 = new TransactionService.TransactionSummary("tx-1", "John Doe", "img1.png");
            TransactionService.TransactionSummary tx2 = new TransactionService.TransactionSummary("tx-2", "Jane Smith", "img2.png");
            TransactionService.TransactionSummary tx3 = new TransactionService.TransactionSummary("tx-3", "Bob Wilson", "img3.png");
            TransactionService.TransactionSummary tx4 = new TransactionService.TransactionSummary("tx-4", "Alice Brown", "img4.png");
            TransactionService.TransactionSummary tx5 = new TransactionService.TransactionSummary("tx-5", "Charlie Green", "img5.png");
            TransactionService.TransactionSummary tx6 = new TransactionService.TransactionSummary("tx-6", "David Blue", "img6.png");
            TransactionService.TransactionSummary tx7 = new TransactionService.TransactionSummary("tx-7", "Emma Red", "img7.png");

            when(transactionService.listTransactionSummaries(USER_ID)).thenReturn(List.of(tx1, tx2, tx3, tx4, tx5, tx6, tx7));
            when(flagRepository.findByUserIdAndFlagType(USER_ID, FAVORITE)).thenReturn(List.of());

            var result = accountService.listQuickPayees(USER_ID, pageRequest);

            assertThat(result.data()).hasSize(3);
            assertThat(result.data().getFirst().payeeId()).isEqualTo("tx-4");
            assertThat(result.pagination().page()).isEqualTo(2);
            assertThat(result.pagination().limit()).isEqualTo(3);
            assertThat(result.pagination().total()).isEqualTo(7);
            assertThat(result.pagination().totalPages()).isEqualTo(3);
        }

        @Test
        @DisplayName("should handle last page with fewer items")
        void listQuickPayeesPaginated_lastPage() {
            var pageRequest = new PageRequest(3, 3);

            TransactionService.TransactionSummary tx1 = new TransactionService.TransactionSummary("tx-1", "John Doe", "img1.png");
            TransactionService.TransactionSummary tx2 = new TransactionService.TransactionSummary("tx-2", "Jane Smith", "img2.png");
            TransactionService.TransactionSummary tx3 = new TransactionService.TransactionSummary("tx-3", "Bob Wilson", "img3.png");
            TransactionService.TransactionSummary tx4 = new TransactionService.TransactionSummary("tx-4", "Alice Brown", "img4.png");
            TransactionService.TransactionSummary tx5 = new TransactionService.TransactionSummary("tx-5", "Charlie Green", "img5.png");
            TransactionService.TransactionSummary tx6 = new TransactionService.TransactionSummary("tx-6", "David Blue", "img6.png");
            TransactionService.TransactionSummary tx7 = new TransactionService.TransactionSummary("tx-7", "Emma Red", "img7.png");

            when(transactionService.listTransactionSummaries(USER_ID)).thenReturn(List.of(tx1, tx2, tx3, tx4, tx5, tx6, tx7));
            when(flagRepository.findByUserIdAndFlagType(USER_ID, FAVORITE)).thenReturn(List.of());

            var result = accountService.listQuickPayees(USER_ID, pageRequest);

            assertThat(result.data()).hasSize(1);
            assertThat(result.data().getFirst().payeeId()).isEqualTo("tx-7");
        }
    }

    @Nested
    @DisplayName("getBalancesByUserId method tests")
    class GetBalancesByUserIdTests {

        @Test
        @DisplayName("should return map of account balances")
        void getBalancesByUserId_success() {

            AccountBalanceEntity balance1 = newBalance("acc-1", USER_ID, bd("1000.50"));
            AccountBalanceEntity balance2 = newBalance("acc-2", USER_ID, bd("2500.75"));
            AccountBalanceEntity balance3 = newBalance("acc-3", USER_ID, bd("500.00"));

            when(balanceRepository.findByUserId(USER_ID)).thenReturn(List.of(balance1, balance2, balance3));


            Map<String, BigDecimal> result = accountService.getBalancesByUserId(USER_ID);


            assertThat(result).hasSize(3);
            assertThat(result.get("acc-1")).isEqualByComparingTo(bd("1000.50"));
            assertThat(result.get("acc-2")).isEqualByComparingTo(bd("2500.75"));
            assertThat(result.get("acc-3")).isEqualByComparingTo(bd("500.00"));
        }

        @Test
        @DisplayName("should return empty map when user has no balances")
        void getBalancesByUserId_emptyMap() {

            String userId = "user-no-balance";

            when(balanceRepository.findByUserId(userId)).thenReturn(List.of());


            Map<String, BigDecimal> result = accountService.getBalancesByUserId(userId);

            assertThat(result).isEmpty();
        }

        @ParameterizedTest(name = "balance={0} should be handled correctly")
        @CsvSource({
            "0, 0",
            "-500.00, -500.00",
            "9999.99, 9999.99"
        })
        @DisplayName("should handle various balance values (zero, negative, positive)")
        void getBalancesByUserId_variousBalances(String inputBalance, String expectedBalance) {
            AccountBalanceEntity balance = newBalance("acc-1", USER_ID, bd(inputBalance));

            when(balanceRepository.findByUserId(USER_ID)).thenReturn(List.of(balance));

            Map<String, BigDecimal> result = accountService.getBalancesByUserId(USER_ID);

            assertThat(result).hasSize(1);
            assertThat(result.get("acc-1")).isEqualByComparingTo(bd(expectedBalance));
        }
    }

    @Nested
    @DisplayName("listGoalAccounts method tests")
    class ListGoalAccountsTests {

        @Test
        @DisplayName("should return only GOAL accounts mapped to GoalItem with same pagination")
        void listGoalAccounts_success() {
            var pageRequest = new PageRequest(1, 20);

            AccountEntity goalAcc = newAccount("acc-goal", USER_ID, "GOAL", "THB", "999-111", "KBank");

            when(accountRepository.findByUserIdAndTypeIgnoreCase(USER_ID, "GOAL", pageRequest.toPageable()))
                    .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(goalAcc)));
            when(balanceRepository.findByUserId(USER_ID)).thenReturn(List.of());
            when(detailRepository.findByUserId(USER_ID)).thenReturn(List.of());

            var result = accountService.listGoalAccounts(USER_ID, pageRequest);

            assertThat(result.data()).hasSize(1);
        }

        @Test
        @DisplayName("should return empty data when no GOAL accounts")
        void listGoalAccounts_noGoals() {
            var pageRequest = new PageRequest(1, 20);

            when(accountRepository.findByUserIdAndTypeIgnoreCase(USER_ID, "GOAL", pageRequest.toPageable()))
                    .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of()));
            when(balanceRepository.findByUserId(USER_ID)).thenReturn(List.of());
            when(detailRepository.findByUserId(USER_ID)).thenReturn(List.of());

            var result = accountService.listGoalAccounts(USER_ID, pageRequest);

            assertThat(result.data()).isEmpty();
        }

        @Test
        @DisplayName("should map GOAL accounts with balance and details")
        void listGoalAccounts_withBalanceAndDetails() {
            var pageRequest = new PageRequest(1, 20);

            AccountEntity goalAcc = newAccount("acc-goal", USER_ID, "GOAL", "THB", "999-111", "KBank");
            AccountBalanceEntity balance = newBalance("acc-goal", USER_ID, bd("5000.00"));
            AccountDetailEntity detail = newDetailWithProgress("acc-goal", USER_ID, "#FF5733", 75);

            when(accountRepository.findByUserIdAndTypeIgnoreCase(USER_ID, "GOAL", pageRequest.toPageable()))
                    .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(goalAcc)));
            when(balanceRepository.findByUserId(USER_ID)).thenReturn(List.of(balance));
            when(detailRepository.findByUserId(USER_ID)).thenReturn(List.of(detail));

            var result = accountService.listGoalAccounts(USER_ID, pageRequest);

            assertThat(result.data()).hasSize(1);
            var goalItem = result.data().getFirst();
            assertThat(goalItem.goalId()).isEqualTo("acc-goal");
            assertThat(goalItem.name()).isEqualTo("999-111");
            assertThat(goalItem.issuer()).isEqualTo("KBank");
            assertThat(goalItem.amount()).isEqualTo(5000.00);
            assertThat(goalItem.status()).isEqualTo("IN_PROGRESS");
        }

        @ParameterizedTest(name = "progress={0} should map to status={1}")
        @CsvSource({
            "0, NOT_STARTED",
            "50, IN_PROGRESS",
            "100, COMPLETED",
            ", UNKNOWN"
        })
        @DisplayName("should map progress to correct status")
        void listGoalAccounts_statusMapping(Integer progress, String expectedStatus) {
            var pageRequest = new PageRequest(1, 20);

            AccountEntity goalAcc = newAccount("acc-goal", USER_ID, "GOAL", "THB", "999-111", "KBank");
            AccountDetailEntity detail = newDetailWithProgress("acc-goal", USER_ID, "#FF5733", progress);

            when(accountRepository.findByUserIdAndTypeIgnoreCase(USER_ID, "GOAL", pageRequest.toPageable()))
                    .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(goalAcc)));
            when(balanceRepository.findByUserId(USER_ID)).thenReturn(List.of());
            when(detailRepository.findByUserId(USER_ID)).thenReturn(List.of(detail));

            var result = accountService.listGoalAccounts(USER_ID, pageRequest);

            assertThat(result.data()).hasSize(1);
            assertThat(result.data().getFirst().status()).isEqualTo(expectedStatus);
        }

        @Test
        @DisplayName("should include pagination info")
        void listGoalAccounts_paginationInfo() {
            var pageRequest = new PageRequest(2, 5);

            AccountEntity goalAcc1 = newAccount("acc-goal-1", USER_ID, "GOAL", "THB", "999-111", "KBank");
            AccountEntity goalAcc2 = newAccount("acc-goal-2", USER_ID, "GOAL", "THB", "999-222", "SCB");

            when(accountRepository.findByUserIdAndTypeIgnoreCase(USER_ID, "GOAL", pageRequest.toPageable()))
                    .thenReturn(new org.springframework.data.domain.PageImpl<>(
                            List.of(goalAcc1, goalAcc2),
                            pageRequest.toPageable(),
                            12
                    ));
            when(balanceRepository.findByUserId(USER_ID)).thenReturn(List.of());
            when(detailRepository.findByUserId(USER_ID)).thenReturn(List.of());

            var result = accountService.listGoalAccounts(USER_ID, pageRequest);

            assertThat(result.data()).hasSize(2);
            assertThat(result.pagination().page()).isEqualTo(2);
            assertThat(result.pagination().limit()).isEqualTo(5);
            assertThat(result.pagination().total()).isEqualTo(12);
            assertThat(result.pagination().totalPages()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("listLoanAccounts method tests")
    class ListLoanAccountsTests {

        @Test
        @DisplayName("should return only LOAN accounts mapped to LoanItem with same pagination")
        void listLoanAccounts_success() {
            var pageRequest = new PageRequest(1, 20);

            AccountEntity loanAcc = newAccount("acc-loan", USER_ID, "LOAN", "THB", "555-666", "BBL");
            AccountBalanceEntity loanBalance = newBalance("acc-loan", USER_ID, bd("5000.00"));

            when(accountRepository.findByUserIdAndTypeIgnoreCase(USER_ID, "LOAN", pageRequest.toPageable()))
                    .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(loanAcc)));
            when(balanceRepository.findByUserId(USER_ID)).thenReturn(List.of(loanBalance));
            when(detailRepository.findByUserId(USER_ID)).thenReturn(List.of());

            var result = accountService.listLoanAccounts(USER_ID, pageRequest);

            assertThat(result.data()).hasSize(1);
        }

        @Test
        @DisplayName("should return empty data when no LOAN accounts")
        void listLoanAccounts_noLoans() {
            var pageRequest = new PageRequest(1, 20);

            when(accountRepository.findByUserIdAndTypeIgnoreCase(USER_ID, "LOAN", pageRequest.toPageable()))
                    .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of()));
            when(balanceRepository.findByUserId(USER_ID)).thenReturn(List.of());
            when(detailRepository.findByUserId(USER_ID)).thenReturn(List.of());

            var result = accountService.listLoanAccounts(USER_ID, pageRequest);

            assertThat(result.data()).isEmpty();
        }

        @Test
        @DisplayName("should map LOAN accounts with balance and details")
        void listLoanAccounts_withBalanceAndDetails() {
            var pageRequest = new PageRequest(1, 20);

            AccountEntity loanAcc = newAccount("acc-loan", USER_ID, "LOAN", "THB", "555-666", "BBL");
            AccountBalanceEntity balance = newBalance("acc-loan", USER_ID, bd("10000.00"));
            AccountDetailEntity detail = newDetailWithProgress("acc-loan", USER_ID, "#FF5733", 50);

            when(accountRepository.findByUserIdAndTypeIgnoreCase(USER_ID, "LOAN", pageRequest.toPageable()))
                    .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(loanAcc)));
            when(balanceRepository.findByUserId(USER_ID)).thenReturn(List.of(balance));
            when(detailRepository.findByUserId(USER_ID)).thenReturn(List.of(detail));

            var result = accountService.listLoanAccounts(USER_ID, pageRequest);

            assertThat(result.data()).hasSize(1);
            var loanItem = result.data().getFirst();
            assertThat(loanItem.loanId()).isEqualTo("acc-loan");
            assertThat(loanItem.name()).isEqualTo("555-666");
            assertThat(loanItem.outstandingAmount()).isEqualTo(10000.00);
            assertThat(loanItem.status()).isEqualTo("IN_PROGRESS");
        }

        @ParameterizedTest(name = "progress={0} should map to status={1}")
        @CsvSource({
            "0, NOT_STARTED",
            "50, IN_PROGRESS",
            "150, COMPLETED",
            ", UNKNOWN"
        })
        @DisplayName("should map progress to correct status")
        void listLoanAccounts_statusMapping(Integer progress, String expectedStatus) {
            var pageRequest = new PageRequest(1, 20);

            AccountEntity loanAcc = newAccount("acc-loan", USER_ID, "LOAN", "THB", "555-666", "BBL");
            AccountDetailEntity detail = newDetailWithProgress("acc-loan", USER_ID, "#FF5733", progress);

            when(accountRepository.findByUserIdAndTypeIgnoreCase(USER_ID, "LOAN", pageRequest.toPageable()))
                    .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(loanAcc)));
            when(balanceRepository.findByUserId(USER_ID)).thenReturn(List.of());
            when(detailRepository.findByUserId(USER_ID)).thenReturn(List.of(detail));

            var result = accountService.listLoanAccounts(USER_ID, pageRequest);

            assertThat(result.data()).hasSize(1);
            assertThat(result.data().getFirst().status()).isEqualTo(expectedStatus);
        }

        @Test
        @DisplayName("should map LOAN accounts with null status when no detail")
        void listLoanAccounts_nullStatus() {
            var pageRequest = new PageRequest(1, 20);

            AccountEntity loanAcc = newAccount("acc-loan", USER_ID, "LOAN", "THB", "555-666", "BBL");

            when(accountRepository.findByUserIdAndTypeIgnoreCase(USER_ID, "LOAN", pageRequest.toPageable()))
                    .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(loanAcc)));
            when(balanceRepository.findByUserId(USER_ID)).thenReturn(List.of());
            when(detailRepository.findByUserId(USER_ID)).thenReturn(List.of());

            var result = accountService.listLoanAccounts(USER_ID, pageRequest);

            assertThat(result.data()).hasSize(1);
            assertThat(result.data().getFirst().status()).isNull();
        }


        @Test
        @DisplayName("should include pagination info")
        void listLoanAccounts_paginationInfo() {
            var pageRequest = new PageRequest(1, 10);

            AccountEntity loanAcc1 = newAccount("acc-loan-1", USER_ID, "LOAN", "THB", "555-666", "BBL");
            AccountEntity loanAcc2 = newAccount("acc-loan-2", USER_ID, "LOAN", "THB", "777-888", "KBank");

            when(accountRepository.findByUserIdAndTypeIgnoreCase(USER_ID, "LOAN", pageRequest.toPageable()))
                    .thenReturn(new org.springframework.data.domain.PageImpl<>(
                            List.of(loanAcc1, loanAcc2),
                            pageRequest.toPageable(),
                            25
                    ));
            when(balanceRepository.findByUserId(USER_ID)).thenReturn(List.of());
            when(detailRepository.findByUserId(USER_ID)).thenReturn(List.of());

            var result = accountService.listLoanAccounts(USER_ID, pageRequest);

            assertThat(result.data()).hasSize(2);
            assertThat(result.pagination().page()).isEqualTo(1);
            assertThat(result.pagination().limit()).isEqualTo(10);
            assertThat(result.pagination().total()).isEqualTo(25);
            assertThat(result.pagination().totalPages()).isEqualTo(3);
        }
    }

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private static AccountEntity newAccount(String accountId, String userId, String type, String currency, String accountNumber, String issuer) {
        AccountEntity account = new AccountEntity();
        account.setAccountId(accountId);
        account.setUserId(userId);
        account.setType(type);
        account.setCurrency(currency);
        account.setAccountNumber(accountNumber);
        account.setIssuer(issuer);
        return account;
    }

    private static AccountBalanceEntity newBalance(String accountId, String userId, BigDecimal amount) {
        AccountBalanceEntity balance = new AccountBalanceEntity();
        balance.setAccountId(accountId);
        balance.setUserId(userId);
        balance.setAmount(amount);
        return balance;
    }

    private static AccountDetailEntity newDetail(String accountId, String userId, String color) {
        AccountDetailEntity detail = new AccountDetailEntity();
        detail.setAccountId(accountId);
        detail.setUserId(userId);
        detail.setColor(color);
        return detail;
    }

    private static AccountDetailEntity newDetailWithProgress(String accountId, String userId, String color, Integer progress) {
        AccountDetailEntity detail = new AccountDetailEntity();
        detail.setAccountId(accountId);
        detail.setUserId(userId);
        detail.setColor(color);
        detail.setProgress(progress);
        return detail;
    }

    private static AccountFlagEntity newFlag(String accountId, String userId, String flagType, String flagValue) {
        AccountFlagEntity flag = new AccountFlagEntity();
        flag.setAccountId(accountId);
        flag.setUserId(userId);
        flag.setFlagType(flagType);
        flag.setFlagValue(flagValue);
        return flag;
    }
}
