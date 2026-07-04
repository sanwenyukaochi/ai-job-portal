package com.portal.job.user.service.security.adapter;

import com.portal.job.user.service.application.ports.output.security.TokenIssuer;
import com.portal.job.user.service.domain.core.entity.UserAccount;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
public class SimpleTokenIssuer implements TokenIssuer {
    @Override
    public String issue(UserAccount userAccount) {
        String payload =
                userAccount.getId().getValue()
                        + ":"
                        + userAccount.getEmail().getValue()
                        + ":"
                        + userAccount.getRole().name()
                        + ":"
                        + userAccount.getTokenVersion()
                        + ":"
                        + Instant.now().toEpochMilli();
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }
}
