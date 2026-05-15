package com.portal.job.user.service.data.access.user.entity;

import com.portal.job.data.user.service.domain.valueobject.UserStatus;
import jakarta.persistence.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "users",
        uniqueConstraints = {@UniqueConstraint(name = "uk_user_email", columnNames = "email")})
@Entity
@EntityListeners(AuditingEntityListener.class)
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "profile_image", nullable = false)
    private String profileImage;

    @OneToOne(
            targetEntity = AuthProviderEntity.class,
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            optional = true,
            mappedBy = "user",
            orphanRemoval = true)
    private AuthProviderEntity authProvider;

    @OneToMany(
            targetEntity = UserRoleEntity.class,
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            mappedBy = "user",
            orphanRemoval = true)
    private List<UserRoleEntity> roles = new ArrayList<>();

    @Column(name = "verified", nullable = false)
    private Boolean verified = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false)
    private Long createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @LastModifiedBy
    @Column(name = "updated_by", nullable = false)
    private Long updatedBy;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEntity that = (UserEntity) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
