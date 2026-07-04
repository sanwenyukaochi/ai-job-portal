package com.portal.job.user.service.domain.core.valueobject;

import com.portal.job.user.service.domain.core.exception.UserDomainException;

public final class PasswordHash {
    private final String value;

    private PasswordHash(String value) {
        this.value = value;
    }

    public static PasswordHash of(String value) {
        if (value == null || value.isBlank()) {
            throw new UserDomainException("Password hash must not be blank.");
        }
        return new PasswordHash(value);
    }

    public String getValue() {
        return value;
    }
}
