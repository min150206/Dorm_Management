package com.dorm;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.dorm.db.DatabaseSetup;
import com.dorm.db.DatabaseConnection;
import com.ui.controller.Login;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        try {
            DatabaseSetup.initialize();
            Login.show(stage);
        } catch (Exception e) {
            Label info = new Label("Cannot connect to the dormitory database.\nSet DORM_DB_URL, DORM_DB_USER and DORM_DB_PASSWORD, then retry.\n\n" + e.getMessage());
            info.setWrapText(true);
            Button retry = new Button("Retry connection");
            retry.setOnAction(event -> { try { start(stage); } catch (IOException ex) { throw new RuntimeException(ex); } });
            VBox box = new VBox(20, info, retry); box.setPadding(new Insets(30));
            stage.setTitle("Dormitory Management - Database setup");
            stage.setScene(new Scene(box, 700, 340)); stage.show();
        }
    }

    public static void setRoot(String fxml) throws IOException {
        // Retained for the original FXML navigation example.
        if (scene == null) {
            scene = new Scene(loadFXML(fxml), 1100, 730);
            primaryStage.setScene(scene);
        } else { scene.setRoot(loadFXML(fxml)); }
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void stop() {
        DatabaseConnection.closeIfOpen();
    }

}
