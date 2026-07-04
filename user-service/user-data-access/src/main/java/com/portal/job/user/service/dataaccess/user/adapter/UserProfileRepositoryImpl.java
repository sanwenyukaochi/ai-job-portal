package com.portal.job.user.service.dataaccess.user.adapter;

import com.portal.job.user.service.application.ports.output.repository.UserProfileRepository;
import com.portal.job.user.service.dataaccess.user.mapper.UserDataAccessMapper;
import com.portal.job.user.service.dataaccess.user.repository.UserProfileJpaRepository;
import com.portal.job.user.service.domain.core.entity.UserProfile;
import com.portal.job.user.service.domain.core.valueobject.UserId;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class UserProfileRepositoryImpl implements UserProfileRepository {
    private final UserProfileJpaRepository userProfileJpaRepository;
    private final UserDataAccessMapper userDataAccessMapper;

    public UserProfileRepositoryImpl(
            UserProfileJpaRepository userProfileJpaRepository,
            UserDataAccessMapper userDataAccessMapper) {
        this.userProfileJpaRepository = userProfileJpaRepository;
        this.userDataAccessMapper = userDataAccessMapper;
    }

    @Override
    public Optional<UserProfile> findByUserId(UserId userId) {
        return userProfileJpaRepository
                .findById(userId.getValue())
                .map(userDataAccessMapper::entityToUserProfile);
    }

    @Override
    public UserProfile save(UserProfile userProfile) {
        return userDataAccessMapper.entityToUserProfile(
                userProfileJpaRepository.save(
                        userDataAccessMapper.userProfileToEntity(userProfile)));
    }
}
