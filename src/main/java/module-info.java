module com.dorm {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    // JavaFX reflection & FXML access
    opens com.dorm to javafx.fxml;
    exports com.dorm;

    exports com.dorm.model;
    exports com.dorm.model.enums;

    exports com.dorm.dao;
    exports com.dorm.dao.impl;
    exports com.dorm.db;

    exports com.dorm.service;
    exports com.dorm.service.impl;
    exports com.dorm.exception;
    exports com.dorm.util;

    exports com.dorm.ui.controller;
    opens com.dorm.ui.controller to javafx.fxml;
}
