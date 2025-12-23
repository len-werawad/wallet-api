package com.lbk.wallet.customer.internal.persistence.repo;

import com.lbk.wallet.customer.internal.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface UserRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByUserId(String userId);
}

