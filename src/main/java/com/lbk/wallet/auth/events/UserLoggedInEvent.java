package com.lbk.wallet.auth.events;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Event published when a user successfully logs in
 */
@Data
@AllArgsConstructor
public class UserLoggedInEvent {
    private final String userId;
    private final LocalDateTime timestamp;
}
