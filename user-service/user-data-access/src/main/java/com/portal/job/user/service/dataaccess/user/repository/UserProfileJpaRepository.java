package com.portal.job.user.service.dataaccess.user.repository;

import com.portal.job.user.service.dataaccess.user.entity.UserProfileEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileJpaRepository extends JpaRepository<UserProfileEntity, UUID> {}
