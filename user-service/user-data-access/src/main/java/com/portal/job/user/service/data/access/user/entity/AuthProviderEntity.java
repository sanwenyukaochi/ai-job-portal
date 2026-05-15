package com.portal.job.user.service.data.access.user.entity;

import com.portal.job.data.user.service.domain.valueobject.AuthProvider;
import jakarta.persistence.*;
import java.util.Objects;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "auth_providers",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_auth_provider_openid",
                    columnNames = {"auth_provider", "open_id"})
        })
@Entity
public class AuthProviderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(
            targetEntity = UserEntity.class,
            cascade = {},
            fetch = FetchType.LAZY,
            optional = false,
            mappedBy = "",
            orphanRemoval = false)
    @JoinColumn(
            name = "USER_ID",
            referencedColumnName = "id",
            unique = true,
            nullable = false,
            insertable = true,
            updatable = true)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider authProvider = AuthProvider.LOCAL;

    private String openId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthProviderEntity that = (AuthProviderEntity) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
