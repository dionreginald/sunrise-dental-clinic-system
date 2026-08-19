-- Sunrise Dental Clinic Appointment System
-- Starter schema based on the fields listed in the assessment brief.
-- Feel free to extend this (e.g. add a 'status' column to appointment
-- for cancellations, or a 'staff' table for real login credentials).

CREATE DATABASE IF NOT EXISTS dental_clinic;
USE dental_clinic;

-- Drop in reverse dependency order (child tables first) so this script
-- can be re-run safely to reset the database to match the current schema.
DROP TABLE IF EXISTS bill;
DROP TABLE IF EXISTS appointment;
DROP TABLE IF EXISTS patient;
DROP TABLE IF EXISTS dentist;
DROP TABLE IF EXISTS treatment_type;

CREATE TABLE IF NOT EXISTS patient (
                                       contact      VARCHAR(20)  PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    address      VARCHAR(255)
    );

CREATE TABLE IF NOT EXISTS dentist (
                                       dentist_id   VARCHAR(20)  PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    specialty    VARCHAR(100)
    );

CREATE TABLE IF NOT EXISTS treatment_type (
                                              treatment_id VARCHAR(20)  PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    price        DECIMAL(10,2) NOT NULL DEFAULT 0.00
    );

CREATE TABLE IF NOT EXISTS appointment (
                                           appt_number  VARCHAR(20)  PRIMARY KEY,
    patient_contact VARCHAR(20) NOT NULL,
    dentist_id   VARCHAR(20)  NOT NULL,
    treatment    VARCHAR(100) NOT NULL,
    appt_date    DATE         NOT NULL,
    appt_time    TIME         NOT NULL,
    status       VARCHAR(20)  DEFAULT 'CONFIRMED',
    FOREIGN KEY (patient_contact) REFERENCES patient(contact),
    FOREIGN KEY (dentist_id) REFERENCES dentist(dentist_id)
    );

CREATE TABLE IF NOT EXISTS bill (
                                    bill_id       VARCHAR(20) PRIMARY KEY,
    appt_number   VARCHAR(20) NOT NULL,
    consult_fee   DECIMAL(10,2) NOT NULL,
    treatment_cost DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    hospital_charge DECIMAL(10,2) NOT NULL,
    tax_percentage  DECIMAL(5,2) NOT NULL,
    total_amount  DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (appt_number) REFERENCES appointment(appt_number)
    );

-- TODO: a real login system needs a staff table, e.g.:
-- CREATE TABLE IF NOT EXISTS staff (
--     username VARCHAR(50) PRIMARY KEY,
--     password_hash VARCHAR(255) NOT NULL
-- );

-- Sample seed data to test against while you build
INSERT INTO dentist (dentist_id, name, specialty) VALUES
                                                      ('D001', 'Dr. Perera', 'General Dentistry'),
														('D002', 'Dr. Fernando', 'Orthodontics'),
														('D003', 'Dr. Silva', 'Endodontics'),
														('D004', 'Dr. Jayasinghe', 'Periodontics'),
														('D005', 'Dr. Wijesinghe', 'Prosthodontics'),
														('D006', 'Dr. Gunawardena', 'Oral Surgery'),
														('D007', 'Dr. Bandara', 'Pediatric Dentistry'),
														('D008', 'Dr. Senanayake', 'General Dentistry'),
														('D009', 'Dr. de Alwis', 'Orthodontics'),
														('D010', 'Dr. Rajapaksha', 'Implant Dentistry'),
														('D011', 'Dr. Samarasinghe', 'Cosmetic Dentistry'),
														('D012', 'Dr. Wickramasinghe', 'Endodontics'),
														('D013', 'Dr. Karunaratne', 'Periodontics'),
														('D014', 'Dr. Herath', 'Prosthodontics'),
														('D015', 'Dr. Abeysekera', 'Oral Surgery'),
														('D016', 'Dr. Ratnayake', 'Pediatric Dentistry'),
														('D017', 'Dr. Ekanayake', 'General Dentistry'),
														('D018', 'Dr. Pathirana', 'Orthodontics'),
														('D019', 'Dr. Amarasinghe', 'Cosmetic Dentistry'),
														('D020', 'Dr. Dissanayake', 'Implant Dentistry');

-- Add/remove rows here to change what's offered in the "Treatment Type"
-- dropdown - no code change needed, the frontend reads this table live.
-- Prices are illustrative (LKR) — adjust to whatever Sunrise Dental
-- actually charges before you present this as "real" data.
INSERT INTO treatment_type (treatment_id, name, price) VALUES
                                                    ('T001', 'Consultation', 1500.00),
                                                    ('T002', 'Scaling & Polishing', 3500.00),
                                                    ('T003', 'Tooth Extraction', 4000.00),
                                                    ('T004', 'Root Canal Treatment', 15000.00),
                                                    ('T005', 'Dental Filling', 5000.00),
                                                    ('T006', 'Braces Adjustment', 3000.00),
                                                    ('T007', 'Teeth Whitening', 12000.00);