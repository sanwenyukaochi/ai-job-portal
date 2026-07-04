package com.portal.job.user.service.application.assembler;

import com.portal.job.user.service.application.dto.response.AuthResponse;
import com.portal.job.user.service.application.dto.response.UserResponse;
import com.portal.job.user.service.application.view.AuthView;
import com.portal.job.user.service.application.view.UserView;

public final class UserResponseAssembler {
    private UserResponseAssembler() {}

    public static UserResponse toResponse(UserView view) {
        return new UserResponse(
                view.id(),
                view.fullName(),
                view.email(),
                view.phone(),
                view.avatarUrl(),
                view.role(),
                view.authProvider(),
                view.status(),
                view.verified(),
                view.tokenVersion(),
                view.lastLoginAt(),
                view.createdAt());
    }

    public static AuthResponse toResponse(AuthView view) {
        return new AuthResponse(
                view.title(), view.message(), view.token(), toResponse(view.user()));
    }
}
