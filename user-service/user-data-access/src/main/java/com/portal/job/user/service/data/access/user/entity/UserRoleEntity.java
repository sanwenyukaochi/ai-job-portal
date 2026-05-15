package com.portal.job.user.service.data.access.user.entity;

import com.portal.job.data.user.service.domain.valueobject.UserRole;
import jakarta.persistence.*;
import java.time.ZonedDateTime;
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
        name = "user_roles",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_user_role",
                    columnNames = {"user_id", "role"})
        })
@Entity
@EntityListeners(AuditingEntityListener.class)
public class UserRoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            targetEntity = UserEntity.class,
            cascade = {},
            fetch = FetchType.LAZY,
            optional = false)
    @JoinColumn(
            name = "USER_ID",
            referencedColumnName = "id",
            unique = false,
            nullable = false,
            insertable = true,
            updatable = true)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

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
        UserRoleEntity that = (UserRoleEntity) o;
        return id.equals(that.id) && user.equals(that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user);
    }
}
