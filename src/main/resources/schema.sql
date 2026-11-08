-- ============================================================
-- Dorm Management System — PostgreSQL Schema
-- ============================================================

CREATE TABLE IF NOT EXISTS account (
    account_id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'STUDENT', 'STAFF'))
);

CREATE TABLE IF NOT EXISTS student (
    student_id VARCHAR(10) PRIMARY KEY,
    account_id INTEGER UNIQUE REFERENCES account(account_id),
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(10) CHECK (phone ~ '^0[0-9]{9}$'),
    gender VARCHAR(10) NOT NULL CHECK (gender IN ('MALE', 'FEMALE'))
);

CREATE TABLE IF NOT EXISTS room_type (
    room_type_id SERIAL PRIMARY KEY,
    type_name VARCHAR(50) UNIQUE NOT NULL,
    capacity INTEGER NOT NULL CHECK (capacity > 0),
    price BIGINT NOT NULL CHECK (price >= 0)
);

CREATE TABLE IF NOT EXISTS room (
    room_id SERIAL PRIMARY KEY,
    room_type_id INTEGER NOT NULL REFERENCES room_type(room_type_id),
    room_number VARCHAR(20) NOT NULL,
    building VARCHAR(20) NOT NULL,
    current_occupancy INTEGER NOT NULL DEFAULT 0 CHECK (current_occupancy >= 0),
    room_status VARCHAR(20) NOT NULL CHECK (room_status IN ('AVAILABLE', 'FULL', 'MAINTENANCE')),
    UNIQUE (building, room_number)
);

CREATE TABLE IF NOT EXISTS contract (
    contract_id SERIAL PRIMARY KEY,
    student_id VARCHAR(10) NOT NULL REFERENCES student(student_id),
    room_id INTEGER NOT NULL REFERENCES room(room_id),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    contract_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
        CHECK (contract_status IN ('ACTIVE', 'EXPIRED', 'TERMINATED')),
    CHECK (end_date > start_date)
);

CREATE TABLE IF NOT EXISTS request (
    request_id SERIAL PRIMARY KEY,
    student_id VARCHAR(10) NOT NULL REFERENCES student(student_id),
    room_id INTEGER NOT NULL REFERENCES room(room_id),
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    request_status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (request_status IN ('PENDING', 'PROCESSING', 'RESOLVED')),
    requested_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_time TIMESTAMP,
    resolved_by INTEGER REFERENCES account(account_id)
);

CREATE TABLE IF NOT EXISTS invoice (
    invoice_id SERIAL PRIMARY KEY,
    contract_id INTEGER NOT NULL REFERENCES contract(contract_id),
    title VARCHAR(255) NOT NULL,
    fee BIGINT NOT NULL CHECK (fee >= 0),
    invoice_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID' CHECK (invoice_status IN ('UNPAID', 'PAID')),
    paid_time TIMESTAMP
);
