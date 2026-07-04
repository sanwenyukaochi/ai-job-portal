package com.portal.job.user.service.domain.core.valueobject;

import com.portal.job.user.service.domain.core.exception.UserDomainException;

public final class FullName {
    private final String value;

    private FullName(String value) {
        this.value = value;
    }

    public static FullName of(String value) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            throw new UserDomainException("Full name must not be blank.");
        }
        return new FullName(normalized);
    }

    public String getValue() {
        return value;
    }
}
