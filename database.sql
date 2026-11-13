-- ============================================================
-- PeopleDesk - database setup script (MySQL 8)
-- Run this ONCE in MySQL Workbench (see README.md, section 11).
-- It is safe to run again: it will not delete existing data.
-- NOTE: No user account is created here. The first administrator
-- is created from the browser at /PeopleDesk/setup, so no
-- password ever appears in this file.
-- ============================================================

CREATE DATABASE IF NOT EXISTS peopledesk
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE peopledesk;

-- ------------------------------------------------------------
-- users: people who can log in to PeopleDesk
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id            INT          NOT NULL AUTO_INCREMENT,
    username      VARCHAR(50)  NOT NULL,
    password_hash VARCHAR(255) NOT NULL,              -- salted PBKDF2 hash, never the real password
    role          VARCHAR(20)  NOT NULL DEFAULT 'ADMIN',
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- employees: the main data managed by the application
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS employees (
    id           INT           NOT NULL AUTO_INCREMENT,
    name         VARCHAR(100)  NOT NULL,
    email        VARCHAR(120)  NOT NULL,
    phone        VARCHAR(20)   NOT NULL,
    department   VARCHAR(60)   NOT NULL,
    job_title    VARCHAR(80)   NOT NULL,
    salary       DECIMAL(10,2) NOT NULL,
    joining_date DATE          NOT NULL,
    created_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_employees_email (email),            -- no two employees with the same email
    KEY idx_employees_name (name),                    -- speeds up searching by name
    KEY idx_employees_department (department),        -- speeds up department count
    CONSTRAINT chk_employees_salary CHECK (salary >= 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- Sample employees (fictional people, example.com emails).
-- INSERT IGNORE skips a row if its email already exists.
-- ------------------------------------------------------------
INSERT IGNORE INTO employees (name, email, phone, department, job_title, salary, joining_date) VALUES
    ('Aarav Sharma',   'aarav.sharma@example.com',   '9876543210',      'Engineering', 'Software Engineer',    65000.00, '2022-06-15'),
    ('Priya Patil',    'priya.patil@example.com',    '9823012345',      'Human Resources', 'HR Executive',     48000.00, '2021-09-01'),
    ('Rohan Deshmukh', 'rohan.deshmukh@example.com', '9765432109',      'Finance',     'Accountant',           52000.00, '2020-03-10'),
    ('Sneha Kulkarni', 'sneha.kulkarni@example.com', '9890123456',      'Engineering', 'QA Engineer',          55000.00, '2023-01-23'),
    ('Vikram Joshi',   'vikram.joshi@example.com',   '9922334455',      'Sales',       'Sales Manager',        72000.00, '2019-11-05'),
    ('Neha Gupta',     'neha.gupta@example.com',     '+91 9811122233',  'Marketing',   'Marketing Specialist', 50000.00, '2022-02-14'),
    ('Karan Mehta',    'karan.mehta@example.com',    '9988776655',      'Engineering', 'Senior Developer',     98000.00, '2018-07-30'),
    ('Anjali Nair',    'anjali.nair@example.com',    '9745612308',      'Operations',  'Operations Analyst',   47000.00, '2024-04-08');
