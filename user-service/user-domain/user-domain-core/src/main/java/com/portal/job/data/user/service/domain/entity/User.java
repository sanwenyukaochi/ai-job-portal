package com.portal.job.data.user.service.domain.entity;

import com.portal.job.data.user.service.domain.valueobject.UserId;
import com.portal.job.data.user.service.domain.valueobject.UserRoleId;
import com.portal.job.domain.entity.AggregateRoot;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
public class User extends AggregateRoot<UserId> {
    private String username;
    private String password;
    private String email;
    private String phone;
    private String profileImage;
    private Boolean accountNonLocked;
    private Boolean accountNonExpired;
    private Boolean credentialsNonExpired;
    private Boolean enabled;

    private UserRoleId roleId;
    private UserRole userRole;

    private Long createdBy;
    private ZonedDateTime createdAt;
    private Long updatedBy;
    private ZonedDateTime updatedAt;

    public User(Builder builder) {
        super.setId(builder.userId);
        this.username = builder.username;
        this.password = builder.password;
        this.email = builder.email;
        this.phone = builder.phone;
        this.profileImage = builder.profileImage;
        this.accountNonLocked = builder.accountNonLocked;
        this.accountNonExpired = builder.accountNonExpired;
        this.credentialsNonExpired = builder.credentialsNonExpired;
        this.enabled = builder.enabled;
        this.roleId = builder.roleId;
        this.userRole = builder.userRole;
        this.createdBy = builder.createdBy;
        this.createdAt = builder.createdAt;
        this.updatedBy = builder.updatedBy;
        this.updatedAt = builder.updatedAt;
    }

    public static final class Builder {
        private UserId userId;
        private String username;
        private String password;
        private String email;
        private String phone;
        private String profileImage;
        private Boolean accountNonLocked;
        private Boolean accountNonExpired;
        private Boolean credentialsNonExpired;
        private Boolean enabled;

        private UserRoleId roleId;
        private UserRole userRole;

        private Long createdBy;
        private ZonedDateTime createdAt;
        private Long updatedBy;
        private ZonedDateTime updatedAt;
        private Builder() {

        }

        public Builder userId(UserId userId) {
            this.userId = userId;
            return this;
        }
        public Builder username(String username) {
            this.username = username;
            return this;
        }
        public Builder password(String password) {
            this.password = password;
            return this;
        }
        public Builder email(String email) {
            this.email = email;
            return this;
        }
        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }
        public Builder profileImage(String profileImage) {
            this.profileImage = profileImage;
            return this;
        }
        public Builder accountNonLocked(Boolean accountNonLocked) {
            this.accountNonLocked = accountNonLocked;
            return this;
        }
        public Builder accountNonExpired(Boolean accountNonExpired) {
            this.accountNonExpired = accountNonExpired;
            return this;
        }
        public Builder credentialsNonExpired(Boolean credentialsNonExpired) {
            this.credentialsNonExpired = credentialsNonExpired;
            return this;
        }
        public Builder enabled(Boolean enabled) {
            this.enabled = enabled;
            return this;
        }
        public Builder roleId(UserRoleId roleId) {
            this.roleId = roleId;
            return this;
        }
        public Builder userRole(UserRole userRole) {
            this.userRole = userRole;
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
        public User build() {
            return new User(this);
        }
    }
}
