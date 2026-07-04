package com.portal.job.user.service.domain.core.entity;

import com.portal.job.user.service.domain.core.valueobject.PasswordHash;
import java.time.Instant;

public class Credential {
    private PasswordHash passwordHash;
    private Instant passwordUpdatedAt;

    public Credential(PasswordHash passwordHash, Instant passwordUpdatedAt) {
        this.passwordHash = passwordHash;
        this.passwordUpdatedAt = passwordUpdatedAt;
    }

    public PasswordHash getPasswordHash() {
        return passwordHash;
    }

    public Instant getPasswordUpdatedAt() {
        return passwordUpdatedAt;
    }

    public void changePassword(PasswordHash passwordHash, Instant changedAt) {
        this.passwordHash = passwordHash;
        this.passwordUpdatedAt = changedAt;
    }
}
