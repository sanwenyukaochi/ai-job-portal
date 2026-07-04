package com.portal.job.user.service.domain.core.valueobject;

import com.portal.job.user.service.domain.valueobject.BaseId;

import java.util.UUID;

public class UserId extends BaseId<UUID> {
    public UserId(UUID value) {
        super(value);
    }

    public static UserId randomId() {
        return new UserId(UUID.randomUUID());
    }

    public static UserId from(String value) {
        return new UserId(UUID.fromString(value));
    }
}
