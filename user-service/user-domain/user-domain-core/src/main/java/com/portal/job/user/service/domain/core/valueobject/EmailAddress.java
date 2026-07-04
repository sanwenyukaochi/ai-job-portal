package com.portal.job.user.service.domain.core.valueobject;

import com.portal.job.user.service.domain.core.exception.UserDomainException;

import java.util.Objects;

public final class EmailAddress {
    private final String value;

    private EmailAddress(String value) {
        this.value = value;
    }

    public static EmailAddress of(String rawValue) {
        String normalized = rawValue == null ? "" : rawValue.trim().toLowerCase();
        if (normalized.isEmpty() || !normalized.contains("@")) {
            throw new UserDomainException("Email format is invalid.");
        }
        return new EmailAddress(normalized);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmailAddress that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
