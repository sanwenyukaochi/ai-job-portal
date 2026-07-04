package com.portal.job.user.service.dataaccess.user.repository;

import com.portal.job.user.service.dataaccess.user.entity.UserAccountEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAccountJpaRepository extends JpaRepository<UserAccountEntity, UUID> {
    Optional<UserAccountEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
