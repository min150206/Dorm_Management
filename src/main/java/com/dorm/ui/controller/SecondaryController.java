package com.dorm.ui.controller;

import com.dorm.App;
import javafx.fxml.FXML;
import java.io.IOException;

public class SecondaryController {

    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
    }
}
