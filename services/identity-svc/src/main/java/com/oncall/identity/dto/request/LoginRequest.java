package com.oncall.identity.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(

        @NotBlank
        @Email
        @Pattern(
                regexp = "^[a-zA-Z0-9._%+\\-]+@agilysys\\.com$",
                message = "Only @agilysys.com email addresses are permitted"
        )
        String email,

        @NotBlank
        String password
) {}
