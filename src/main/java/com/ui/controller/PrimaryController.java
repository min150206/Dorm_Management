package com.ui.controller;

import com.dorm.dao.impl.AccountDAOImpl;
import com.dorm.model.*;
import com.dorm.model.enums.*;
import com.dorm.logic.code.Session;
import com.dorm.logic.code.*;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

// A small JavaFX screen class. Services do the checking and database work.
public class PrimaryController {
    private final Stage stage;
    private final AuthServiceImpl auth = new AuthServiceImpl();
    private final StudentServiceImpl students = new StudentServiceImpl();
    private final RoomServiceImpl rooms = new RoomServiceImpl();
    private final ContractServiceImpl contracts = new ContractServiceImpl();
    private final RequestServiceImpl requests = new RequestServiceImpl();
    private final InvoiceServiceImpl invoices = new InvoiceServiceImpl();
    private TabPane tabs;
    private String studentId;
    private final Label message = new Label();

    public PrimaryController() { this(null); }
    public PrimaryController(Stage stage) { this.stage = stage; }

    @javafx.fxml.FXML
    private void switchToSecondary() throws java.io.IOException {
        com.dorm.App.setRoot("secondary");
    }
    interface Action { void run() throws Exception; }
    private void run(Action action) {
        try { action.run(); }
        catch (Exception e) {
            String text = e.getMessage() == null ? e.toString() : e.getMessage();
            if (e instanceof java.sql.SQLException) {
                String state = ((java.sql.SQLException)e).getSQLState();
                if ("23503".equals(state)) text = "This record is still used by a contract, invoice or request, or a linked ID is missing.";
                if ("23505".equals(state)) text = "That email, student ID, room number or type name already exists.";
            }
            Alert a = new Alert(Alert.AlertType.ERROR, text, ButtonType.OK);
            a.initOwner(stage); a.setHeaderText("Please check the input"); a.showAndWait();
        }
    }
    private Button button(String text, Action action) {
        Button b = new Button(text); b.setOnAction(e -> run(action)); return b;
    }
    private void show(javafx.scene.Parent root) {
        Scene scene = new Scene(root, 1100, 730);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/dorm/dorm.css")).toExternalForm());
        stage.setTitle("Dormitory Management"); stage.setScene(scene); stage.setMinWidth(900); stage.setMinHeight(620); stage.show();
    }
    public void showLogin() {
        run(() -> Login.show(stage));
    }
    private void registerStudent() {
        Map<String,String> f = form("Register student", fields("Email", "", "Password", "", "Student ID", "", "Full name", "", "Phone", "", "Gender", "MALE"));
        if (f == null) return;
        run(() -> {
            auth.registerStudent(f.get("Email"), f.get("Password"), f.get("Student ID"), f.get("Full name"), f.get("Phone"), f.get("Gender"));
            if (stage.getScene().lookup("#main-tabs") != null) refresh();
            message.setText("Student registered. The new account can now log in.");
        });
    }
    public void showDashboard() throws Exception {
        Session.requireLogin();
        boolean manager = Session.getAccount().getRole() != Role.STUDENT;
        studentId = manager ? null : auth.getStudentProfile(Session.getAccount().getId()).orElseThrow().getId();
        BorderPane root = new BorderPane(); root.setPadding(new Insets(20));
        Label title = new Label("Dormitory Management"); title.getStyleClass().add("title");
        Label user = new Label(Session.getAccount().getEmail() + "  |  " + Session.getAccount().getRole());
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox top = new HBox(16, new VBox(5, title, user), spacer,
                button("Change password", () -> changePassword()), button("Log out", () -> { message.setText(""); showLogin(); }));
        top.setPadding(new Insets(0,0,20,0)); root.setTop(top);
        tabs = new TabPane(); tabs.setId("main-tabs"); tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        if (manager) {
            addTab("Students", () -> studentPane()); addTab("Room types", () -> typePane());
        } else { addTab("My profile", () -> studentPane()); }
        addTab("Rooms", () -> roomPane()); addTab("Contracts", () -> contractPane());
        addTab("Requests", () -> requestPane()); addTab("Invoices", () -> invoicePane());
        if (Session.getAccount().getRole() == Role.ADMIN) addTab("Accounts", () -> accountPane());
        root.setCenter(tabs);
        message.setText("Select a row before using an update button. Fees are in VND.");
        message.setPadding(new Insets(12,0,0,0)); root.setBottom(message); show(root);
        refresh();
    }
    interface PaneFactory { javafx.scene.Node create() throws Exception; }
    private void addTab(String name, PaneFactory factory) {
        Tab tab = new Tab(name); tab.setUserData(factory);
        tab.setOnSelectionChanged(e -> { if (tab.isSelected()) run(() -> tab.setContent(factory.create())); });
        tabs.getTabs().add(tab);
    }
    private void refresh() throws Exception {
        Tab selected = tabs.getSelectionModel().getSelectedItem();
        selected.setContent(((PaneFactory)selected.getUserData()).create());
    }
    private boolean manager() { return Session.getAccount().getRole() != Role.STUDENT; }
    private VBox pane(String heading, TableView<?> table, javafx.scene.Node... controls) {
        Label h = new Label(heading); h.getStyleClass().add("heading");
        FlowPane bar = new FlowPane(9, 9); bar.getChildren().addAll(controls);
        VBox box = new VBox(14, h, bar, table); box.setPadding(new Insets(20,0,0,0)); VBox.setVgrow(table, Priority.ALWAYS); return box;
    }
    private <T> TableView<T> table(List<T> rows, String[] names, List<Function<T,Object>> values) {
        TableView<T> table = new TableView<>(FXCollections.observableArrayList(rows));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPlaceholder(new Label("No records found."));
        for (int i=0;i<names.length;i++) {
            final Function<T,Object> value = values.get(i);
            TableColumn<T,String> c = new TableColumn<>(names[i]);
            c.setCellValueFactory(p -> new ReadOnlyStringWrapper(Objects.toString(value.apply(p.getValue()), "")));
            table.getColumns().add(c);
        }
        return table;
    }
    private <T> T selected(TableView<T> table) {
        T value = table.getSelectionModel().getSelectedItem();
        if (value == null) throw new IllegalArgumentException("Select a row first."); return value;
    }
    private Map<String,String> fields(String... pairs) {
        Map<String,String> f = new LinkedHashMap<>();
        for (int i=0;i<pairs.length;i+=2) f.put(pairs[i], pairs[i+1]); return f;
    }
    private Map<String,String> form(String title, Map<String,String> values) {
        Dialog<Map<String,String>> dialog = new Dialog<>(); dialog.initOwner(stage); dialog.setTitle(title);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        GridPane grid = new GridPane(); grid.setHgap(14); grid.setVgap(12); grid.setPadding(new Insets(20));
        Map<String,Control> inputs = new LinkedHashMap<>(); int row = 0;
        for (Map.Entry<String,String> entry : values.entrySet()) {
            String name = entry.getKey(); Control input;
            if (name.equals("Gender") || name.equals("Room status")) {
                ComboBox<String> choice = new ComboBox<>();
                if (name.equals("Gender")) choice.getItems().addAll("MALE", "FEMALE");
                else choice.getItems().addAll("AVAILABLE", "MAINTENANCE");
                choice.setValue(entry.getValue()); input = choice;
            } else if (name.contains("date")) {
                DatePicker date = new DatePicker(LocalDate.parse(entry.getValue())); date.setEditable(false); input = date;
            } else {
                TextField text = name.toLowerCase().contains("password") ? new PasswordField() : new TextField();
                text.setText(entry.getValue()); text.setPrefWidth(310); input = text;
            }
            grid.addRow(row++, new Label(name), input); inputs.put(name, input);
        }
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(b -> {
            if (b != ButtonType.OK) return null;
            Map<String,String> result = new LinkedHashMap<>();
            inputs.forEach((name, input) -> {
                String value;
                if (input instanceof ComboBox) value = Objects.toString(((ComboBox<?>)input).getValue(), "");
                else if (input instanceof DatePicker) value = Objects.toString(((DatePicker)input).getValue(), "");
                else value = ((TextField)input).getText();
                result.put(name, value);
            });
            return result;
        });
        return dialog.showAndWait().orElse(null);
    }
    private int number(Map<String,String> f, String name) { return Integer.parseInt(f.get(name).trim()); }
    private void changePassword() throws Exception {
        Map<String,String> f = form("Change password", fields("Old password", "", "New password", ""));
        if (f != null) {
            if (!auth.changePassword(Session.getAccount().getId(), f.get("Old password"), f.get("New password")))
                throw new IllegalArgumentException("Old password is incorrect.");
            message.setText("Password changed.");
        }
    }
    private javafx.scene.Node studentPane() throws Exception {
        List<Student> data = manager() ? students.getAllStudents() : List.of(students.getStudentById(studentId).orElseThrow());
        TableView<Student> t = table(data, new String[]{"Student ID","Full name","Phone","Gender","Account ID"},
                List.of(Student::getId, Student::getFullName, Student::getPhone, Student::getGender, Student::getAccountId)); t.setId("students-table");
        TextField search = new TextField(); search.setPromptText("Search name");
        Button find = button("Search", () -> t.setItems(FXCollections.observableArrayList(students.searchStudentsByName(search.getText()))));
        Button edit = button("Update selected", () -> {
            Student s = selected(t);
            Map<String,String> f = form("Update student " + s.getId(), fields("Full name",s.getFullName(),"Phone",s.getPhone(),"Gender",s.getGender().name()));
            if (f != null) { students.updateStudent(new Student(s.getId(),s.getAccountId(), f.get("Full name"),f.get("Phone"),Gender.fromString(f.get("Gender")))); refresh(); }
        });
        if (!manager()) return pane("My student profile",t,edit,button("Refresh", () -> refresh()));
        return pane("Student records",t,search,find,button("Add student", () -> registerStudent()),edit,
                button("Delete selected", () -> { Student s=selected(t); if (confirm("Delete student " + s.getId() + "?")) { students.deleteStudent(s.getId()); refresh(); } }),button("Refresh", () -> refresh()));
    }
    private boolean confirm(String text) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,text,ButtonType.OK,ButtonType.CANCEL); a.initOwner(stage); a.setHeaderText(null);
        return a.showAndWait().orElse(ButtonType.CANCEL)==ButtonType.OK;
    }
    private javafx.scene.Node typePane() throws Exception {
        TableView<RoomType> t=table(rooms.getAllRoomTypes(),new String[]{"Type ID","Name","Capacity","Price VND"},
                List.of(RoomType::getId,RoomType::getTypeName,RoomType::getCapacity,RoomType::getPrice));
        return pane("Room types and prices",t,button("Add type",()->{
            Map<String,String> f=form("Add room type",fields("Type name","","Capacity","4","Price","500000"));
            if(f!=null){rooms.addRoomType(f.get("Type name"),number(f,"Capacity"),Long.parseLong(f.get("Price")));refresh();}
        }),button("Update selected",()->{
            RoomType v=selected(t);Map<String,String> f=form("Update room type",fields("Type name",v.getTypeName(),"Capacity",""+v.getCapacity(),"Price",""+v.getPrice()));
            if(f!=null){rooms.updateRoomType(new RoomType(v.getId(),f.get("Type name"),number(f,"Capacity"),Long.parseLong(f.get("Price"))));refresh();}
        }),button("Refresh",()->refresh()));
    }
    private javafx.scene.Node roomPane() throws Exception {
        TableView<Room> t=table(rooms.getAllRooms(),new String[]{"Room ID","Building","Room number","Type ID","Occupancy","Status"},
                List.of(Room::getId,Room::getBuilding,Room::getRoomNumber,Room::getRoomTypeId,Room::getCurrentOccupancy,Room::getRoomStatus)); t.setId("rooms-table");
        TextField building=new TextField();building.setPromptText("Building");
        VBox box=pane("Rooms and availability",t,building,button("Find building",()->t.setItems(FXCollections.observableArrayList(rooms.getRoomsByBuilding(building.getText().trim())))),
                button("Available only",()->t.setItems(FXCollections.observableArrayList(rooms.getAvailableRooms()))),button("Refresh",()->refresh()));
        if(manager()) ((FlowPane)box.getChildren().get(1)).getChildren().addAll(button("Add room",()->{
            Map<String,String> f=form("Add room - see Room types for IDs",fields("Type ID","","Room number","","Building",""));
            if(f!=null){rooms.addRoom(number(f,"Type ID"),f.get("Room number"),f.get("Building"));refresh();}
        }),button("Update selected",()->{
            Room v=selected(t);Map<String,String> f=form("Update room",fields("Type ID",""+v.getRoomTypeId(),"Room number",v.getRoomNumber(),"Building",v.getBuilding(),"Room status",v.getRoomStatus()==RoomStatus.MAINTENANCE?"MAINTENANCE":"AVAILABLE"));
            if(f!=null){rooms.updateRoom(new Room(v.getId(),number(f,"Type ID"),f.get("Room number"),f.get("Building"),v.getCurrentOccupancy(),RoomStatus.fromString(f.get("Room status"))));refresh();}
        }),button("Delete selected",()->{Room r=selected(t);if(confirm("Delete room " + r.getRoomNumber()+"?")){rooms.deleteRoom(r.getId());refresh();}}));
        return box;
    }
    private javafx.scene.Node contractPane() throws Exception {
        List<Contract> data=manager()?contracts.getAllContracts():contracts.getContractsByStudent(studentId);
        TableView<Contract> t=table(data,new String[]{"Contract ID","Student ID","Room ID","Start date","End date","Status"},
                List.of(Contract::getId,Contract::getStudentId,Contract::getRoomId,Contract::getStartDate,Contract::getEndDate,Contract::getContractStatus)); t.setId("contracts-table");
        VBox box=pane("Accommodation contracts",t,button("Refresh",()->refresh()));
        if(manager()) ((FlowPane)box.getChildren().get(1)).getChildren().addAll(button("Create contract",()->{
            Map<String,String> f=form("Create contract",fields("Student ID","","Room ID","","Start date",LocalDate.now().toString(),"End date",LocalDate.now().plusMonths(6).toString()));
            if(f!=null){contracts.createContract(f.get("Student ID"),number(f,"Room ID"),Date.valueOf(f.get("Start date")),Date.valueOf(f.get("End date")));refresh();}
        }),button("Cancel selected",()->{Contract c=selected(t);if(confirm("Terminate contract " + c.getId()+"?")){contracts.cancelContract(c.getId());refresh();}}),
                button("Expire old contracts",()->{int count=contracts.expireOldContracts();refresh();message.setText(count+" contracts expired.");}));
        return box;
    }
    private javafx.scene.Node requestPane() throws Exception {
        List<Request> data=manager()?requests.getAllRequests():requests.getRequestsByStudent(studentId);
        TableView<Request> t=table(data,new String[]{"ID","Student","Room","Title","Status","Requested","Resolved by"},
                List.of(Request::getId,Request::getStudentId,Request::getRoomId,Request::getTitle,Request::getRequestStatus,Request::getRequestedTime,Request::getResolvedBy)); t.setId("requests-table");
        VBox box=pane("Maintenance requests",t,button("Submit request",()->{
            Map<String,String> f=form("Submit maintenance request",fields("Student ID",manager()?"":studentId,"Room ID","","Title","","Description",""));
            if(f!=null){requests.submitRequest(f.get("Student ID"),number(f,"Room ID"),f.get("Title"),f.get("Description"));refresh();}
        }),button("View details",()->{Request r=selected(t);Alert a=new Alert(Alert.AlertType.INFORMATION,r.getDescription()+"\nResolved: "+Objects.toString(r.getResolvedTime(),"Not yet"));a.initOwner(stage);a.setHeaderText(r.getTitle());a.showAndWait();}),button("Refresh",()->refresh()));
        if(manager()) ((FlowPane)box.getChildren().get(1)).getChildren().addAll(
                button("Start processing",()->{requests.resolveRequest(selected(t).getId(),Session.getAccount().getId(),RequestStatus.PROCESSING);refresh();}),
                button("Resolve selected",()->{requests.resolveRequest(selected(t).getId(),Session.getAccount().getId(),RequestStatus.RESOLVED);refresh();}));
        return box;
    }
    private javafx.scene.Node invoicePane() throws Exception {
        List<Invoice> data=new ArrayList<>();
        if(manager()) data=invoices.getAllInvoices();
        else for(Contract c:contracts.getContractsByStudent(studentId)) data.addAll(invoices.getInvoicesByContract(c.getId()));
        TableView<Invoice> t=table(data,new String[]{"Invoice ID","Contract ID","Title","Fee VND","Status","Paid time"},
                List.of(Invoice::getId,Invoice::getContractId,Invoice::getTitle,Invoice::getFee,Invoice::getInvoiceStatus,Invoice::getPaidTime)); t.setId("invoices-table");
        VBox box=pane("Invoices and recorded payments",t,button("Unpaid only",()->t.setItems(FXCollections.observableArrayList(manager()?invoices.getUnpaidInvoices():invoices.getUnpaidInvoicesByStudent(studentId)))),button("Refresh",()->refresh()));
        if(manager()) ((FlowPane)box.getChildren().get(1)).getChildren().addAll(button("Create invoice",()->{
            Map<String,String> f=form("Create invoice",fields("Contract ID","","Title","Room fee","Fee","500000"));
            if(f!=null){invoices.createInvoice(number(f,"Contract ID"),f.get("Title"),Long.parseLong(f.get("Fee")));refresh();}
        }),button("Record payment",()->{Invoice i=selected(t);if(confirm("Confirm money received for invoice "+i.getId()+"?")){invoices.payInvoice(i.getId());refresh();}}));
        return box;
    }
    private javafx.scene.Node accountPane() throws Exception {
        Session.requireAdmin();
        TableView<Account> t=table(new AccountDAOImpl().findAll(),new String[]{"Account ID","Email","Role"},List.of(Account::getId,Account::getEmail,Account::getRole));
        return pane("Account administration",t,button("Add staff",()->{
            Map<String,String> f=form("Add staff account",fields("Email","","Password",""));
            if(f!=null){auth.registerStaff(f.get("Email"),f.get("Password"));refresh();}
        }),button("Add admin",()->{
            Map<String,String> f=form("Add admin account",fields("Email","","Password",""));
            if(f!=null){auth.registerAdmin(f.get("Email"),f.get("Password"));refresh();}
        }),button("Refresh",()->refresh()));
    }
}
