package com.portal.job.user.service.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserProfileRequest(
        @NotBlank(message = "Full name is mandatory") String fullName,
        String phone,
        String avatarUrl) {}
