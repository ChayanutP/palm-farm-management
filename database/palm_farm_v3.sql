-- =========================================================
-- Palm Farm Management System — Database Schema & Data
-- =========================================================
-- ไฟล์นี้สร้าง database, ตาราง และข้อมูลครบในตัวเอง
-- กด Import ได้เลยโดยไม่ต้องเลือก database ก่อน
--
-- Source      : palm_farm.sql (phpMyAdmin dump, 2026-11-06)
-- Target      : MySQL 5.7+ / MariaDB 10.4+
-- Character set: utf8mb4 / utf8mb4_unicode_ci
-- =========================================================

SET NAMES utf8mb4;
SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";

CREATE DATABASE IF NOT EXISTS palm_farm
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE palm_farm;

-- ─────────────────────────────────────────
-- ล้างตารางเดิม (เรียงย้อนตาม foreign key)
-- เพื่อให้ไฟล์นี้ import ซ้ำได้โดยไม่ error
-- ลบ 9 บรรทัดนี้ออกได้ ถ้าไม่ต้องการให้ข้อมูลเดิมหาย
-- ─────────────────────────────────────────
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS bills;
DROP TABLE IF EXISTS reservation_line;
DROP TABLE IF EXISTS task;
DROP TABLE IF EXISTS equipment;
DROP TABLE IF EXISTS vehicle;
DROP TABLE IF EXISTS resource;
DROP TABLE IF EXISTS palm_plantation;
DROP TABLE IF EXISTS employee;
SET FOREIGN_KEY_CHECKS = 1;


-- ─────────────────────────────────────────
-- 1. EMPLOYEE — พนักงาน
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS employee (
    national_id   CHAR(13)     NOT NULL PRIMARY KEY,   -- เลขบัตรประชาชน 13 หลัก
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password      VARCHAR(100) NOT NULL,
    first_name    VARCHAR(60)  NOT NULL,
    last_name     VARCHAR(60)  NOT NULL,
    department    ENUM('RESOURCE','PALM_CARE','ACCOUNTING','HARVEST','CUTTING') NOT NULL,
    position      VARCHAR(80),
    phone         VARCHAR(20),
    created_at    TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO employee (national_id, username, password, first_name, last_name, department, position, phone, created_at) VALUES
('1100100000001', 'resource01', 'resource_123', 'สมชาย',   'ใจดี',       'RESOURCE',   'ผู้จัดสรรทรัพยากร',  '081-111-1111', '2026-11-05 05:52:42'),
('1100100000002', 'palmcare01', 'palmcare_123', 'สมหญิง',  'รักต้นไม้',   'PALM_CARE',  'พนักงานดูแลปาล์ม',   '082-222-2222', '2026-11-05 05:52:42'),
('1100100000004', 'account01',  'account_123',  'มาลี',    'บัญชีเก่ง',   'ACCOUNTING', 'นักบัญชี',           '084-444-4444', '2026-11-05 05:52:42'),
('1100100000005', 'harvest01',  'harvest_123',  'ประสิทธิ์', 'เก็บเกี่ยว',  'HARVEST',    'พนักงานเก็บเกี่ยว',  '085-555-5555', '2026-11-05 05:52:42'),
('1100100000006', 'cutting01',  'cutting_123',  'วรวุฒิ',   'ตัดดี',      'CUTTING',    'พนักงานตัด/ปลูก',    '086-777-7777', '2026-11-05 05:52:42'),
('1100100000007', 'harvest02',  'harvest_123',  'สุดา',     'ขนส่งดี',    'HARVEST',    'คนขับรถขนส่ง',       '085-666-6666', '2026-11-05 05:52:42');


-- ─────────────────────────────────────────
-- 2. PALM_PLANTATION — แปลงปาล์ม
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS palm_plantation (
    plantation_id INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    area_rai      DECIMAL(8,2),
    location      VARCHAR(200)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO palm_plantation (plantation_id, name, area_rai, location) VALUES
(1, 'แปลงปาล์ม A', 50.00, 'หมู่ 1 ต.บ้านสวน อ.เมือง จ.กระบี่'),
(2, 'แปลงปาล์ม B', 80.00, 'หมู่ 3 ต.คลองประสงค์ อ.เมือง จ.กระบี่'),
(3, 'แปลงปาล์ม C', 30.00, 'หมู่ 5 ต.อ่าวนาง อ.เมือง จ.กระบี่'),
(4, 'แปลงปาล์ม D', 40.00, 'หมู่ 7 ต.โตนด อ.เมือง จ.กระบี่'),
(5, 'แปลงปาล์ม E', 45.00, 'หมู่ 9 ต.สำลี อ.เมือง จ.กระบี่');


-- ─────────────────────────────────────────
-- 3. RESOURCE (Superclass) — ทรัพยากร
-- Attributes ร่วม: resourceId, ชนิด, สถานะ
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS resource (
    resource_id   INT AUTO_INCREMENT PRIMARY KEY,
    resource_type ENUM('VEHICLE','EQUIPMENT') NOT NULL,
    status        ENUM('available','reserved','in_use','maintenance') DEFAULT 'available'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO resource (resource_id, resource_type, status) VALUES
(1,  'VEHICLE',   'reserved'),
(2,  'VEHICLE',   'available'),
(3,  'VEHICLE',   'available'),
(4,  'VEHICLE',   'available'),
(5,  'VEHICLE',   'available'),
(6,  'VEHICLE',   'reserved'),
(7,  'VEHICLE',   'available'),
(8,  'VEHICLE',   'available'),
(9,  'EQUIPMENT', 'reserved'),
(10, 'EQUIPMENT', 'available'),
(11, 'EQUIPMENT', 'available'),
(12, 'EQUIPMENT', 'available'),
(13, 'EQUIPMENT', 'reserved'),
(14, 'EQUIPMENT', 'available'),
(15, 'EQUIPMENT', 'available'),
(16, 'EQUIPMENT', 'available');


-- ─────────────────────────────────────────
-- 3.1 VEHICLE (Subclass ของ Resource)
-- Attributes เฉพาะ: เลขทะเบียน, ขนาดรถยนต์, จำนวนที่นั่ง
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS vehicle (
    resource_id   INT PRIMARY KEY,
    license_plate VARCHAR(50) NOT NULL,
    vehicle_size  VARCHAR(100),
    seat_count    INT DEFAULT 2,
    FOREIGN KEY (resource_id) REFERENCES resource(resource_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO vehicle (resource_id, license_plate, vehicle_size, seat_count) VALUES
(1, 'กข-1234', 'รถบรรทุก 6 ล้อ บรรทุกได้ 10 ตัน',  3),
(2, 'ขก-5566', 'รถบรรทุก 6 ล้อ บรรทุกได้ 10 ตัน',  3),
(3, 'งง-8899', 'รถบรรทุก 10 ล้อ บรรทุกได้ 20 ตัน', 2),
(4, 'ตต-001',  'รถแทรกเตอร์ กำลัง 75 แรงม้า',      1),
(5, 'ตต-002',  'รถแทรกเตอร์ กำลัง 90 แรงม้า',      1),
(6, 'กข-5678', 'รถกระบะ 4 ประตู',                  5),
(7, 'กข-9012', 'รถกระบะ 2 ประตู ขนของ',            2),
(8, 'ลล-111',  'รถตัก (Loader)',                   1);


-- ─────────────────────────────────────────
-- 3.2 EQUIPMENT (Subclass ของ Resource)
-- Attributes เฉพาะ: รหัสอุปกรณ์, ชื่ออุปกรณ์
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS equipment (
    resource_id    INT PRIMARY KEY,
    equip_id       VARCHAR(50),
    equipment_name VARCHAR(100) NOT NULL,
    FOREIGN KEY (resource_id) REFERENCES resource(resource_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO equipment (resource_id, equip_id, equipment_name) VALUES
(9,  'EQ-001', 'เครื่องตัดปาล์ม รุ่น X200 เลขที่ 01'),
(10, 'EQ-002', 'เครื่องตัดปาล์ม รุ่น X200 เลขที่ 02'),
(11, 'EQ-003', 'มีดตัดทะลายปาล์ม ชุด 1'),
(12, 'EQ-004', 'มีดตัดทะลายปาล์ม ชุด 2'),
(13, 'EQ-005', 'สายยางรดน้ำ ชุด A (50 เมตร)'),
(14, 'EQ-006', 'ปั๊มน้ำไฟฟ้า เลขที่ 01 (3 แรงม้า)'),
(15, 'EQ-007', 'เครื่องพ่นยา ชุด 1 (สะพายหลัง 20 ลิตร)'),
(16, 'EQ-008', 'เลื่อยยนต์ เลขที่ 01');


-- ─────────────────────────────────────────
-- 4. TASK — งานที่สั่ง / มอบหมาย
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS task (
    task_id            INT AUTO_INCREMENT PRIMARY KEY,
    description        VARCHAR(255),
    department         ENUM('RESOURCE','PALM_CARE','ACCOUNTING','HARVEST','CUTTING') NOT NULL,
    is_complete        TINYINT(1) NOT NULL DEFAULT 0,
    is_allocated       TINYINT(1) NOT NULL DEFAULT 0,
    is_ordered         TINYINT(1) NOT NULL DEFAULT 0,
    requisition_status TINYINT(1) NOT NULL DEFAULT 0,
    work_ordered_date  DATE,
    deadline           DATE,
    plantation_id      INT      NOT NULL,
    created_by         CHAR(13) NOT NULL,
    assigned_to        CHAR(13),
    FOREIGN KEY (plantation_id) REFERENCES palm_plantation(plantation_id),
    FOREIGN KEY (created_by)    REFERENCES employee(national_id),
    FOREIGN KEY (assigned_to)   REFERENCES employee(national_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO task (task_id, description, department, is_complete, is_allocated, is_ordered, requisition_status, work_ordered_date, deadline, plantation_id, created_by, assigned_to) VALUES
(1,  'เก็บเกี่ยวปาล์มรอบ ก.ค.',        'HARVEST',   1, 1, 1, 1, '2026-07-01', '2026-07-15', 1, '1100100000001', '1100100000005'),
(2,  'เก็บเกี่ยวปาล์มรอบ ส.ค.',        'HARVEST',   1, 1, 1, 1, '2026-08-01', '2026-08-15', 1, '1100100000001', '1100100000005'),
(3,  'เก็บเกี่ยวปาล์มรอบ ก.ย.',        'HARVEST',   1, 1, 1, 1, '2026-09-01', '2026-09-15', 1, '1100100000001', '1100100000005'),
(4,  'รดน้ำและใส่ปุ๋ยปาล์มแปลง B',    'PALM_CARE', 1, 1, 1, 1, '2026-08-03', '2026-08-10', 2, '1100100000001', '1100100000002'),
(5,  'รดน้ำและใส่ปุ๋ยปาล์มแปลง B',    'PALM_CARE', 1, 1, 1, 1, '2026-09-03', '2026-09-10', 2, '1100100000001', '1100100000002'),
(6,  'ตัดปาล์มแปลง B รอบ ก.ย.',       'CUTTING',   1, 1, 1, 1, '2026-09-02', '2026-09-14', 2, '1100100000001', '1100100000006'),
(7,  'ขนส่งปาล์มแปลง B ไปโรงงาน',     'HARVEST',   1, 1, 1, 1, '2026-09-08', '2026-09-20', 2, '1100100000001', '1100100000007'),
(8,  'ดูแลและตัดแต่งปาล์มแปลง A',     'PALM_CARE', 1, 1, 1, 1, '2026-09-10', '2026-09-20', 1, '1100100000001', '1100100000002'),
(9,  'เก็บเกี่ยวปาล์มรอบ ต.ค.',        'HARVEST',   0, 1, 1, 0, '2026-10-22', '2026-11-05', 1, '1100100000001', NULL),
(10, 'รดน้ำและใส่ปุ๋ยปาล์มแปลง B',    'PALM_CARE', 0, 1, 1, 0, '2026-10-22', '2026-11-03', 2, '1100100000001', NULL),
(11, 'ตัดปาล์มแปลง A รอบ ต.ค.',       'CUTTING',   0, 0, 0, 0, '2026-10-22', '2026-11-02', 1, '1100100000001', NULL),
(12, 'ขนส่งปาล์มแปลง B ไปโรงงาน',     'HARVEST',   0, 1, 1, 0, '2026-10-22', '2026-11-06', 2, '1100100000001', NULL),
(13, 'รดน้ำและใส่ปุ๋ย',                'PALM_CARE', 0, 0, 0, 0, '2026-11-05', '2026-11-30', 3, '1100100000001', NULL);


-- ─────────────────────────────────────────
-- 5. RESERVATION_LINE — รายการจัดสรรทรัพยากรต่องาน
-- date = วันที่ฝ่ายทรัพยากรกดจัดสรร (ไม่ใช่กำหนดส่งงาน)
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS reservation_line (
    reserved_id INT AUTO_INCREMENT PRIMARY KEY,
    task_id     INT NOT NULL,
    resource_id INT NOT NULL,
    date        DATE,
    status      ENUM('pending','used','returned') DEFAULT 'pending',
    FOREIGN KEY (task_id)     REFERENCES task(task_id) ON DELETE CASCADE,
    FOREIGN KEY (resource_id) REFERENCES resource(resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO reservation_line (reserved_id, task_id, resource_id, date, status) VALUES
(1, 1, 11, '2026-07-02', 'returned'),
(2, 1, 7, '2026-07-02', 'returned'),
(3, 2, 12, '2026-08-02', 'returned'),
(4, 2, 7, '2026-08-02', 'returned'),
(5, 3, 11, '2026-09-02', 'returned'),
(6, 3, 7, '2026-09-02', 'returned'),
(7, 4, 14, '2026-08-04', 'returned'),
(8, 4, 2, '2026-08-04', 'returned'),
(9, 5, 14, '2026-09-04', 'returned'),
(10, 6, 10, '2026-09-03', 'returned'),
(11, 6, 4, '2026-09-03', 'returned'),
(12, 7, 2, '2026-09-09', 'returned'),
(13, 7, 8, '2026-09-09', 'returned'),
(14, 8, 16, '2026-09-11', 'returned'),
(15, 8, 5, '2026-09-11', 'returned'),
(16, 9, 9, '2026-10-23', 'pending'),
(17, 9, 6, '2026-10-23', 'pending'),
(18, 10, 13, '2026-10-23', 'pending'),
(19, 12, 1, '2026-10-23', 'pending');


-- ─────────────────────────────────────────
-- 6. BILLS — ใบเสร็จรับซื้อปาล์ม
-- is_stored = 0 คือรอฝ่ายบัญชีบันทึกรายละเอียด (bill_number/sale_date/buyer ยังว่าง)
-- is_stored = 1 คือบันทึกครบแล้ว, stored_by = นักบัญชีผู้บันทึก (แผนก ACCOUNTING)
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS bills (
    bill_id         INT AUTO_INCREMENT PRIMARY KEY,
    bill_number     VARCHAR(50),
    bill_image_path VARCHAR(500),
    is_stored       TINYINT(1) NOT NULL DEFAULT 0,
    sale_date       DATE,
    buyer_name      VARCHAR(100),
    palm_quantity   DECIMAL(10,2) DEFAULT 0.00,
    price_per_unit  DECIMAL(8,2)  DEFAULT 0.00,
    total_amount    DECIMAL(12,2) DEFAULT 0.00,
    stored_by       CHAR(13),
    uploaded_at     TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (stored_by) REFERENCES employee(national_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO bills (bill_id, bill_number, bill_image_path, is_stored, sale_date, buyer_name, palm_quantity, price_per_unit, total_amount, stored_by, uploaded_at) VALUES
(1, NULL,            'src/main/resources/bills/bill_unstored_001.png', 0, NULL,         NULL,                         0.00,    0.00, 0.00,     NULL,            '2026-11-05 05:52:43'),
(2, NULL,            'src/main/resources/bills/bill_unstored_002.png', 0, NULL,         NULL,                         0.00,    0.00, 0.00,     NULL,            '2026-11-05 05:52:43'),
(3, 'BILL-2026-001', 'src/main/resources/bills/bill_001.png',          1, '2026-09-20', 'Palm Oil Factory Co., Ltd.', 5000.00, 6.50, 32500.00, '1100100000004', '2026-11-05 05:52:43'),
(4, 'BILL-2026-002', 'src/main/resources/bills/bill_002.png',          1, '2026-09-28', 'Thai Palm Oil Co., Ltd.',    3200.00, 6.80, 21760.00, '1100100000004', '2026-11-05 05:52:43'),
(5, NULL,            'src/main/resources/bills/bill_unstored_003.png', 0, NULL,         NULL,                         0.00,    0.00, 0.00,     NULL,            '2026-11-06 07:43:31');


-- ─────────────────────────────────────────
-- 7. AUTO_INCREMENT — ตั้งค่าตัวนับให้ตรงกับฐานข้อมูลต้นทาง
-- ─────────────────────────────────────────
ALTER TABLE bills            AUTO_INCREMENT = 9;
ALTER TABLE palm_plantation  AUTO_INCREMENT = 6;
ALTER TABLE reservation_line AUTO_INCREMENT = 21;
ALTER TABLE resource         AUTO_INCREMENT = 17;
ALTER TABLE task             AUTO_INCREMENT = 14;

-- =========================================================
-- จบไฟล์
-- =========================================================
