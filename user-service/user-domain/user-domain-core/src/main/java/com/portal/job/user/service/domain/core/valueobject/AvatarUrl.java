package com.portal.job.user.service.domain.core.valueobject;

import com.portal.job.user.service.domain.core.exception.UserDomainException;

public final class AvatarUrl {
    private final String value;

    private AvatarUrl(String value) {
        this.value = value;
    }

    public static AvatarUrl ofNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (!(normalized.startsWith("http://") || normalized.startsWith("https://"))) {
            throw new UserDomainException("Avatar URL must start with http:// or https://.");
        }
        return new AvatarUrl(normalized);
    }

    public String getValue() {
        return value;
    }
}
