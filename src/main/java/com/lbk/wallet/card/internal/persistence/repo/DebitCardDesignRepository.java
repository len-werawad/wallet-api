package com.lbk.wallet.card.internal.persistence.repo;

import com.lbk.wallet.card.internal.persistence.entity.DebitCardDesignEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface DebitCardDesignRepository extends JpaRepository<DebitCardDesignEntity, String> {
    List<DebitCardDesignEntity> findByUserId(String userId);
}

