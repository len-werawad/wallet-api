package com.lbk.socialbanking.account.internal.persistence.repo;

import com.lbk.socialbanking.account.internal.persistence.entity.AccountBalanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountBalanceRepository extends JpaRepository<AccountBalanceEntity, String> {
    List<AccountBalanceEntity> findByUserId(String userId);
}

