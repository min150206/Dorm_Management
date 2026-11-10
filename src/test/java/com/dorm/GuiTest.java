package com.dorm;

import com.dorm.db.*;
import com.dorm.model.*;
import com.dorm.logic.code.Session;
import com.dorm.logic.code.*;
import com.ui.controller.PrimaryController;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.concurrent.*;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named="DORM_TEST_DB_URL", matches=".*[/]dorm_test(?:[?].*)?")
class GuiTest {
    @Test void loginTabsAndStudentViewWork() throws Exception {
        System.setProperty("DORM_DB_URL",System.getenv("DORM_TEST_DB_URL"));
        DatabaseSetup.initialize();
        try(Statement s=DatabaseConnection.getInstance().getConnection().createStatement()) {
            s.execute("TRUNCATE invoice, request, contract, room, room_type, student, account RESTART IDENTITY CASCADE");
        }
        Session.logout();
        AuthServiceImpl auth=new AuthServiceImpl();
        auth.registerAdmin("admin@dorm.test","admin123");
        auth.registerStudent("student@dorm.test","student123","SV001","Nguyen An","0901234567","MALE");
        auth.login("admin@dorm.test","admin123");
        RoomServiceImpl rooms=new RoomServiceImpl();
        RoomType type=rooms.addRoomType("Four beds",4,500000);
        Room room=rooms.addRoom(type.getId(),"101","A");
        rooms.addRoom(type.getId(),"102","A");
        Contract c=new ContractServiceImpl().createContract("SV001",room.getId(),Date.valueOf(LocalDate.now()),Date.valueOf(LocalDate.now().plusMonths(6)));
        new RequestServiceImpl().submitRequest("SV001",room.getId(),"Broken ceiling fan","The fan at bed 1 does not start.");
        new InvoiceServiceImpl().createInvoice(c.getId(),"November room fee",500000);
        auth.registerStaff("staff@dorm.test","staff123");
        CountDownLatch started=new CountDownLatch(1);
        Platform.startup(started::countDown);assertTrue(started.await(15,TimeUnit.SECONDS));
        CompletableFuture<Void> result=new CompletableFuture<>();
        Platform.runLater(()->{
            Stage stage=new Stage();
            try {
                for(String file:new String[]{"primary.fxml","secondary.fxml","Log.fxml"}) {
                    assertNotNull(javafx.fxml.FXMLLoader.load(App.class.getResource(file)));
                }
                PrimaryController view=new PrimaryController(stage);view.showLogin();
                snapshot(stage,"login");
                ((TextField)stage.getScene().lookup("#login-email")).setText("admin@dorm.test");
                ((PasswordField)stage.getScene().lookup("#login-password")).setText("admin123");
                ((Button)stage.getScene().lookup("#login-button")).fire();
                TabPane tabs=(TabPane)stage.getScene().lookup("#main-tabs");assertNotNull(tabs);assertEquals(7,tabs.getTabs().size());
                for(Tab tab:tabs.getTabs()) {
                    tabs.getSelectionModel().select(tab);assertNotNull(tab.getContent());
                    snapshot(stage,tab.getText().toLowerCase().replace(' ','-'));
                }
                auth.login("student@dorm.test","student123");view.showDashboard();
                tabs=(TabPane)stage.getScene().lookup("#main-tabs");assertEquals(5,tabs.getTabs().size());
                assertEquals("My profile",tabs.getTabs().get(0).getText());
                for(Tab tab:tabs.getTabs()) {tabs.getSelectionModel().select(tab);assertNotNull(tab.getContent());}
                TableView<?> invoices=(TableView<?>)stage.getScene().lookup("#invoices-table");assertEquals(1,invoices.getItems().size());
                snapshot(stage,"student-invoices");
                result.complete(null);
            } catch(Throwable e) {result.completeExceptionally(e);}
            finally {stage.close();Session.logout();DatabaseConnection.closeIfOpen();}
        });
        result.get(45,TimeUnit.SECONDS);Platform.exit();
    }
    private void snapshot(Stage stage,String name) throws Exception {
        stage.getScene().getRoot().applyCss();stage.getScene().getRoot().layout();
        Path dir=Path.of("target","gui-screenshots");Files.createDirectories(dir);
        ImageIO.write(SwingFXUtils.fromFXImage(stage.getScene().snapshot(null),null),"png",dir.resolve(name+".png").toFile());
    }
}
