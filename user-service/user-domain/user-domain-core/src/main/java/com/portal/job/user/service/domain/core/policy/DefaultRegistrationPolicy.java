package com.portal.job.user.service.domain.core.policy;

import com.portal.job.user.service.domain.core.exception.UserDomainException;
import com.portal.job.user.service.domain.core.valueobject.UserRole;

public class DefaultRegistrationPolicy implements RegistrationPolicy {
    @Override
    public void validateSelfRegistration(UserRole role) {
        if (role == UserRole.ROLE_ADMIN) {
            throw new UserDomainException("Cannot self-register as ROLE_ADMIN.");
        }
    }
}
