package com.lbk.socialbanking.transaction.internal.service;

import com.lbk.socialbanking.transaction.api.TransactionService;
import com.lbk.socialbanking.transaction.internal.persistence.entity.TransactionEntity;
import com.lbk.socialbanking.transaction.internal.persistence.repo.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Nested
    @DisplayName("listTransactions")
    class ListTransactions {

        @Test
        @DisplayName("should return first page with next cursor")
        void listTransactions_firstPage() {
            String userId = "u1";

            TransactionEntity t1 = createTransactionEntity("tx-1", userId, "T1", "img1", true);
            TransactionEntity t2 = createTransactionEntity("tx-2", userId, "T2", "img2", false);
            TransactionEntity t3 = createTransactionEntity("tx-3", userId, "T3", "img3", true);
            when(transactionRepository.findByUserIdOrderByTransactionIdAsc(userId))
                    .thenReturn(List.of(t1, t2, t3));

            TransactionService.TransactionsPage page = transactionService.listTransactions(userId, "acc-1", null, 2);

            assertThat(page.items()).hasSize(2);
            assertThat(page.items().get(0).transactionId()).isEqualTo("tx-1");
            assertThat(page.items().get(1).transactionId()).isEqualTo("tx-2");
            assertThat(page.nextCursor()).isNotNull();
        }

        @Test
        @DisplayName("should return second page and null next cursor when at end")
        void listTransactions_secondPage_end() {
            String userId = "u1";

            TransactionEntity t1 = createTransactionEntity("tx-1", userId, "T1", "img1", true);
            TransactionEntity t2 = createTransactionEntity("tx-2", userId, "T2", "img2", false);
            TransactionEntity t3 = createTransactionEntity("tx-3", userId, "T3", "img3", true);
            when(transactionRepository.findByUserIdOrderByTransactionIdAsc(userId))
                    .thenReturn(List.of(t1, t2, t3));

            TransactionService.TransactionsPage first = transactionService.listTransactions(userId, "acc-1", null, 2);
            String cursor = first.nextCursor();

            TransactionService.TransactionsPage second = transactionService.listTransactions(userId, "acc-1", cursor, 2);

            assertThat(second.items()).hasSize(1);
            assertThat(second.items().getFirst().transactionId()).isEqualTo("tx-3");
            assertThat(second.nextCursor()).isNull();
        }

        @Test
        @DisplayName("should handle cursor beyond list size")
        void listTransactions_cursorBeyondSize() {
            String userId = "u1";

            TransactionEntity t1 = createTransactionEntity("tx-1", userId, "T1", "img1", true);
            when(transactionRepository.findByUserIdOrderByTransactionIdAsc(userId))
                    .thenReturn(List.of(t1));

            String cursor = encodeOffset();
            TransactionService.TransactionsPage page = transactionService.listTransactions(userId, "acc-1", cursor, 10);

            assertThat(page.items()).isEmpty();
            assertThat(page.nextCursor()).isNull();
        }

        @Test
        @DisplayName("should treat invalid cursor as start from 0")
        void listTransactions_invalidCursor() {
            String userId = "u1";

            TransactionEntity t1 = createTransactionEntity("tx-1", userId, "T1", "img1", true);
            TransactionEntity t2 = createTransactionEntity("tx-2", userId, "T2", "img2", false);
            when(transactionRepository.findByUserIdOrderByTransactionIdAsc(userId))
                    .thenReturn(List.of(t1, t2));

            TransactionService.TransactionsPage page = transactionService.listTransactions(userId, "acc-1", "invalid", 10);

            assertThat(page.items()).hasSize(2);
            assertThat(page.items().getFirst().transactionId()).isEqualTo("tx-1");
        }

        @Test
        @DisplayName("should return empty list and null cursor when no transactions")
        void listTransactions_empty() {
            String userId = "u-empty";
            when(transactionRepository.findByUserIdOrderByTransactionIdAsc(userId))
                    .thenReturn(List.of());

            TransactionService.TransactionsPage page = transactionService.listTransactions(userId, "acc-1", null, 10);

            assertThat(page.items()).isEmpty();
            assertThat(page.nextCursor()).isNull();
        }
    }

    @Nested
    @DisplayName("listTransactionSummaries")
    class ListTransactionSummaries {

        @Test
        @DisplayName("should map entities to summaries")
        void listTransactionSummaries_success() {
            String userId = "u1";

            TransactionEntity t1 = createTransactionEntity("tx-1", userId, "T1", "img1", true);
            TransactionEntity t2 = createTransactionEntity("tx-2", userId, "T2", "img2", false);
            when(transactionRepository.findByUserIdOrderByTransactionIdAsc(userId))
                    .thenReturn(List.of(t1, t2));

            List<TransactionService.TransactionSummary> result = transactionService.listTransactionSummaries(userId);

            assertThat(result).hasSize(2);
            assertThat(result.getFirst().transactionId()).isEqualTo("tx-1");
            assertThat(result.getFirst().name()).isEqualTo("T1");
            assertThat(result.getFirst().image()).isEqualTo("img1");
        }

        @Test
        @DisplayName("should return empty list when no transactions")
        void listTransactionSummaries_empty() {
            String userId = "u-empty";
            when(transactionRepository.findByUserIdOrderByTransactionIdAsc(userId))
                    .thenReturn(List.of());

            List<TransactionService.TransactionSummary> result = transactionService.listTransactionSummaries(userId);

            assertThat(result).isEmpty();
        }
    }

    private TransactionEntity createTransactionEntity(String id, String userId, String name, String image, Boolean isBank) {
        TransactionEntity e = new TransactionEntity();
        e.setTransactionId(id);
        e.setUserId(userId);
        e.setName(name);
        e.setImage(image);
        e.setIsBank(isBank);
        return e;
    }

    private String encodeOffset() {
        return java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString(("o:" + 5).getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
