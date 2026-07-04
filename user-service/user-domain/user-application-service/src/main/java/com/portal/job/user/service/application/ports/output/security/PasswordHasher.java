package com.portal.job.user.service.application.ports.output.security;

import com.portal.job.user.service.domain.core.valueobject.PasswordHash;

public interface PasswordHasher {
    PasswordHash hash(String rawPassword);

    boolean matches(String rawPassword, PasswordHash passwordHash);
}
