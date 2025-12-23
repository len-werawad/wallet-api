package com.lbk.wallet.card.internal.persistence.repo;

import com.lbk.wallet.card.internal.persistence.entity.DebitCardDetailsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface DebitCardDetailsRepository extends JpaRepository<DebitCardDetailsEntity, String> {
    List<DebitCardDetailsEntity> findByUserId(String userId);
}

