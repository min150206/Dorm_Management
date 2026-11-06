package com.dorm.dao;

import com.dorm.model.BaseEntity;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Generic Data Access Object interface.
 * Defines the standard CRUD contract that every DAO must fulfil.
 *
 * @param <T>  the entity type (must extend BaseEntity)
 * @param <ID> the primary-key type (Integer or String)
 */
public interface GenericDAO<T extends BaseEntity<ID>, ID extends Serializable> {

    /** Persist a new entity and return it with its generated ID. */
    T save(T entity) throws SQLException;

    /** Update an existing entity and return the updated version. */
    T update(T entity) throws SQLException;

    /** Delete the entity with the given primary key. Returns true if a row was removed. */
    boolean deleteById(ID id) throws SQLException;

    /** Find an entity by its primary key. */
    Optional<T> findById(ID id) throws SQLException;

    /** Return every row in the table. */
    List<T> findAll() throws SQLException;

    /** Count the total number of rows. */
    long count() throws SQLException;

    /** Check whether a row with the given primary key exists. */
    boolean existsById(ID id) throws SQLException;
}
