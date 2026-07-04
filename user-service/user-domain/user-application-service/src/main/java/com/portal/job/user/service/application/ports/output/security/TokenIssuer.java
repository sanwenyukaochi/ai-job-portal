package com.portal.job.user.service.application.ports.output.security;

import com.portal.job.user.service.domain.core.entity.UserAccount;

public interface TokenIssuer {
    String issue(UserAccount userAccount);
}
