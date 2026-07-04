package com.portal.job.user.service.domain.valueobject;

import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;

public abstract class BaseId<T> {
    @Getter(AccessLevel.PUBLIC)
    private final T value;

    protected BaseId(T value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseId<?> baseId = (BaseId<?>) o;
        return value.equals(baseId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
