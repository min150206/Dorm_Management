package com.ui.controller;

import com.dorm.App;
import com.dorm.dao.impl.AccountDAOImpl;
import com.dorm.logic.code.AuthServiceImpl;
import com.dorm.logic.code.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import java.util.*;

public class Login {
    @FXML private TextField loginEmail;
    @FXML private PasswordField loginPassword;
    @FXML private Label loginMessage;
    @FXML private Button setupAdminButton;
    private final AuthServiceImpl auth = new AuthServiceImpl();

    public static void show(Stage stage) throws Exception {
        Session.logout();
        Parent root = FXMLLoader.load(App.class.getResource("LoginPage.fxml"));
        Scene scene = new Scene(root, 1100, 730);
        scene.getStylesheets().add(Objects.requireNonNull(App.class.getResource("dorm.css")).toExternalForm());
        stage.setTitle("Dormitory Management");
        stage.setMinWidth(900); stage.setMinHeight(620);
        stage.setScene(scene); stage.show();
    }
    @FXML private void initialize() throws Exception {
        boolean first = new AccountDAOImpl().count() == 0;
        setupAdminButton.setVisible(first); setupAdminButton.setManaged(first);
    }
    private Stage stage() { return (Stage)loginEmail.getScene().getWindow(); }
    private void error(Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR, Objects.toString(e.getMessage(), "Operation failed."), ButtonType.OK);
        alert.initOwner(stage()); alert.setHeaderText("Please check the input"); alert.showAndWait();
    }
    @FXML private void logIn() {
        try {
            if (auth.login(loginEmail.getText(), loginPassword.getText()).isEmpty()) {
                loginMessage.setText("Email or password is incorrect."); return;
            }
            new PrimaryController(stage()).showDashboard();
        } catch (Exception e) { error(e); }
    }
    @FXML private void createAdmin() {
        Map<String,String> values = form("First admin setup", "Email", "Password");
        if (values == null) return;
        try {
            auth.registerAdmin(values.get("Email"), values.get("Password"));
            setupAdminButton.setVisible(false); setupAdminButton.setManaged(false);
            loginMessage.setText("Admin created. Log in with the new account.");
        } catch (Exception e) { error(e); }
    }
    @FXML private void registerStudent() {
        Map<String,String> values = form("Register student", "Email", "Password", "Student ID", "Full name", "Phone", "Gender");
        if (values == null) return;
        try {
            auth.registerStudent(values.get("Email"),values.get("Password"),values.get("Student ID"),
                    values.get("Full name"),values.get("Phone"),values.get("Gender"));
            loginMessage.setText("Student registered. Log in with your new account.");
        } catch (Exception e) { error(e); }
    }
    private Map<String,String> form(String title, String... fields) {
        Dialog<Map<String,String>> dialog = new Dialog<>();
        dialog.setTitle(title); dialog.initOwner(stage());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK,ButtonType.CANCEL);
        GridPane grid = new GridPane();grid.setPadding(new Insets(20));grid.setHgap(15);grid.setVgap(12);
        Map<String,Control> inputs = new LinkedHashMap<>();
        for (int i=0;i<fields.length;i++) {
            String name=fields[i];Control input;
            if (name.equals("Gender")) {
                ComboBox<String> choice=new ComboBox<>();choice.getItems().addAll("MALE","FEMALE");choice.setValue("MALE");input=choice;
            } else {
                TextField text=name.equals("Password")?new PasswordField():new TextField();text.setPrefWidth(300);input=text;
            }
            inputs.put(name,input);grid.addRow(i,new Label(name),input);
        }
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(b->{
            if(b!=ButtonType.OK)return null;
            Map<String,String> result=new LinkedHashMap<>();
            inputs.forEach((name,input)->result.put(name,input instanceof ComboBox?Objects.toString(((ComboBox<?>)input).getValue(),""):((TextField)input).getText()));
            return result;
        });
        return dialog.showAndWait().orElse(null);
    }
    public static void main(String[] args) { App.main(args); }
}
