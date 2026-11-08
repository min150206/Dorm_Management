package com.dorm.dao;

import com.dorm.model.RoomType;
import java.sql.SQLException;
import java.util.Optional;

/**
 * DAO interface for the 'room_type' table.
 */
public interface RoomTypeDAO extends GenericDAO<RoomType, Integer> {

    /** Find a room type by its unique name. */
    Optional<RoomType> findByTypeName(String typeName) throws SQLException;
}
