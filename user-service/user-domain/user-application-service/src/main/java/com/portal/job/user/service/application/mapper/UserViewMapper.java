package com.portal.job.user.service.application.mapper;

import com.portal.job.user.service.application.view.UserView;
import com.portal.job.user.service.domain.core.entity.UserAccount;
import com.portal.job.user.service.domain.core.entity.UserProfile;

public final class UserViewMapper {
    private UserViewMapper() {}

    public static UserView toView(UserAccount account, UserProfile profile) {
        return new UserView(
                account.getId().getValue().toString(),
                profile.getFullName().getValue(),
                account.getEmail().getValue(),
                profile.getPhoneNumber() == null ? null : profile.getPhoneNumber().getValue(),
                profile.getAvatarUrl() == null ? null : profile.getAvatarUrl().getValue(),
                account.getRole(),
                account.getAuthProvider(),
                account.getStatus(),
                account.isVerified(),
                account.getTokenVersion(),
                account.getLastLoginAt(),
                account.getCreatedAt());
    }
}
