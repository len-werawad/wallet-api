package com.lbk.socialbanking.customer.internal.persistence.repo;

import com.lbk.socialbanking.customer.internal.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface UserRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByUserId(String userId);
}

