package com.portal.job.user.service.application.ports.input.service;

import com.portal.job.user.service.application.view.AuthView;
import com.portal.job.user.service.domain.core.valueobject.UserRole;

public interface AuthApplicationService {
    AuthView registerLocal(
            String fullName, String email, String rawPassword, String phone, UserRole role);

    AuthView login(String email, String rawPassword);
}
