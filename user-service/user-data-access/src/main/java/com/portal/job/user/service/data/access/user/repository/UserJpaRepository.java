package com.portal.job.user.service.data.access.user.repository;

import com.portal.job.data.user.service.domain.valueobject.Role;
import com.portal.job.user.service.data.access.user.entity.UserEntity;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    UserEntity findByEmail(String email);

    Optional<UserEntity> findByRole_Role(Role roleRole);

    boolean existsByEmail(String email);
}
