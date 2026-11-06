module com.dorm {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    //allow javafx to read pkg
    opens com.dorm to javafx.fxml;

    //export pkg for the outside to read
    exports com.dorm;

    // ── Backend packages ──────────────────────────────────────────────
    exports com.dorm.model;
    exports com.dorm.model.enums;
    exports com.dorm.dao;
    exports com.dorm.dao.impl;
    exports com.dorm.db;

    // ── UI packages ───────────────────────────────────────────────────
    exports com.UI.login;
    //exports com.UI.admin;
    //exports com.UI.controller;
    //exports com.UI.student;
}
