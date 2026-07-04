package com.portal.job.user.service.domain.core.entity;

import com.portal.job.user.service.domain.core.exception.UserDomainException;
import com.portal.job.user.service.domain.core.policy.RegistrationPolicy;
import com.portal.job.user.service.domain.core.valueobject.AccountStatus;
import com.portal.job.user.service.domain.core.valueobject.AuthProvider;
import com.portal.job.user.service.domain.core.valueobject.EmailAddress;
import com.portal.job.user.service.domain.core.valueobject.PasswordHash;
import com.portal.job.user.service.domain.core.valueobject.UserId;
import com.portal.job.user.service.domain.core.valueobject.UserRole;
import com.portal.job.user.service.domain.entity.AggregateRoot;
import java.time.Instant;

public class UserAccount extends AggregateRoot<UserId> {
    private final EmailAddress email;
    private final AuthProvider authProvider;
    private final Credential credential;
    private UserRole role;
    private AccountStatus status;
    private boolean verified;
    private int tokenVersion;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant lastLoginAt;
    private Instant suspendedAt;
    private Instant deletedAt;

    private UserAccount(
            UserId userId,
            EmailAddress email,
            Credential credential,
            UserRole role,
            AuthProvider authProvider,
            Instant createdAt) {
        setId(userId);
        this.email = email;
        this.credential = credential;
        this.role = role;
        this.authProvider = authProvider;
        this.status = AccountStatus.ACTIVE;
        this.verified = false;
        this.tokenVersion = 0;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public static UserAccount registerLocal(
            UserId userId,
            EmailAddress email,
            PasswordHash passwordHash,
            UserRole role,
            Instant createdAt,
            RegistrationPolicy registrationPolicy) {
        registrationPolicy.validateSelfRegistration(role);
        return new UserAccount(
                userId,
                email,
                new Credential(passwordHash, createdAt),
                role,
                AuthProvider.LOCAL,
                createdAt);
    }

    public static UserAccount rehydrate(
            UserId userId,
            EmailAddress email,
            Credential credential,
            UserRole role,
            AuthProvider authProvider,
            AccountStatus status,
            boolean verified,
            int tokenVersion,
            Instant createdAt,
            Instant updatedAt,
            Instant lastLoginAt,
            Instant suspendedAt,
            Instant deletedAt) {
        UserAccount userAccount =
                new UserAccount(userId, email, credential, role, authProvider, createdAt);
        userAccount.status = status;
        userAccount.verified = verified;
        userAccount.tokenVersion = tokenVersion;
        userAccount.updatedAt = updatedAt;
        userAccount.lastLoginAt = lastLoginAt;
        userAccount.suspendedAt = suspendedAt;
        userAccount.deletedAt = deletedAt;
        return userAccount;
    }

    public void ensureCanAuthenticate() {
        if (status == AccountStatus.SUSPENDED) {
            throw new UserDomainException("Suspended user cannot login.");
        }
        if (status == AccountStatus.DELETED) {
            throw new UserDomainException("Deleted user cannot login.");
        }
    }

    public void recordLoginSuccess(Instant loggedInAt) {
        ensureCanAuthenticate();
        this.lastLoginAt = loggedInAt;
        this.updatedAt = loggedInAt;
    }

    public void suspend(Instant suspendedAt) {
        if (status == AccountStatus.DELETED) {
            throw new UserDomainException("Deleted user cannot be suspended.");
        }
        this.status = AccountStatus.SUSPENDED;
        this.suspendedAt = suspendedAt;
        this.updatedAt = suspendedAt;
        rotateTokenVersion();
    }

    public void activate(Instant activatedAt) {
        if (status == AccountStatus.DELETED) {
            throw new UserDomainException("Deleted user cannot be activated.");
        }
        this.status = AccountStatus.ACTIVE;
        this.suspendedAt = null;
        this.updatedAt = activatedAt;
    }

    public void delete(Instant deletedAt) {
        this.status = AccountStatus.DELETED;
        this.deletedAt = deletedAt;
        this.updatedAt = deletedAt;
        rotateTokenVersion();
    }

    public void changeRole(UserRole newRole, Instant changedAt) {
        if (status == AccountStatus.DELETED) {
            throw new UserDomainException("Deleted user role cannot be changed.");
        }
        this.role = newRole;
        this.updatedAt = changedAt;
    }

    public void verify(Instant verifiedAt) {
        this.verified = true;
        this.updatedAt = verifiedAt;
    }

    public void rotateTokenVersion() {
        this.tokenVersion = this.tokenVersion + 1;
    }

    public EmailAddress getEmail() {
        return email;
    }

    public AuthProvider getAuthProvider() {
        return authProvider;
    }

    public Credential getCredential() {
        return credential;
    }

    public UserRole getRole() {
        return role;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public boolean isVerified() {
        return verified;
    }

    public int getTokenVersion() {
        return tokenVersion;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public Instant getSuspendedAt() {
        return suspendedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }
}
