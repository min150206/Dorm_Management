package com.dorm.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Abstract base entity for all domain models.
 * Provides common identity, equality, and string representation logic.
 *
 * @param <ID> the type of the primary key (Integer for SERIAL PKs, String for VARCHAR PKs)
 */
public abstract class BaseEntity<ID extends Serializable> implements Serializable {

    protected ID id;

    public BaseEntity() {
    }

    public BaseEntity(ID id) {
        this.id = id;
    }

    /** Returns the primary key of this entity. */
    public ID getId() {
        return id;
    }

    /** Sets the primary key of this entity. */
    public void setId(ID id) {
        this.id = id;
    }

    /** Returns true if this entity has been persisted (has a non-null ID). */
    public boolean isPersisted() {
        return id != null;
    }

    /**
     * Subclasses must return the database table name for this entity.
     * Used by the abstract DAO layer for generic SQL generation.
     */
    public abstract String getTableName();

    /**
     * Subclasses must return the primary key column name.
     */
    public abstract String getIdColumnName();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity<?> that = (BaseEntity<?>) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id=" + id + "}";
    }
}
