package com.portal.job.user.service.application.dto.request;

import com.portal.job.user.service.domain.core.valueobject.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SignupRequest(
        @NotBlank(message = "Full name is mandatory") String fullName,
        @Email(message = "Email should be valid") @NotBlank(message = "Email is mandatory")
                String email,
        @NotBlank(message = "Password is mandatory") String password,
        String phone,
        @NotNull(message = "Role is mandatory") UserRole role) {}
