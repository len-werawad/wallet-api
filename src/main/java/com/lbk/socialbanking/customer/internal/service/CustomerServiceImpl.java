package com.lbk.socialbanking.customer.internal.service;

import com.lbk.socialbanking.customer.api.CustomerService;
import com.lbk.socialbanking.customer.internal.persistence.entity.UserGreetingEntity;
import com.lbk.socialbanking.customer.internal.persistence.repo.UserGreetingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceImpl implements CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerServiceImpl.class);

    private final UserGreetingRepository greetings;

    public CustomerServiceImpl(UserGreetingRepository greetings) {
        this.greetings = greetings;
    }

    @Override
    public String getGreeting(String userId) {
        log.debug("Fetching greeting for user: {}", userId);

        String greeting = greetings.findById(userId)
                .map(UserGreetingEntity::getGreeting)
                .orElse("Welcome");

        log.debug("Greeting retrieved for user {}: {}", userId, greeting);
        return greeting;
    }
}
