package com.portal.job.user.service.application.ports.output.repository;

import com.portal.job.user.service.domain.core.entity.UserProfile;
import com.portal.job.user.service.domain.core.valueobject.UserId;
import java.util.Optional;

public interface UserProfileRepository {
    Optional<UserProfile> findByUserId(UserId userId);

    UserProfile save(UserProfile userProfile);
}
