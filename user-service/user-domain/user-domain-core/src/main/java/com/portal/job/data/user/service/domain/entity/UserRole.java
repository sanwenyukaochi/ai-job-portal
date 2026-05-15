package com.portal.job.data.user.service.domain.entity;

import com.portal.job.data.user.service.domain.valueobject.Role;
import com.portal.job.data.user.service.domain.valueobject.UserId;
import com.portal.job.data.user.service.domain.valueobject.UserRoleId;
import com.portal.job.domain.entity.AggregateRoot;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
public class UserRole extends AggregateRoot<UserRoleId> {
    private Role role;

    private UserId userId;
    private User user;

    private Long createdBy;
    private ZonedDateTime createdAt;
    private Long updatedBy;
    private ZonedDateTime updatedAt;

    public UserRole(Builder builder) {
        super.setId(builder.userRoleId);
        this.role = builder.role;
        this.userId = builder.userId;
        this.user = builder.user;
        this.createdBy = builder.createdBy;
        this.createdAt = builder.createdAt;
        this.updatedBy = builder.updatedBy;
        this.updatedAt = builder.updatedAt;
    }

    public static final class Builder {
        private UserRoleId userRoleId;
        private Role role;

        private UserId userId;
        private User user;

        private Long createdBy;
        private ZonedDateTime createdAt;
        private Long updatedBy;
        private ZonedDateTime updatedAt;

        private Builder() {
        }

        public Builder userRoleId(UserRoleId userRoleId) {
            this.userRoleId = userRoleId;
            return this;
        }

        public Builder role(Role role) {
            this.role = role;
            return this;
        }

        public Builder userId(UserId userId) {
            this.userId = userId;
            return this;
        }
        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder createdBy(Long createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder createdAt(ZonedDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedBy(Long updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        public Builder updatedAt(ZonedDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public UserRole build() {
            return new UserRole(this);
        }
    }
}
