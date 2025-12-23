package com.lbk.wallet.card.internal.persistence.repo;

import com.lbk.wallet.card.internal.persistence.entity.DebitCardStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface DebitCardStatusRepository extends JpaRepository<DebitCardStatusEntity, String> {
    List<DebitCardStatusEntity> findByUserId(String userId);
}

