package com.portal.job.user.service.domain.entity;

import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public abstract class BaseEntity<ID> {
    @Getter(AccessLevel.PUBLIC)
    @Setter(AccessLevel.PUBLIC)
    private ID id;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity<?> that = (BaseEntity<?>) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
