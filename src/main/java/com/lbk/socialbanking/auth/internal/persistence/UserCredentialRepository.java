package com.lbk.socialbanking.auth.internal.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCredentialRepository extends JpaRepository<UserCredentialEntity, Long> {
    Optional<UserCredentialEntity> findTopByUserIdOrderByUpdatedAtDesc(String userId);
}
