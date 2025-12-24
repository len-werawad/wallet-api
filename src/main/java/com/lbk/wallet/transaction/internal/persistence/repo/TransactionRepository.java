package com.lbk.wallet.transaction.internal.persistence.repo;

import com.lbk.wallet.transaction.internal.persistence.entity.TransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<TransactionEntity, String> {
    List<TransactionEntity> findByUserIdOrderByTransactionIdAsc(String userId);

    Page<TransactionEntity> findByUserIdOrderByTransactionIdAsc(String userId, Pageable pageable);
}
