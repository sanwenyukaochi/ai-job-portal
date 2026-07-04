package com.portal.job.user.service.dataaccess.adapter;

import com.portal.job.user.service.application.ports.output.repository.UserProfileRepository;
import com.portal.job.user.service.domain.core.entity.UserProfile;
import com.portal.job.user.service.domain.core.valueobject.UserId;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryUserProfileRepository implements UserProfileRepository {
    private final Map<UUID, UserProfile> storage = new ConcurrentHashMap<>();

    @Override
    public Optional<UserProfile> findByUserId(UserId userId) {
        return Optional.ofNullable(storage.get(userId.getValue()));
    }

    @Override
    public UserProfile save(UserProfile userProfile) {
        storage.put(userProfile.getId().getValue(), userProfile);
        return userProfile;
    }
}
