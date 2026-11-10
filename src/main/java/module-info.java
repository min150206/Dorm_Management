module com.dorm {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires javafx.swing;
    requires org.postgresql.jdbc;

    //allow javafx to read pkg
    opens com.dorm to javafx.fxml;
    opens com.ui.controller to javafx.fxml;

    //export pkg for the outside to read
    exports com.dorm;

    // ── Backend packages ──────────────────────────────────────────────
    exports com.dorm.model;
    exports com.dorm.model.enums;
    exports com.dorm.dao;
    exports com.dorm.dao.impl;
    exports com.dorm.db;
    exports com.dorm.logic.itf;
    exports com.dorm.logic.code;
    exports com.ui.controller;

    // ── UI packages ───────────────────────────────────────────────────
}
