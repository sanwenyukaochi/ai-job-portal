package com.portal.job.user.service.application.ports.input.service;

import com.portal.job.user.service.application.view.UserView;
import com.portal.job.user.service.domain.core.valueobject.UserRole;
import java.util.List;

public interface UserManagementApplicationService {
    UserView getCurrentProfile(String email);

    UserView updateCurrentProfile(String email, String fullName, String phone, String avatarUrl);

    UserView getUserById(String userId);

    List<UserView> getAllUsers();

    UserView suspendUser(String userId);

    UserView activateUser(String userId);

    UserView deleteUser(String userId);

    UserView changeUserRole(String userId, UserRole role);
}
