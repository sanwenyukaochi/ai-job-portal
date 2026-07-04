package com.portal.job.user.service.security.adapter;

import com.portal.job.user.service.application.ports.output.security.PasswordHasher;
import com.portal.job.user.service.domain.core.valueobject.PasswordHash;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BcryptPasswordHasher implements PasswordHasher {
    private final BCryptPasswordEncoder delegate = new BCryptPasswordEncoder();

    @Override
    public PasswordHash hash(String rawPassword) {
        return PasswordHash.of(delegate.encode(rawPassword));
    }

    @Override
    public boolean matches(String rawPassword, PasswordHash passwordHash) {
        return delegate.matches(rawPassword, passwordHash.getValue());
    }
}
