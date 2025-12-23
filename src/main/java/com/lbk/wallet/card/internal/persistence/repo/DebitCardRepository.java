package com.lbk.wallet.card.internal.persistence.repo;

import com.lbk.wallet.card.internal.persistence.entity.DebitCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface DebitCardRepository extends JpaRepository<DebitCardEntity, String> {
    List<DebitCardEntity> findByUserId(String userId);
}
