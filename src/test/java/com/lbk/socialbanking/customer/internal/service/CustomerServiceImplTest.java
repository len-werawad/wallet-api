package com.lbk.socialbanking.customer.internal.service;

import com.lbk.socialbanking.customer.internal.persistence.entity.UserGreetingEntity;
import com.lbk.socialbanking.customer.internal.persistence.repo.UserGreetingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private UserGreetingRepository userGreetingRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Nested
    @DisplayName("getGreeting")
    class GetGreeting {

        @Test
        @DisplayName("should return greeting from repository when present")
        void getGreeting_found() {
            String userId = "u1";
            UserGreetingEntity entity = new UserGreetingEntity();
            entity.setUserId(userId);
            entity.setGreeting("Hello u1");
            given(userGreetingRepository.findById(userId)).willReturn(Optional.of(entity));

            String result = customerService.getGreeting(userId);

            assertThat(result).isEqualTo("Hello u1");
        }

        @Test
        @DisplayName("should return default greeting when not found")
        void getGreeting_notFound() {
            String userId = "u2";
            given(userGreetingRepository.findById(userId)).willReturn(Optional.empty());

            String result = customerService.getGreeting(userId);

            assertThat(result).isEqualTo("Welcome");
        }
    }
}

