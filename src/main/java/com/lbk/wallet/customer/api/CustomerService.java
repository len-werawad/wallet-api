package com.lbk.wallet.customer.api;

/**
 * CustomerService defines operations related to customer information.
 */
public interface CustomerService {

    /**
     * Returns a greeting message for the specified user.
     *
     * @param userId the ID of the user
     * @return a greeting message
     */
    String getGreeting(String userId);
}
