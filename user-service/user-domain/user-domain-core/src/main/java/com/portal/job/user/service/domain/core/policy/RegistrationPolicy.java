package com.portal.job.user.service.domain.core.policy;

import com.portal.job.user.service.domain.core.valueobject.UserRole;

public interface RegistrationPolicy {
    void validateSelfRegistration(UserRole role);
}
