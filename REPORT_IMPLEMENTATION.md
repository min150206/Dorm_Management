# REPORT ON BACKEND SYSTEM IMPLEMENTATION: DORM MANAGEMENT SYSTEM

## I. INTRODUCTION

The Dormitory Management System (DMS) is designed to automate and streamline dormitory administration for educational institutions. The system supports three primary user roles: **Administrators**, **Staff**, and **Students**. It handles room inventory allocation, student check-in contracts, billing/invoicing, maintenance request tickets, and credential management.

### User Roles Description

| Role | Responsibilities & Permissions |
| :--- | :--- |
| **ADMIN** | Full administrative privileges: manages room types, room records, system staff accounts, oversees occupancy reports, and resolves student escalations. |
| **STAFF** | Operational privileges: reviews dormitory maintenance requests, processes room inspections, updates request resolution status, and records bill payments. |
| **STUDENT** | Resident access: views personal profile, browses available rooms, signs rental contracts, creates and monitors repair requests, and reviews payment invoices. |

---

## II. CLASS ANALYSIS

### 1. Objects

In the analysis phase, real-world domain entities within the dormitory ecosystem were identified and classified based on their states and behavioral responsibilities.

**Table 1: List of Objects**

| No | Object Name | State (Attributes) | Behaviours (Actions) |
| :--- | :--- | :--- | :--- |
| 1 | **Account** | `accountId`: 101, `email`: "admin@dorm.edu", `password`: "hash...", `role`: ADMIN | `authenticate()`, `changePassword()`, `validateRole()` |
| 2 | **Student** | `studentId`: "STU001", `accountId`: 102, `fullName`: "Nguyen Van A", `phone`: "0912345678", `gender`: MALE | `viewProfile()`, `submitRequest()`, `signContract()` |
| 3 | **RoomType** | `roomTypeId`: 1, `typeName`: "Deluxe 2-Bed", `capacity`: 2, `price`: 2,500,000 VND | `calculateRent()`, `verifyCapacity()` |
| 4 | **Room** | `roomId`: 201, `roomTypeId`: 1, `roomNumber`: "B1-201", `building`: "Block B", `currentOccupancy`: 1, `roomStatus`: AVAILABLE | `checkIn()`, `checkOut()`, `isFull()`, `flagMaintenance()` |
| 5 | **Contract** | `contractId`: 501, `studentId`: "STU001", `roomId`: 201, `startDate`: 2026-09-01, `endDate`: 2027-06-30, `contractStatus`: ACTIVE | `activate()`, `terminate()`, `isExpired()` |
| 6 | **Request** | `requestId`: 801, `studentId`: "STU001", `roomId`: 201, `title`: "Air conditioner leaking", `description`: "Water drips...", `requestStatus`: PENDING, `requestedTime`: 2026-11-06 08:30:00 | `submit()`, `assignResolver()`, `updateStatus()` |
| 7 | **Invoice** | `invoiceId`: 901, `contractId`: 501, `title`: "Monthly Rent Nov 2026", `fee`: 2,500,000, `invoiceStatus`: UNPAID, `paidTime`: null | `issue()`, `recordPayment()`, `markOverdue()` |

*Discussion on Table 1:*  
Table 1 groups real-world dormitory entities into state-behavior pairings. Every entity is bound to specific lifecycle events (e.g., contracts transition from `ACTIVE` to `EXPIRED` or `TERMINATED`; requests move from `PENDING` to `PROCESSING` to `RESOLVED`). These state-behavior groupings form the baseline for class synthesis in the design phase.

---

### 2. Classes and Inheritance Analysis

To avoid boilerplate code and decouple persistence logic from business logic, the system utilizes a **Layered Architecture (3-Tier Model)** combined with the **Data Access Object (DAO)** and **Template Method** design patterns:

1. **Domain Model Layer (`com.dorm.model`)**:  
   All entity classes share common primary identity characteristics. Therefore, a generic abstract class `BaseEntity<ID>` is established. Concrete classes (`Account`, `RoomType`, `Room`, `Contract`, `Request`, `Invoice`) extend `BaseEntity<Integer>`, while `Student` extends `BaseEntity<String>` because student identification uses institutional alphanumeric codes (`VARCHAR(10)`).

2. **Persistence / DAO Layer (`com.dorm.dao`, `com.dorm.dao.impl`)**:  
   CRUD behaviors (`save`, `update`, `deleteById`, `findById`, `findAll`, `count`, `existsById`) are abstracted into a top-level interface `GenericDAO<T, ID>`. The generic JDBC operations are implemented in the abstract class `AbstractDAO<T, ID>`, which concrete DAOs (`AccountDAOImpl`, `StudentDAOImpl`, etc.) inherit from.

```
BaseEntity<ID> (Abstract)
   ├── Account        (extends BaseEntity<Integer>)
   ├── Student        (extends BaseEntity<String>)
   ├── RoomType       (extends BaseEntity<Integer>)
   ├── Room           (extends BaseEntity<Integer>)
   ├── Contract       (extends BaseEntity<Integer>)
   ├── Request        (extends BaseEntity<Integer>)
   └── Invoice        (extends BaseEntity<Integer>)

GenericDAO<T, ID> (Interface)
   ├── AbstractDAO<T, ID> (Abstract class implementing GenericDAO)
   │     ├── AccountDAOImpl  (extends AbstractDAO<Account, Integer> implements AccountDAO)
   │     ├── StudentDAOImpl  (extends AbstractDAO<Student, String> implements StudentDAO)
   │     ├── RoomTypeDAOImpl (extends AbstractDAO<RoomType, Integer> implements RoomTypeDAO)
   │     ├── RoomDAOImpl     (extends AbstractDAO<Room, Integer> implements RoomDAO)
   │     ├── ContractDAOImpl (extends AbstractDAO<Contract, Integer> implements ContractDAO)
   │     ├── RequestDAOImpl  (extends AbstractDAO<Request, Integer> implements RequestDAO)
   │     └── InvoiceDAOImpl  (extends AbstractDAO<Invoice, Integer> implements InvoiceDAO)
```

---

## III. CLASS DESIGN

### 1. Class Diagram

```
+-------------------------------------------------------------------+
|               <<abstract>> BaseEntity<ID>                         |
+-------------------------------------------------------------------+
| # id: ID                                                          |
+-------------------------------------------------------------------+
| + getId(): ID                                                     |
| + setId(id: ID): void                                             |
| + isPersisted(): boolean                                          |
| + getTableName()*: String                                         |
| + getIdColumnName()*: String                                      |
| + equals(o: Object): boolean                                      |
| + hashCode(): int                                                 |
+-------------------------------------------------------------------+
                                  ^
       +--------------------------+--------------------------+
       |                          |                          |
+---------------+          +---------------+          +---------------+
|    Account    |          |    Student    |          |     Room      |
+---------------+          +---------------+          +---------------+
| - email       |          | - accountId   |          | - roomTypeId  |
| - password    |          | - fullName    |          | - roomNumber  |
| - role: Role  |          | - phone       |          | - building    |
+---------------+          | - gender      |          | - occupancy   |
                           +---------------+          | - roomStatus  |
                                                      +---------------+
```

*Discussion on Class Diagram:*  
The diagram reflects clear separation of concerns. `BaseEntity` enforces the uniform identity property (`id`) and identity equality (`equals`/`hashCode`), while concrete model classes encapsulate domain-specific fields with strongly typed Java `enum` values (`Role`, `Gender`, `RoomStatus`, `RequestStatus`, `ContractStatus`, `InvoiceStatus`).

---

### 2. Details of Classes

**Table 2: Details of Classes**

| No | Class | Instance Variables | Methods | Description |
| :--- | :--- | :--- | :--- | :--- |
| 1 | `BaseEntity<ID>` | `protected ID id;` | `public ID getId()`, `public void setId(ID id)`, `public abstract String getTableName()`, `public abstract String getIdColumnName()` | Abstract root class for entities. Enforces entity identity and table metadata mapping. |
| 2 | `Account` | `private String email;`<br>`private String password;`<br>`private Role role;` | `public String getEmail()`, `public void setEmail(String email)`, `public Role getRole()`, `public void setRole(Role role)` | Represents user credentials and RBAC roles (`ADMIN`, `STUDENT`, `STAFF`). |
| 3 | `Student` | `private int accountId;`<br>`private String fullName;`<br>`private String phone;`<br>`private Gender gender;` | `public String getStudentId()`, `public void setStudentId(String id)`, `public String getFullName()`, `public void setFullName(...)` | Stores personal student records linked 1-to-1 with an `Account`. |
| 4 | `RoomType` | `private String typeName;`<br>`private int capacity;`<br>`private long price;` | `public String getTypeName()`, `public int getCapacity()`, `public long getPrice()` | Defines room pricing tiers and maximum resident capacity. |
| 5 | `Room` | `private int roomTypeId;`<br>`private String roomNumber;`<br>`private String building;`<br>`private int currentOccupancy;`<br>`private RoomStatus roomStatus;` | `public int getCurrentOccupancy()`, `public void setCurrentOccupancy(int)`, `public RoomStatus getRoomStatus()`, `public void setRoomStatus(...)` | Models physical dormitory rooms and tracks real-time occupancy. |
| 6 | `Contract` | `private String studentId;`<br>`private int roomId;`<br>`private Date startDate;`<br>`private Date endDate;`<br>`private ContractStatus contractStatus;` | `public Date getStartDate()`, `public Date getEndDate()`, `public ContractStatus getContractStatus()` | Governs room occupancy agreements between students and the dorm. |
| 7 | `Request` | `private String studentId;`<br>`private int roomId;`<br>`private String title;`<br>`private String description;`<br>`private RequestStatus requestStatus;`<br>`private Timestamp requestedTime;`<br>`private Timestamp resolvedTime;`<br>`private Integer resolvedBy;` | `public RequestStatus getRequestStatus()`, `public void setRequestStatus(...)`, `public void setResolvedBy(Integer)` | Handles ticketing for repair/maintenance requests. |
| 8 | `Invoice` | `private int contractId;`<br>`private String title;`<br>`private long fee;`<br>`private InvoiceStatus invoiceStatus;`<br>`private Timestamp paidTime;` | `public long getFee()`, `public InvoiceStatus getInvoiceStatus()`, `public void setPaidTime(Timestamp)` | Manages billing statements associated with signed rental contracts. |
| 9 | `DatabaseConnection` | `private static volatile DatabaseConnection instance;`<br>`private Connection connection;` | `public static DatabaseConnection getInstance()`, `public Connection getConnection()`, `public void closeConnection()` | Thread-safe singleton providing pooled JDBC connections to PostgreSQL. |

*Discussion on Table 2:*  
All entity attributes are marked `private` to safeguard state consistency (Encapsulation). Modifiers for `id` in `BaseEntity` and utility methods in `AbstractDAO` are marked `protected` to permit subclass inheritance while barring arbitrary external manipulation.

---

### 3. Abstract Classes

**Table 3: Details of Abstract Classes**

| No | Abstract Class | Abstract Methods | Concrete Methods | Description |
| :--- | :--- | :--- | :--- | :--- |
| 1 | `BaseEntity<ID>`<br>*(Source: `src/main/java/com/dorm/model/BaseEntity.java`)* | `getTableName()`<br>`getIdColumnName()` | `getId()`, `setId(ID id)`, `isPersisted()`, `equals(Object o)`, `hashCode()`, `toString()` | Serves as the base entity for domain objects, guaranteeing standard identity contracts. |
| 2 | `AbstractDAO<T, ID>`<br>*(Source: `src/main/java/com/dorm/dao/impl/AbstractDAO.java`)* | `mapRow(ResultSet rs)`<br>`getTableName()`<br>`getIdColumnName()`<br>`getInsertSQL()`<br>`getUpdateSQL()`<br>`setInsertParameters(...)`<br>`setUpdateParameters(...)` | `save(T entity)`<br>`update(T entity)`<br>`deleteById(ID id)`<br>`findById(ID id)`<br>`findAll()`<br>`count()`<br>`existsById(ID id)`<br>`queryList(...)`<br>`querySingle(...)`<br>`getConnection()` | Implements the Template Method pattern to handle JDBC boilerplate (statements, transactions, result sets) while delegating table-specific mappings to subclasses. |

*Discussion on Table 3:*  
By isolating JDBC lifecycle management in `AbstractDAO<T, ID>`, redundant database access code is eliminated across the seven database tables. Subclasses only supply query definitions and parameter bindings.

---

### 4. OOP Techniques Implemented

#### 4.1. Method Overloading
Method overloading allows multiple methods to share the same name with different parameter signatures.

*Representative Code Snippets:*
1. **Constructors in Domain Entities** (`src/main/java/com/dorm/model/Account.java`):
```java
// Default no-argument constructor for reflective frameworks
public Account() {}

// Parameterized constructor for complete state initialization
public Account(Integer accountId, String email, String password, Role role) {
    super(accountId);
    this.email    = email;
    this.password = password;
    this.role     = role;
}
```

2. **Database Query Helpers in AbstractDAO** (`src/main/java/com/dorm/dao/impl/AbstractDAO.java`):
`queryList` and `querySingle` overload query execution by taking varargs parameters:
```java
protected List<T> queryList(String sql, Object... params) throws SQLException;
protected Optional<T> querySingle(String sql, Object... params) throws SQLException;
```

#### 4.2. Method Overriding
Method overriding allows a subclass to provide a specific implementation of a method defined in its superclass or interface.

*Representative Code Snippets:*
1. **Entity Equality Overriding** (`src/main/java/com/dorm/model/BaseEntity.java`):
```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BaseEntity<?> that = (BaseEntity<?>) o;
    return Objects.equals(id, that.id);
}

@Override
public int hashCode() {
    return Objects.hash(id);
}
```

2. **Template Method Overriding in Concrete DAOs** (`src/main/java/com/dorm/dao/impl/StudentDAOImpl.java`):
```java
@Override
protected Student mapRow(ResultSet rs) throws SQLException {
    String genderStr = rs.getString("gender");
    return new Student(
            rs.getString("student_id"),
            rs.getInt("account_id"),
            rs.getString("full_name"),
            rs.getString("phone"),
            genderStr != null ? Gender.fromString(genderStr) : null
    );
}

// Special overriding for Student because VARCHAR PK is manually assigned, not SERIAL
@Override
public Student save(Student entity) throws SQLException {
    String sql = getInsertSQL();
    try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
        setInsertParameters(ps, entity);
        ps.executeUpdate();
    }
    return entity;
}
```

#### 4.3. Inheritance and 3-Tier Layered Model
The project strictly implements a **3-Tier Architecture**:
- **Presentation Tier**: JavaFX views and controllers (`com.dorm.PrimaryController`, `com.ui.login`).
- **Application / Service Tier**: Business orchestration, input validation, and transaction boundaries (`com.dorm.service`).
- **Data Tier**: DAO interfaces and abstract implementations communicating with PostgreSQL (`com.dorm.dao`, `com.dorm.dao.impl`, `com.dorm.db`).

---

### 5. Package Design

The backend source code is organized into a modular package structure:

```
src/main/java/com/dorm/
├── db/                        # Database connectivity
│   └── DatabaseConnection.java
├── model/                     # Domain entities & business enums
│   ├── BaseEntity.java
│   ├── Account.java
│   ├── Student.java
│   ├── RoomType.java
│   ├── Room.java
│   ├── Contract.java
│   ├── Request.java
│   ├── Invoice.java
│   └── enums/
│       ├── Role.java
│       ├── Gender.java
│       ├── RoomStatus.java
│       ├── RequestStatus.java
│       ├── ContractStatus.java
│       └── InvoiceStatus.java
├── dao/                       # Data Access Object interfaces
│   ├── GenericDAO.java
│   ├── AccountDAO.java
│   ├── StudentDAO.java
│   ├── RoomTypeDAO.java
│   ├── RoomDAO.java
│   ├── ContractDAO.java
│   ├── RequestDAO.java
│   ├── InvoiceDAO.java
│   └── impl/                  # Concrete DAO implementations
│       ├── AbstractDAO.java
│       ├── AccountDAOImpl.java
│       ├── StudentDAOImpl.java
│       ├── RoomTypeDAOImpl.java
│       ├── RoomDAOImpl.java
│       ├── ContractDAOImpl.java
│       ├── RequestDAOImpl.java
│       └── InvoiceDAOImpl.java
└── service/                   # Business logic interfaces
    ├── AuthService.java
    ├── StudentService.java
    ├── RoomService.java
    ├── ContractService.java
    ├── RequestService.java
    └── InvoiceService.java
```

---

### 6. Interface Design

Interfaces decouple specifications from runtime implementations:

1. **`GenericDAO<T, ID>`**: Common CRUD operations (`save`, `update`, `deleteById`, `findById`, `findAll`, `count`, `existsById`).
2. **Entity-Specific DAOs**:
   - `AccountDAO`: `findByEmail`, `findByRole`, `findByEmailAndPassword`, `emailExists`.
   - `StudentDAO`: `findByAccountId`, `searchByName`, `findByGender`, `studentIdExists`.
   - `RoomDAO`: `findByBuilding`, `findByStatus`, `findAvailableRooms`, `updateOccupancy`.
   - `ContractDAO`: `findActiveContracts`, `findByStudentId`, `findActiveByStudentId`.
   - `RequestDAO`: `findPendingRequests`, `findByStudentId`, `findByStatus`.
   - `InvoiceDAO`: `findUnpaidInvoices`, `findByContractId`, `findUnpaidByStudentId`.
3. **Service Interfaces**:
   - `AuthService`: Coordinates student/admin registration and password modifications.
   - `ContractService`: Manages booking workflows, automatically updating room occupancy and setting room status to `FULL` when capacity is met.
   - `RoomService`, `RequestService`, `InvoiceService`: Encapsulates validation and state changes.

---

### 7. Access Control

**Table X1: Data Access Control Table**

| No | Data (Field) | Class | Modifier | Description |
| :--- | :--- | :--- | :--- | :--- |
| 1 | `id` | `BaseEntity` | `protected` | Accessible only to inheriting entity subclasses; direct modification by unrelated classes is blocked. |
| 2 | `password` | `Account` | `private` | Protects credentials from unauthorized exposure; accessed only through verified service methods. |
| 3 | `phone`, `gender` | `Student` | `private` | Personal identifiable information (PII) is protected; read/write is guarded by getters and setters. |
| 4 | `currentOccupancy` | `Room` | `private` | Prevents race conditions or direct manipulation of room counts outside the contract service. |
| 5 | `connection` | `DatabaseConnection` | `private` | Encapsulates the raw JDBC resource to enforce singleton access through `getConnection()`. |

**Table X2: Method Access Control Table**

| No | Method | Class | Modifier | Description |
| :--- | :--- | :--- | :--- | :--- |
| 1 | `mapRow(ResultSet rs)` | `AbstractDAO` | `protected` | Internal template method; exposed only to subclass DAOs during row mapping. |
| 2 | `getConnection()` | `AbstractDAO` | `protected` | Allows DAOs in the same inheritance tree to obtain a connection without leaking it publicly. |
| 3 | `DatabaseConnection()` | `DatabaseConnection` | `private` | Private constructor prevents direct instantiation, enforcing the Singleton pattern. |
| 4 | `save(T entity)` | `GenericDAO` / `AbstractDAO` | `public` | Public API contract permitting upper layers (Service/UI) to persist data. |
| 5 | `login(...)` | `AuthService` | `public` | Entry point for authentication operations called by the Presentation/Login Controller. |

*Discussion on Tables X1 and X2:*  
The access control configuration enforces data security and loose coupling. Sensitive variables are strictly `private`. Subclass hook methods in the Template Method pattern are restricted to `protected`, and service entry points are declared `public`.

---

### 8. Encapsulation vs Inheritance vs Polymorphism

#### 8.1. Encapsulation
Encapsulation wraps state variables inside an object and restricts outside access through controlled methods. In `Room.java` (`src/main/java/com/dorm/model/Room.java`):
```java
private int currentOccupancy;
private RoomStatus roomStatus;

public int getCurrentOccupancy() {
    return currentOccupancy;
}

public void setCurrentOccupancy(int currentOccupancy) {
    if (currentOccupancy < 0) {
        throw new IllegalArgumentException("Occupancy cannot be negative.");
    }
    this.currentOccupancy = currentOccupancy;
}
```
*Why this demonstrates Encapsulation:*  
The fields cannot be altered directly from outside the class. The setter can validate business constraints (e.g., occupancy cannot be negative) before changing state.

#### 8.2. Inheritance
Inheritance allows child classes to acquire fields and behaviors of a parent class. In `Contract.java` (`src/main/java/com/dorm/model/Contract.java`):
```java
public class Contract extends BaseEntity<Integer> {
    private String studentId;
    private int roomId;
    // ...
    @Override
    public String getTableName() { return "contract"; }
    @Override
    public String getIdColumnName() { return "contract_id"; }
}
```
*Why this demonstrates Inheritance:*  
`Contract` inherits the `id` field, getter, setter, `isPersisted()`, and `equals`/`hashCode` implementations from `BaseEntity<Integer>`, reusing core logic without code duplication.

#### 8.3. Polymorphism
Polymorphism allows objects to be treated as instances of their parent type, with method calls dynamically dispatched at runtime. In `AbstractDAO.java` (`src/main/java/com/dorm/dao/impl/AbstractDAO.java`):
```java
public Optional<T> findById(ID id) throws SQLException {
    String sql = "SELECT * FROM " + getTableName() + " WHERE " + getIdColumnName() + " = ?";
    try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
        ps.setObject(1, id);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return Optional.of(mapRow(rs)); // Dynamic dispatch!
            }
        }
    }
    return Optional.empty();
}
```
*Why this demonstrates Polymorphism:*  
When `accountDAO.findById(1)` is executed, `AbstractDAO.findById` calls `mapRow(rs)`. The JVM dynamically resolves this call to `AccountDAOImpl.mapRow(rs)` at runtime, returning an `Account` instance. When invoked on `RoomDAOImpl`, it resolves to `RoomDAOImpl.mapRow(rs)` and produces a `Room` instance.

---

## IV. EXPERIMENT & DATABASE SPECIFICATION

### 1. Environment and Tools
- **Operating System**: Windows 11 64-bit
- **Programming Language**: Java 11 / Java 21 (modular JavaFX compatible)
- **Build & Dependency Management**: Apache Maven 3.8+
- **RDBMS**: PostgreSQL 16
- **Database Driver**: PostgreSQL JDBC Driver `42.7.4`
- **User Interface Framework**: OpenJFX (JavaFX Controls & FXML `21.0.9`)

### 2. Database Schema (PostgreSQL DDL)
The schema implements 7 relational tables linked via foreign key constraints, check constraints, and unique indexes (`src/main/resources/schema.sql`).

**Table 4: Database Schema Summary**

| Table Name | Primary Key | Foreign Keys | Key Constraints & Checks |
| :--- | :--- | :--- | :--- |
| `account` | `account_id` (SERIAL) | None | `email` UNIQUE, `CHECK (role IN ('ADMIN', 'STUDENT', 'STAFF'))` |
| `student` | `student_id` (VARCHAR) | `account_id` &rarr; `account` | `account_id` UNIQUE, `phone ~ '^0[0-9]{9}$'`, `CHECK (gender IN ('MALE', 'FEMALE'))` |
| `room_type` | `room_type_id` (SERIAL) | None | `type_name` UNIQUE, `CHECK (capacity > 0)`, `CHECK (price >= 0)` |
| `room` | `room_id` (SERIAL) | `room_type_id` &rarr; `room_type` | `UNIQUE(building, room_number)`, `CHECK (room_status IN ('AVAILABLE', 'FULL', 'MAINTENANCE'))` |
| `contract` | `contract_id` (SERIAL) | `student_id` &rarr; `student`, `room_id` &rarr; `room` | `CHECK (end_date > start_date)`, `CHECK (contract_status IN ('ACTIVE', 'EXPIRED', 'TERMINATED'))` |
| `request` | `request_id` (SERIAL) | `student_id` &rarr; `student`, `room_id` &rarr; `room`, `resolved_by` &rarr; `account` | `CHECK (request_status IN ('PENDING', 'PROCESSING', 'RESOLVED'))` |
| `invoice` | `invoice_id` (SERIAL) | `contract_id` &rarr; `contract` | `CHECK (fee >= 0)`, `CHECK (invoice_status IN ('UNPAID', 'PAID'))` |

*Discussion on Table 4:*  
The schema incorporates integrity constraints directly at the database engine level (regex validation on phone numbers, relational cascades, date sequencing, and enumeration checks). This matches the Java enum definitions in the model layer.

---

## V. APPENDIX A: CLASS DESCRIPTION & FILE REFERENCES

| Class / Interface Name | Source File Path | Key Responsibilities |
| :--- | :--- | :--- |
| `BaseEntity<ID>` | `src/main/java/com/dorm/model/BaseEntity.java` | Generic abstract entity root defining table mapping contracts and ID equality. |
| `Account` | `src/main/java/com/dorm/model/Account.java` | Credentials and authorization role definition (`ADMIN`, `STUDENT`, `STAFF`). |
| `Student` | `src/main/java/com/dorm/model/Student.java` | Student profile records with phone validation and gender attributes. |
| `RoomType` | `src/main/java/com/dorm/model/RoomType.java` | Defines room classification, pricing, and maximum allowed occupancy. |
| `Room` | `src/main/java/com/dorm/model/Room.java` | Represents individual rooms with real-time occupancy counts and operational statuses. |
| `Contract` | `src/main/java/com/dorm/model/Contract.java` | Legal occupancy contracts linking students with assigned rooms and lease terms. |
| `Request` | `src/main/java/com/dorm/model/Request.java` | Ticket management for repair and service requests submitted by residents. |
| `Invoice` | `src/main/java/com/dorm/model/Invoice.java` | Billing statements with payment tracking and audit timestamps. |
| `GenericDAO<T, ID>` | `src/main/java/com/dorm/dao/GenericDAO.java` | Generic interface defining the standard CRUD contract. |
| `AbstractDAO<T, ID>`| `src/main/java/com/dorm/dao/impl/AbstractDAO.java` | Abstract template class encapsulating all JDBC CRUD execution logic. |
| `DatabaseConnection`| `src/main/java/com/dorm/db/DatabaseConnection.java` | Thread-safe Singleton connection provider with auto-reconnect logic. |
| `AuthService` | `src/main/java/com/dorm/service/AuthService.java` | High-level authentication and student registration workflows. |
| `ContractService` | `src/main/java/com/dorm/service/ContractService.java` | Business workflows governing contract creation, check-ins, and occupancy updates. |
