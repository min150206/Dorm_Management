package com.dorm.dao;

import com.dorm.model.BaseEntity;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/*
 * Generic CRUD operations for DAO implementations.
 */
public interface GenericDAO<T extends BaseEntity<ID>, ID extends Serializable> {

    // Inserts a new entity and sets the generated ID
    T save(T entity) throws SQLException;

    // Updates an existing record by ID
    T update(T entity) throws SQLException;

    boolean deleteById(ID id) throws SQLException;

    Optional<T> findById(ID id) throws SQLException;

    List<T> findAll() throws SQLException;

    long count() throws SQLException;

    boolean existsById(ID id) throws SQLException;
}
