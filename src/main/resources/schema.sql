-- ============================================================
-- Dorm Management System — PostgreSQL Schema
-- ============================================================

-- 1. Account
CREATE TABLE IF NOT EXISTS account (
    account_id  SERIAL       PRIMARY KEY,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(50)  NOT NULL
);

-- 2. Student  (1-to-1 with account)
CREATE TABLE IF NOT EXISTS student (
    student_id  VARCHAR(50)  PRIMARY KEY,
    account_id  INTEGER      NOT NULL UNIQUE REFERENCES account(account_id),
    full_name   VARCHAR(255) NOT NULL,
    phone       VARCHAR(20),
    gender      VARCHAR(20)
);

-- 3. Room Type
CREATE TABLE IF NOT EXISTS room_type (
    room_type_id SERIAL       PRIMARY KEY,
    type_name    VARCHAR(100) NOT NULL UNIQUE,
    capacity     INTEGER      NOT NULL,
    price        BIGINT       NOT NULL
);

-- 4. Room  (N-to-1 with room_type)
CREATE TABLE IF NOT EXISTS room (
    room_id           SERIAL       PRIMARY KEY,
    room_type_id      INTEGER      NOT NULL REFERENCES room_type(room_type_id),
    room_number       VARCHAR(50)  NOT NULL,
    building          VARCHAR(100) NOT NULL,
    current_occupancy INTEGER      NOT NULL DEFAULT 0,
    room_status       VARCHAR(50)  NOT NULL DEFAULT 'AVAILABLE'
);

-- 5. Request  (N-to-1 with student, room, account)
CREATE TABLE IF NOT EXISTS request (
    request_id     SERIAL       PRIMARY KEY,
    student_id     VARCHAR(50)  NOT NULL REFERENCES student(student_id),
    room_id        INTEGER      NOT NULL REFERENCES room(room_id),
    title          VARCHAR(255) NOT NULL,
    description    TEXT,
    request_status VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    requested_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_time  TIMESTAMP,
    resolved_by    INTEGER      REFERENCES account(account_id)
);

-- 6. Contract  (N-to-1 with student, room)
CREATE TABLE IF NOT EXISTS contract (
    contract_id     SERIAL       PRIMARY KEY,
    student_id      VARCHAR(50)  NOT NULL REFERENCES student(student_id),
    room_id         INTEGER      NOT NULL REFERENCES room(room_id),
    start_date      DATE         NOT NULL,
    end_date        DATE         NOT NULL,
    contract_status VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE'
);

-- 7. Invoice  (N-to-1 with contract)
CREATE TABLE IF NOT EXISTS invoice (
    invoice_id     SERIAL       PRIMARY KEY,
    contract_id    INTEGER      NOT NULL REFERENCES contract(contract_id),
    title          VARCHAR(255) NOT NULL,
    fee            BIGINT       NOT NULL,
    invoice_status VARCHAR(50)  NOT NULL DEFAULT 'UNPAID',
    paid_time      TIMESTAMP
);
