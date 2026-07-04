package com.portal.job.user.service.dataaccess.user.mapper;

import com.portal.job.user.service.dataaccess.user.entity.AccountStatusEntity;
import com.portal.job.user.service.dataaccess.user.entity.AuthProviderEntity;
import com.portal.job.user.service.dataaccess.user.entity.UserAccountEntity;
import com.portal.job.user.service.dataaccess.user.entity.UserProfileEntity;
import com.portal.job.user.service.dataaccess.user.entity.UserRoleEntity;
import com.portal.job.user.service.domain.core.entity.Credential;
import com.portal.job.user.service.domain.core.entity.UserAccount;
import com.portal.job.user.service.domain.core.entity.UserProfile;
import com.portal.job.user.service.domain.core.valueobject.AccountStatus;
import com.portal.job.user.service.domain.core.valueobject.AuthProvider;
import com.portal.job.user.service.domain.core.valueobject.AvatarUrl;
import com.portal.job.user.service.domain.core.valueobject.EmailAddress;
import com.portal.job.user.service.domain.core.valueobject.FullName;
import com.portal.job.user.service.domain.core.valueobject.PasswordHash;
import com.portal.job.user.service.domain.core.valueobject.PhoneNumber;
import com.portal.job.user.service.domain.core.valueobject.UserId;
import com.portal.job.user.service.domain.core.valueobject.UserRole;

import org.springframework.stereotype.Component;

@Component
public class UserDataAccessMapper {
    public UserAccountEntity userAccountToEntity(UserAccount userAccount) {
        return UserAccountEntity.builder()
                .id(userAccount.getId().getValue())
                .email(userAccount.getEmail().getValue())
                .passwordHash(userAccount.getCredential().getPasswordHash().getValue())
                .role(UserRoleEntity.valueOf(userAccount.getRole().name()))
                .authProvider(AuthProviderEntity.valueOf(userAccount.getAuthProvider().name()))
                .status(AccountStatusEntity.valueOf(userAccount.getStatus().name()))
                .verified(userAccount.isVerified())
                .tokenVersion(userAccount.getTokenVersion())
                .createdAt(userAccount.getCreatedAt())
                .updatedAt(userAccount.getUpdatedAt())
                .lastLoginAt(userAccount.getLastLoginAt())
                .suspendedAt(userAccount.getSuspendedAt())
                .deletedAt(userAccount.getDeletedAt())
                .passwordUpdatedAt(userAccount.getCredential().getPasswordUpdatedAt())
                .build();
    }

    public UserAccount entityToUserAccount(UserAccountEntity entity) {
        return UserAccount.rehydrate(
                new UserId(entity.getId()),
                EmailAddress.of(entity.getEmail()),
                new Credential(
                        PasswordHash.of(entity.getPasswordHash()), entity.getPasswordUpdatedAt()),
                UserRole.valueOf(entity.getRole().name()),
                AuthProvider.valueOf(entity.getAuthProvider().name()),
                AccountStatus.valueOf(entity.getStatus().name()),
                entity.isVerified(),
                entity.getTokenVersion(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getLastLoginAt(),
                entity.getSuspendedAt(),
                entity.getDeletedAt());
    }

    public UserProfileEntity userProfileToEntity(UserProfile userProfile) {
        return UserProfileEntity.builder()
                .userId(userProfile.getId().getValue())
                .fullName(userProfile.getFullName().getValue())
                .phone(
                        userProfile.getPhoneNumber() == null
                                ? null
                                : userProfile.getPhoneNumber().getValue())
                .avatarUrl(
                        userProfile.getAvatarUrl() == null
                                ? null
                                : userProfile.getAvatarUrl().getValue())
                .createdAt(userProfile.getCreatedAt())
                .updatedAt(userProfile.getUpdatedAt())
                .build();
    }

    public UserProfile entityToUserProfile(UserProfileEntity entity) {
        return UserProfile.rehydrate(
                new UserId(entity.getUserId()),
                FullName.of(entity.getFullName()),
                PhoneNumber.ofNullable(entity.getPhone()),
                AvatarUrl.ofNullable(entity.getAvatarUrl()),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
