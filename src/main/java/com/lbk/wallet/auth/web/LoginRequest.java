package com.lbk.wallet.auth.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "userId is required")
        String userId,

        @NotBlank(message = "pin is required")
        @Size(min = 6, max = 6, message = "pin must be exactly 6 digits")
        @Pattern(regexp = "\\d{6}", message = "pin must be numeric")
        String pin
) {
}
