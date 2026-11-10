package com.dorm;

import com.dorm.dao.impl.*;
import com.dorm.db.*;
import com.dorm.model.*;
import com.dorm.model.enums.*;
import com.dorm.logic.code.Session;
import com.dorm.logic.code.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named="DORM_TEST_DB_URL", matches=".*[/]dorm_test(?:[?].*)?")
class WorkflowTest {
    AuthServiceImpl auth = new AuthServiceImpl();
    RoomServiceImpl rooms = new RoomServiceImpl();
    ContractServiceImpl contracts = new ContractServiceImpl();
    RequestServiceImpl requests = new RequestServiceImpl();
    InvoiceServiceImpl invoices = new InvoiceServiceImpl();
    Account admin;
    Room room;
    @BeforeAll static void setupDatabase() throws Exception {
        System.setProperty("DORM_DB_URL", System.getenv("DORM_TEST_DB_URL"));
        DatabaseSetup.initialize();
    }
    @BeforeEach void fixtures() throws Exception {
        Session.logout();
        try (Statement s=DatabaseConnection.getInstance().getConnection().createStatement()) {
            s.execute("TRUNCATE invoice, request, contract, room, room_type, student, account RESTART IDENTITY CASCADE");
        }
        admin = auth.registerAdmin("admin@dorm.test","admin123");
        auth.registerStudent("sv1@dorm.test","student123","SV001","Nguyen An","0901234567","MALE");
        auth.registerStudent("sv2@dorm.test","student123","SV002","Tran Binh","0912345678","FEMALE");
        auth.login("admin@dorm.test","admin123");
        RoomType type=rooms.addRoomType("Single",1,500000);
        room=rooms.addRoom(type.getId(),"101","A");
    }
    Contract contract(String student) throws Exception {
        return contracts.createContract(student,room.getId(),Date.valueOf(LocalDate.now()),Date.valueOf(LocalDate.now().plusMonths(6)));
    }
    @Test void registrationLoginPasswordAndDuplicates() throws Exception {
        assertTrue(auth.login(" ADMIN@DORM.TEST ","admin123").isPresent());
        assertFalse(new AccountDAOImpl().findById(admin.getId()).orElseThrow().getPassword().equals("admin123"));
        assertThrows(IllegalArgumentException.class,()->auth.registerStudent("new@dorm.test","student123","SV001","Name","0901234567","MALE"));
        assertFalse(auth.isEmailTaken("new@dorm.test"));
        assertThrows(IllegalArgumentException.class,()->auth.registerStudent("bad","student123","S3","Name","123","MALE"));
        assertTrue(auth.changePassword(admin.getId(),"admin123","newpass123"));
        assertTrue(auth.login("admin@dorm.test","newpass123").isPresent());
        assertTrue(auth.login("admin@dorm.test","wrong").isEmpty());
        assertNull(Session.getAccount());
    }
    @Test void registrationRollsBackIfProfileInsertFails() throws Exception {
        try(Statement s=DatabaseConnection.getInstance().getConnection().createStatement()) {
            s.execute("ALTER TABLE student ADD CONSTRAINT test_reject_name CHECK (full_name <> 'Reject')");
        }
        try {
            assertThrows(SQLException.class,()->auth.registerStudent("rollback@dorm.test","student123","SV003","Reject","0901234567","MALE"));
            assertFalse(auth.isEmailTaken("rollback@dorm.test"));
            assertTrue(DatabaseConnection.getInstance().getConnection().getAutoCommit());
        } finally {
            try(Statement s=DatabaseConnection.getInstance().getConnection().createStatement()) {s.execute("ALTER TABLE student DROP CONSTRAINT test_reject_name");}
        }
    }
    @Test void contractCapacityAndCancellationAreConsistent() throws Exception {
        Contract c=contract("SV001");
        assertEquals(RoomStatus.FULL,rooms.getRoomById(room.getId()).orElseThrow().getRoomStatus());
        assertTrue(rooms.getAvailableRooms().isEmpty());
        assertEquals(1,rooms.getRoomsByBuilding("A",RoomStatus.FULL).size());
        assertEquals(0,rooms.getRoomsByBuilding("A",RoomStatus.AVAILABLE).size());
        assertThrows(IllegalArgumentException.class,()->contract("SV002"));
        assertThrows(IllegalArgumentException.class,()->contract("SV001"));
        contracts.cancelContract(c.getId()); contracts.cancelContract(c.getId());
        Room freed=rooms.getRoomById(room.getId()).orElseThrow();
        assertEquals(0,freed.getCurrentOccupancy()); assertEquals(RoomStatus.AVAILABLE,freed.getRoomStatus());
        assertNotNull(contract("SV002"));
    }
    @Test void maintenanceAndDatesAreChecked() throws Exception {
        room.setRoomStatus(RoomStatus.MAINTENANCE);rooms.updateRoom(room);
        assertThrows(IllegalArgumentException.class,()->contract("SV001"));
        room.setRoomStatus(RoomStatus.AVAILABLE);rooms.updateRoom(room);
        assertThrows(IllegalArgumentException.class,()->contracts.createContract("SV001",room.getId(),Date.valueOf(LocalDate.now().plusDays(1)),Date.valueOf(LocalDate.now().plusMonths(6))));
        assertThrows(IllegalArgumentException.class,()->contracts.createContract("SV001",room.getId(),Date.valueOf(LocalDate.now()),Date.valueOf(LocalDate.now())));
        Contract c=contract("SV001");
        assertThrows(IllegalArgumentException.class,()->contracts.expireContract(c.getId()));
        try(Statement s=DatabaseConnection.getInstance().getConnection().createStatement()) {
            s.execute("UPDATE contract SET start_date=CURRENT_DATE-10,end_date=CURRENT_DATE-1 WHERE contract_id="+c.getId());
        }
        assertEquals(1,contracts.expireOldContracts());
        assertEquals(0,rooms.getRoomById(room.getId()).orElseThrow().getCurrentOccupancy());
    }
    @Test void requestsAndInvoicesCompleteTheirLifecycle() throws Exception {
        Contract c=contract("SV001");
        auth.login("sv1@dorm.test","student123");
        Request r=requests.submitRequest("SV001",room.getId(),"Broken fan","Fan in bed 1 does not turn.");
        assertThrows(IllegalArgumentException.class,()->requests.submitRequest("SV002",room.getId(),"X","X"));
        auth.login("admin@dorm.test","admin123");
        requests.resolveRequest(r.getId(),admin.getId(),RequestStatus.PROCESSING);
        Request resolved=requests.resolveRequest(r.getId(),admin.getId(),RequestStatus.RESOLVED);
        assertNotNull(resolved.getResolvedTime()); assertEquals(admin.getId(),resolved.getResolvedBy());
        assertThrows(IllegalArgumentException.class,()->requests.resolveRequest(r.getId(),admin.getId(),RequestStatus.PROCESSING));
        assertThrows(IllegalArgumentException.class,()->invoices.createInvoice(c.getId(),"Bad fee",-1));
        Invoice i=invoices.createInvoice(c.getId(),"November fee",500000);
        assertEquals(1,invoices.getUnpaidInvoicesByStudent("SV001").size());
        Timestamp paid=invoices.payInvoice(i.getId()).getPaidTime();
        assertEquals(paid,invoices.payInvoice(i.getId()).getPaidTime());
        assertTrue(invoices.getUnpaidInvoices().isEmpty());
    }
    @Test void studentCannotReadOtherRecordsOrRunStaffOperations() throws Exception {
        Contract c=contract("SV001");
        Invoice i=invoices.createInvoice(c.getId(),"Fee",500000);
        auth.login("sv2@dorm.test","student123");
        assertThrows(IllegalArgumentException.class,()->contracts.getContractById(c.getId()));
        assertThrows(IllegalArgumentException.class,()->invoices.getInvoiceById(i.getId()));
        assertThrows(IllegalArgumentException.class,()->invoices.payInvoice(i.getId()));
        assertThrows(IllegalArgumentException.class,()->new StudentServiceImpl().getAllStudents());
        assertThrows(IllegalArgumentException.class,()->rooms.addRoomType("Other",4,100));
        assertThrows(IllegalArgumentException.class,()->auth.registerAdmin("hack@dorm.test","password"));
        assertEquals(1,rooms.getAllRooms().size());
    }
    @Test void staffCanManageButCannotCreateAdmins() throws Exception {
        auth.registerStaff("staff@dorm.test","staff123");auth.login("staff@dorm.test","staff123");
        assertNotNull(contract("SV001"));
        assertThrows(IllegalArgumentException.class,()->auth.registerAdmin("other@dorm.test","password"));
    }
    @Test void studentSearchUpdateDeleteAndForeignKeys() throws Exception {
        StudentServiceImpl service=new StudentServiceImpl();
        assertEquals(1,service.searchStudentsByName("nguyen").size());
        Student s=service.getStudentById("SV002").orElseThrow();s.setFullName("Tran Binh Updated");service.updateStudent(s);
        assertEquals("Tran Binh Updated",service.getStudentById("SV002").orElseThrow().getFullName());
        contract("SV001");
        assertThrows(SQLException.class,()->service.deleteStudent("SV001"));
        assertThrows(SQLException.class,()->rooms.deleteRoom(room.getId()));
        assertTrue(service.deleteStudent("SV002"));
        assertTrue(service.getStudentById("SV002").isEmpty());
        assertFalse(auth.isEmailTaken("sv2@dorm.test"));
    }
    @Test void roomTypeChangesKeepOccupancyAndStatus() throws Exception {
        contract("SV001");
        RoomType t=rooms.getRoomTypeById(room.getRoomTypeId()).orElseThrow();t.setCapacity(2);rooms.updateRoomType(t);
        assertEquals(RoomStatus.AVAILABLE,rooms.getRoomById(room.getId()).orElseThrow().getRoomStatus());
        contract("SV002");t.setCapacity(1);
        assertThrows(IllegalArgumentException.class,()->rooms.updateRoomType(t));
        assertEquals(2,rooms.getRoomTypeById(t.getId()).orElseThrow().getCapacity());
    }
    @Test void concurrentAllocationsCannotOverfillOneBed() throws Exception {
        ExecutorService pool=Executors.newFixedThreadPool(2);
        try {
            Callable<Boolean> a=()->{try{contract("SV001");return true;}catch(IllegalArgumentException e){return false;}};
            Callable<Boolean> b=()->{try{contract("SV002");return true;}catch(IllegalArgumentException e){return false;}};
            var results=pool.invokeAll(java.util.List.of(a,b));
            int successes=0;for(Future<Boolean> r:results) if(r.get()) successes++;
            assertEquals(1,successes);assertEquals(1,rooms.getRoomById(room.getId()).orElseThrow().getCurrentOccupancy());
        } finally {pool.shutdownNow();}
    }
    @AfterAll static void disconnect() {Session.logout();DatabaseConnection.getInstance().closeConnection();}
}
