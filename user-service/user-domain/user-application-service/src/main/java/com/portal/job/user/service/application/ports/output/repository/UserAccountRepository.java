package com.portal.job.user.service.application.ports.output.repository;

import com.portal.job.user.service.domain.core.entity.UserAccount;
import com.portal.job.user.service.domain.core.valueobject.EmailAddress;
import com.portal.job.user.service.domain.core.valueobject.UserId;

import java.util.List;
import java.util.Optional;

public interface UserAccountRepository {
    Optional<UserAccount> findById(UserId userId);

    Optional<UserAccount> findByEmail(EmailAddress emailAddress);

    List<UserAccount> findAll();

    boolean existsByEmail(EmailAddress emailAddress);

    UserAccount save(UserAccount userAccount);
}
