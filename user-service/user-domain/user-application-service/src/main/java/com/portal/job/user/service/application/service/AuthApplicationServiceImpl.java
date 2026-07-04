package com.portal.job.user.service.application.service;

import com.portal.job.user.service.application.mapper.UserViewMapper;
import com.portal.job.user.service.application.ports.input.service.AuthApplicationService;
import com.portal.job.user.service.application.ports.output.repository.UserAccountRepository;
import com.portal.job.user.service.application.ports.output.repository.UserProfileRepository;
import com.portal.job.user.service.application.ports.output.security.PasswordHasher;
import com.portal.job.user.service.application.ports.output.security.TokenIssuer;
import com.portal.job.user.service.application.view.AuthView;
import com.portal.job.user.service.application.view.UserView;
import com.portal.job.user.service.domain.core.entity.UserAccount;
import com.portal.job.user.service.domain.core.entity.UserProfile;
import com.portal.job.user.service.domain.core.exception.UserDomainException;
import com.portal.job.user.service.domain.core.policy.RegistrationPolicy;
import com.portal.job.user.service.domain.core.valueobject.AvatarUrl;
import com.portal.job.user.service.domain.core.valueobject.EmailAddress;
import com.portal.job.user.service.domain.core.valueobject.FullName;
import com.portal.job.user.service.domain.core.valueobject.PhoneNumber;
import com.portal.job.user.service.domain.core.valueobject.UserId;
import com.portal.job.user.service.domain.core.valueobject.UserRole;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class AuthApplicationServiceImpl implements AuthApplicationService {
    private final UserAccountRepository userAccountRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;
    private final RegistrationPolicy registrationPolicy;
    private final Clock clock;

    public AuthApplicationServiceImpl(
            UserAccountRepository userAccountRepository,
            UserProfileRepository userProfileRepository,
            PasswordHasher passwordHasher,
            TokenIssuer tokenIssuer,
            RegistrationPolicy registrationPolicy,
            Clock clock) {
        this.userAccountRepository = userAccountRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
        this.registrationPolicy = registrationPolicy;
        this.clock = clock;
    }

    @Override
    public AuthView registerLocal(
            String fullName, String email, String rawPassword, String phone, UserRole role) {
        EmailAddress emailAddress = EmailAddress.of(email);
        if (userAccountRepository.existsByEmail(emailAddress)) {
            throw new UserDomainException("Email already registered: " + emailAddress.getValue());
        }

        Instant now = Instant.now(clock);
        UserId userId = UserId.randomId();
        UserAccount account =
                UserAccount.registerLocal(
                        userId,
                        emailAddress,
                        passwordHasher.hash(rawPassword),
                        role,
                        now,
                        registrationPolicy);
        UserProfile profile =
                UserProfile.create(
                        userId,
                        FullName.of(fullName),
                        PhoneNumber.ofNullable(phone),
                        AvatarUrl.ofNullable(null),
                        now);

        userAccountRepository.save(account);
        userProfileRepository.save(profile);

        UserView userView = UserViewMapper.toView(account, profile);
        return new AuthView(
                "Welcome " + profile.getFullName().getValue(),
                "Registration successful",
                tokenIssuer.issue(account),
                userView);
    }

    @Override
    public AuthView login(String email, String rawPassword) {
        UserAccount account =
                userAccountRepository
                        .findByEmail(EmailAddress.of(email))
                        .orElseThrow(
                                () ->
                                        new UserDomainException(
                                                "User not found with email: " + email));
        account.ensureCanAuthenticate();
        if (!passwordHasher.matches(rawPassword, account.getCredential().getPasswordHash())) {
            throw new UserDomainException("Invalid password.");
        }

        account.recordLoginSuccess(Instant.now(clock));
        userAccountRepository.save(account);

        UserProfile profile =
                userProfileRepository
                        .findByUserId(account.getId())
                        .orElseThrow(() -> new UserDomainException("User profile not found."));
        return new AuthView(
                "Login successful",
                "Welcome back, " + profile.getFullName().getValue(),
                tokenIssuer.issue(account),
                UserViewMapper.toView(account, profile));
    }
}
