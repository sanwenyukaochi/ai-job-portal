package com.portal.job.user.service.application.service;

import com.portal.job.user.service.application.mapper.UserViewMapper;
import com.portal.job.user.service.application.ports.input.service.UserManagementApplicationService;
import com.portal.job.user.service.application.ports.output.repository.UserAccountRepository;
import com.portal.job.user.service.application.ports.output.repository.UserProfileRepository;
import com.portal.job.user.service.application.view.UserView;
import com.portal.job.user.service.domain.core.entity.UserAccount;
import com.portal.job.user.service.domain.core.entity.UserProfile;
import com.portal.job.user.service.domain.core.exception.UserDomainException;
import com.portal.job.user.service.domain.core.valueobject.AccountStatus;
import com.portal.job.user.service.domain.core.valueobject.AvatarUrl;
import com.portal.job.user.service.domain.core.valueobject.EmailAddress;
import com.portal.job.user.service.domain.core.valueobject.FullName;
import com.portal.job.user.service.domain.core.valueobject.PhoneNumber;
import com.portal.job.user.service.domain.core.valueobject.UserId;
import com.portal.job.user.service.domain.core.valueobject.UserRole;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class UserManagementApplicationServiceImpl implements UserManagementApplicationService {
    private final UserAccountRepository userAccountRepository;
    private final UserProfileRepository userProfileRepository;
    private final Clock clock;

    public UserManagementApplicationServiceImpl(
            UserAccountRepository userAccountRepository,
            UserProfileRepository userProfileRepository,
            Clock clock) {
        this.userAccountRepository = userAccountRepository;
        this.userProfileRepository = userProfileRepository;
        this.clock = clock;
    }

    @Override
    public UserView getCurrentProfile(String email) {
        UserAccount account = getAccountByEmail(email);
        return toView(account);
    }

    @Override
    public UserView updateCurrentProfile(String email, String fullName, String phone, String avatarUrl) {
        UserAccount account = getAccountByEmail(email);
        UserProfile profile = getProfile(account.getId());
        profile.updateBasicInfo(
                FullName.of(fullName),
                PhoneNumber.ofNullable(phone),
                AvatarUrl.ofNullable(avatarUrl),
                Instant.now(clock),
                account.getStatus() != AccountStatus.DELETED);
        userProfileRepository.save(profile);
        return UserViewMapper.toView(account, profile);
    }

    @Override
    public UserView getUserById(String userId) {
        return toView(getAccountById(userId));
    }

    @Override
    public List<UserView> getAllUsers() {
        return userAccountRepository.findAll().stream()
                .sorted(Comparator.comparing(UserAccount::getCreatedAt))
                .map(this::toView)
                .toList();
    }

    @Override
    public UserView suspendUser(String userId) {
        UserAccount account = getAccountById(userId);
        account.suspend(Instant.now(clock));
        userAccountRepository.save(account);
        return toView(account);
    }

    @Override
    public UserView activateUser(String userId) {
        UserAccount account = getAccountById(userId);
        account.activate(Instant.now(clock));
        userAccountRepository.save(account);
        return toView(account);
    }

    @Override
    public UserView deleteUser(String userId) {
        UserAccount account = getAccountById(userId);
        account.delete(Instant.now(clock));
        userAccountRepository.save(account);
        return toView(account);
    }

    @Override
    public UserView changeUserRole(String userId, UserRole role) {
        UserAccount account = getAccountById(userId);
        account.changeRole(role, Instant.now(clock));
        userAccountRepository.save(account);
        return toView(account);
    }

    private UserAccount getAccountByEmail(String email) {
        return userAccountRepository
                .findByEmail(EmailAddress.of(email))
                .orElseThrow(() -> new UserDomainException("User not found with email: " + email));
    }

    private UserAccount getAccountById(String userId) {
        return userAccountRepository
                .findById(UserId.from(userId))
                .orElseThrow(() -> new UserDomainException("User not found with id: " + userId));
    }

    private UserProfile getProfile(UserId userId) {
        return userProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new UserDomainException("User profile not found."));
    }

    private UserView toView(UserAccount account) {
        return UserViewMapper.toView(account, getProfile(account.getId()));
    }
}
