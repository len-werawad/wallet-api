package com.lbk.wallet.account.internal.persistence.repo;

import com.lbk.wallet.account.internal.persistence.entity.AccountDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountDetailRepository extends JpaRepository<AccountDetailEntity, String> {
    List<AccountDetailEntity> findByUserId(String userId);
}
