package com.dorm.ui.controller;

import com.dorm.App;
import javafx.fxml.FXML;
import java.io.IOException;

public class PrimaryController {

    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }
}
