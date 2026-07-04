package com.portal.job.user.service.dataaccess.adapter;

import com.portal.job.user.service.application.ports.output.repository.UserAccountRepository;
import com.portal.job.user.service.domain.core.entity.UserAccount;
import com.portal.job.user.service.domain.core.valueobject.EmailAddress;
import com.portal.job.user.service.domain.core.valueobject.UserId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryUserAccountRepository implements UserAccountRepository {
    private final Map<UUID, UserAccount> storage = new ConcurrentHashMap<>();
    private final Map<String, UUID> emailIndex = new ConcurrentHashMap<>();

    @Override
    public Optional<UserAccount> findById(UserId userId) {
        return Optional.ofNullable(storage.get(userId.getValue()));
    }

    @Override
    public Optional<UserAccount> findByEmail(EmailAddress emailAddress) {
        UUID userId = emailIndex.get(emailAddress.getValue());
        return userId == null ? Optional.empty() : Optional.ofNullable(storage.get(userId));
    }

    @Override
    public List<UserAccount> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public boolean existsByEmail(EmailAddress emailAddress) {
        return emailIndex.containsKey(emailAddress.getValue());
    }

    @Override
    public UserAccount save(UserAccount userAccount) {
        storage.put(userAccount.getId().getValue(), userAccount);
        emailIndex.put(userAccount.getEmail().getValue(), userAccount.getId().getValue());
        return userAccount;
    }
}
