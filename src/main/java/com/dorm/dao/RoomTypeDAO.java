package com.dorm.dao;

import com.dorm.model.RoomType;
import java.sql.SQLException;
import java.util.Optional;

public interface RoomTypeDAO extends GenericDAO<RoomType, Integer> {

    Optional<RoomType> findByTypeName(String typeName) throws SQLException;
}
