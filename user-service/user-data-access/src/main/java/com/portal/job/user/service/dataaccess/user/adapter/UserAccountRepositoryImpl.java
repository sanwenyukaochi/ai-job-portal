package com.portal.job.user.service.dataaccess.user.adapter;

import com.portal.job.user.service.application.ports.output.repository.UserAccountRepository;
import com.portal.job.user.service.dataaccess.user.mapper.UserDataAccessMapper;
import com.portal.job.user.service.dataaccess.user.repository.UserAccountJpaRepository;
import com.portal.job.user.service.domain.core.entity.UserAccount;
import com.portal.job.user.service.domain.core.valueobject.EmailAddress;
import com.portal.job.user.service.domain.core.valueobject.UserId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class UserAccountRepositoryImpl implements UserAccountRepository {
    private final UserAccountJpaRepository userAccountJpaRepository;
    private final UserDataAccessMapper userDataAccessMapper;

    public UserAccountRepositoryImpl(
            UserAccountJpaRepository userAccountJpaRepository,
            UserDataAccessMapper userDataAccessMapper) {
        this.userAccountJpaRepository = userAccountJpaRepository;
        this.userDataAccessMapper = userDataAccessMapper;
    }

    @Override
    public Optional<UserAccount> findById(UserId userId) {
        return userAccountJpaRepository
                .findById(userId.getValue())
                .map(userDataAccessMapper::entityToUserAccount);
    }

    @Override
    public Optional<UserAccount> findByEmail(EmailAddress emailAddress) {
        return userAccountJpaRepository
                .findByEmail(emailAddress.getValue())
                .map(userDataAccessMapper::entityToUserAccount);
    }

    @Override
    public List<UserAccount> findAll() {
        return userAccountJpaRepository.findAll().stream()
                .map(userDataAccessMapper::entityToUserAccount)
                .toList();
    }

    @Override
    public boolean existsByEmail(EmailAddress emailAddress) {
        return userAccountJpaRepository.existsByEmail(emailAddress.getValue());
    }

    @Override
    public UserAccount save(UserAccount userAccount) {
        return userDataAccessMapper.entityToUserAccount(
                userAccountJpaRepository.save(userDataAccessMapper.userAccountToEntity(userAccount)));
    }
}
