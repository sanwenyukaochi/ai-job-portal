package com.portal.job.user.service.application.dto.response;

import com.portal.job.user.service.domain.core.valueobject.AccountStatus;
import com.portal.job.user.service.domain.core.valueobject.AuthProvider;
import com.portal.job.user.service.domain.core.valueobject.UserRole;
import java.time.Instant;

public record UserResponse(
        String id,
        String fullName,
        String email,
        String phone,
        String avatarUrl,
        UserRole role,
        AuthProvider authProvider,
        AccountStatus status,
        boolean verified,
        int tokenVersion,
        Instant lastLoginAt,
        Instant createdAt) {}
