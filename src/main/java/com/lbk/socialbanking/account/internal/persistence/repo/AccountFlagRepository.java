package com.lbk.socialbanking.account.internal.persistence.repo;

import com.lbk.socialbanking.account.internal.persistence.entity.AccountFlagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountFlagRepository extends JpaRepository<AccountFlagEntity, Integer> {
    List<AccountFlagEntity> findByUserIdAndFlagType(String userId, String flagType);
}
