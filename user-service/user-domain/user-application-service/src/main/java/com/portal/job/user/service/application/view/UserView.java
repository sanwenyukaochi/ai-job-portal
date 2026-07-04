package com.portal.job.user.service.application.view;

import com.portal.job.user.service.domain.core.valueobject.AccountStatus;
import com.portal.job.user.service.domain.core.valueobject.AuthProvider;
import com.portal.job.user.service.domain.core.valueobject.UserRole;

import java.time.Instant;

public record UserView(
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
