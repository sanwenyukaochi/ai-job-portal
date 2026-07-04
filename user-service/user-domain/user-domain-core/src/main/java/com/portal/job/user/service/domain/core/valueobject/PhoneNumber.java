package com.portal.job.user.service.domain.core.valueobject;

import com.portal.job.user.service.domain.core.exception.UserDomainException;

public final class PhoneNumber {
    private final String value;

    private PhoneNumber(String value) {
        this.value = value;
    }

    public static PhoneNumber ofNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (!normalized.matches("[0-9+\\-() ]{6,20}")) {
            throw new UserDomainException("Phone number format is invalid.");
        }
        return new PhoneNumber(normalized);
    }

    public String getValue() {
        return value;
    }
}
