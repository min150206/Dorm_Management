module com.dorm {
    requires javafx.controls;
    requires javafx.fxml;
   
    //allow javafx to read pkg
    opens com.dorm to javafx.fxml;
    
    //export pkg for the outside to read
    exports com.dorm;
    
    //exports com.backend;
    //exports com.db;
    
    //UI pkg
    exports com.UI.login;
    //exports com.UI.admin;
    //exports com.UI.controller;
    //exports com.UI.student;
    
}
