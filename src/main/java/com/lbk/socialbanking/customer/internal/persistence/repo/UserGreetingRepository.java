package com.lbk.socialbanking.customer.internal.persistence.repo;

import com.lbk.socialbanking.customer.internal.persistence.entity.UserGreetingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserGreetingRepository extends JpaRepository<UserGreetingEntity, String> {
}
