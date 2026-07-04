package com.portal.job.user.service.domain.core.entity;

import com.portal.job.user.service.domain.core.exception.UserDomainException;
import com.portal.job.user.service.domain.core.valueobject.AvatarUrl;
import com.portal.job.user.service.domain.core.valueobject.FullName;
import com.portal.job.user.service.domain.core.valueobject.PhoneNumber;
import com.portal.job.user.service.domain.core.valueobject.UserId;
import com.portal.job.user.service.domain.entity.AggregateRoot;
import java.time.Instant;

public class UserProfile extends AggregateRoot<UserId> {
    private FullName fullName;
    private PhoneNumber phoneNumber;
    private AvatarUrl avatarUrl;
    private final Instant createdAt;
    private Instant updatedAt;

    private UserProfile(
            UserId userId,
            FullName fullName,
            PhoneNumber phoneNumber,
            AvatarUrl avatarUrl,
            Instant createdAt) {
        setId(userId);
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.avatarUrl = avatarUrl;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public static UserProfile create(
            UserId userId,
            FullName fullName,
            PhoneNumber phoneNumber,
            AvatarUrl avatarUrl,
            Instant createdAt) {
        return new UserProfile(userId, fullName, phoneNumber, avatarUrl, createdAt);
    }

    public void updateBasicInfo(
            FullName fullName,
            PhoneNumber phoneNumber,
            AvatarUrl avatarUrl,
            Instant updatedAt,
            boolean canBeManaged) {
        if (!canBeManaged) {
            throw new UserDomainException("Deleted user profile cannot be updated.");
        }
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.avatarUrl = avatarUrl;
        this.updatedAt = updatedAt;
    }

    public FullName getFullName() {
        return fullName;
    }

    public PhoneNumber getPhoneNumber() {
        return phoneNumber;
    }

    public AvatarUrl getAvatarUrl() {
        return avatarUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
