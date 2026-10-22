-- =========================================================
-- Palm Farm Management System — Database Schema & Mock Data
-- =========================================================

CREATE DATABASE IF NOT EXISTS palm_farm
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE palm_farm;

-- ─────────────────────────────────────────
-- 1. EMPLOYEE
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS employee (
    national_id   CHAR(13)     NOT NULL PRIMARY KEY,   -- เลขบัตรประชาชน 13 หลัก
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password      VARCHAR(100) NOT NULL,
    first_name    VARCHAR(60)  NOT NULL,
    last_name     VARCHAR(60)  NOT NULL,
    department    ENUM('RESOURCE','PALM_CARE','QUALITY','ACCOUNTING','HARVEST','CUTTING') NOT NULL,
    position      VARCHAR(80),
    phone         VARCHAR(20),
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ─────────────────────────────────────────
-- 2. RESOURCE (Superclass)
-- Attributes ร่วม: resourceId, ชนิด, สถานะ
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS resource (
    resource_id   INT AUTO_INCREMENT PRIMARY KEY,
    resource_type ENUM('VEHICLE', 'EQUIPMENT') NOT NULL,
    status        ENUM('available', 'reserved', 'in_use', 'maintenance') DEFAULT 'available'
);

-- ─────────────────────────────────────────
-- 2.1 VEHICLE (Subclass ของ Resource)
-- Attributes เฉพาะ: เลขทะเบียน, ขนาดรถยนต์, จำนวนที่นั่ง
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS vehicle (
    resource_id   INT PRIMARY KEY,
    license_plate VARCHAR(50) NOT NULL,
    vehicle_size  VARCHAR(100),
    seat_count    INT DEFAULT 2,
    FOREIGN KEY (resource_id) REFERENCES resource(resource_id) ON DELETE CASCADE
);

-- ─────────────────────────────────────────
-- 2.2 EQUIPMENT (Subclass ของ Resource)
-- Attributes เฉพาะ: EquipId, ชื่ออุปกรณ์
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS equipment (
    resource_id    INT PRIMARY KEY,
    equip_id       VARCHAR(50),
    equipment_name VARCHAR(100) NOT NULL,
    FOREIGN KEY (resource_id) REFERENCES resource(resource_id) ON DELETE CASCADE
);

-- ─────────────────────────────────────────
-- 3. PALM PLANTATION
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS palm_plantation (
    plantation_id INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    area_rai      DECIMAL(8,2),
    location      VARCHAR(200)
);

-- ─────────────────────────────────────────
-- 4. TASK
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS task (
    task_id            INT AUTO_INCREMENT PRIMARY KEY,
    description        VARCHAR(255),
    department         ENUM('RESOURCE','PALM_CARE','QUALITY','ACCOUNTING','HARVEST','CUTTING') NOT NULL,
    is_complete        BOOLEAN NOT NULL DEFAULT FALSE,  -- false=กำลังดำเนินการ, true=เสร็จสิ้น
    is_allocated       BOOLEAN NOT NULL DEFAULT FALSE,  -- false=รอจัดสรร,       true=จัดสรรแล้ว
    is_ordered         BOOLEAN NOT NULL DEFAULT FALSE,  -- false=ยังไม่ส่งงาน,    true=ส่งงานแล้ว
    requisition_status BOOLEAN NOT NULL DEFAULT FALSE,  -- false=ยังไม่เบิก,      true=เบิกแล้ว
    work_ordered_date  DATE,
    deadline           DATE,
    plantation_id      INT NOT NULL,
    FOREIGN KEY (plantation_id) REFERENCES palm_plantation(plantation_id)
);

-- ─────────────────────────────────────────
-- 5. RESERVATION LINE
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS reservation_line (
    reserved_id   INT AUTO_INCREMENT PRIMARY KEY,
    task_id       INT NOT NULL,
    resource_id   INT NOT NULL,
    date          DATE,
    used_status   ENUM('pending','used','returned') DEFAULT 'pending',
    return_status ENUM('not_returned','returned')   DEFAULT 'not_returned',
    FOREIGN KEY (task_id)     REFERENCES task(task_id),
    FOREIGN KEY (resource_id) REFERENCES resource(resource_id)
);

-- ─────────────────────────────────────────
-- 6. QUALITY INSPECTION
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS quality_inspection (
    inspection_id   INT AUTO_INCREMENT PRIMARY KEY,
    plantation_id   INT NOT NULL,
    task_id         INT,
    quality_id      ENUM('A','B','C','D','REJECT') NOT NULL,
    defect_details  TEXT,
    date            DATE,
    inspector_name  VARCHAR(100),
    inspection_date DATE,
    amount          DECIMAL(10,2),
    FOREIGN KEY (plantation_id) REFERENCES palm_plantation(plantation_id),
    FOREIGN KEY (task_id)       REFERENCES task(task_id)
);

-- ─────────────────────────────────────────
-- 7. BILLS
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS bills (
    bill_id         INT AUTO_INCREMENT PRIMARY KEY,
    bill_number     VARCHAR(50),
    bill_image_path VARCHAR(500),
    is_stored       BOOLEAN NOT NULL DEFAULT FALSE,
    sale_date       DATE,
    buyer_name      VARCHAR(100),
    palm_quantity   DECIMAL(10,2) DEFAULT 0,
    price_per_unit  DECIMAL(8,2)  DEFAULT 0,
    total_amount    DECIMAL(12,2) DEFAULT 0,
    plantation_id   INT,
    uploaded_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (plantation_id) REFERENCES palm_plantation(plantation_id)
);

-- =========================================================
-- MOCK DATA
-- =========================================================

-- 1. Employees
INSERT INTO employee (national_id, username, password, first_name, last_name, department, position, phone) VALUES
('1100100000001', 'resource01', 'resource_123', 'สมชาย',   'ใจดี',      'RESOURCE',   'ผู้จัดสรรทรัพยากร',        '081-111-1111'),
('1100100000002', 'palmcare01', 'palmcare_123', 'สมหญิง',  'รักต้นไม้',  'PALM_CARE',  'พนักงานดูแลปาล์ม',         '082-222-2222'),
('1100100000003', 'quality01',  'quality_123',  'วิชัย',    'ตรวจดี',    'QUALITY',    'เจ้าหน้าที่ตรวจสอบคุณภาพ', '083-333-3333'),
('1100100000004', 'account01',  'account_123',  'มาลี',     'บัญชีเก่ง', 'ACCOUNTING', 'นักบัญชี',                 '084-444-4444'),
('1100100000005', 'harvest01',  'harvest_123',  'ประสิทธิ์', 'เก็บเกี่ยว', 'HARVEST',   'พนักงานเก็บเกี่ยว',        '085-555-5555'),
('1100100000006', 'cutting01',  'cutting_123',  'วรวุฒิ',  'ตัดดี',    'CUTTING',    'พนักงานตัด/ปลูก',          '086-777-7777'),
('1100100000007', 'harvest02',  'harvest_123',  'สุดา',     'ขนส่งดี',   'HARVEST',    'คนขับรถขนส่ง',              '085-666-6666');

-- 2. Palm Plantations
INSERT INTO palm_plantation (name, area_rai, location) VALUES
('แปลงปาล์ม A', 50.00, 'หมู่ 1 ต.บ้านสวน อ.เมือง จ.กระบี่'),
('แปลงปาล์ม B', 80.00, 'หมู่ 3 ต.คลองประสงค์ อ.เมือง จ.กระบี่'),
('แปลงปาล์ม C', 30.00, 'หมู่ 5 ต.อ่าวนาง อ.เมือง จ.กระบี่');

-- 3. Resources (Superclass)
INSERT INTO resource (resource_id, resource_type, status) VALUES
-- ยานพาหนะ (VEHICLE: 1-8)
(1, 'VEHICLE', 'available'),
(2, 'VEHICLE', 'available'),
(3, 'VEHICLE', 'available'),
(4, 'VEHICLE', 'available'),
(5, 'VEHICLE', 'available'),
(6, 'VEHICLE', 'available'),
(7, 'VEHICLE', 'available'),
(8, 'VEHICLE', 'available'),
-- อุปกรณ์ (EQUIPMENT: 9-16)
(9,  'EQUIPMENT', 'available'),
(10, 'EQUIPMENT', 'available'),
(11, 'EQUIPMENT', 'available'),
(12, 'EQUIPMENT', 'available'),
(13, 'EQUIPMENT', 'available'),
(14, 'EQUIPMENT', 'available'),
(15, 'EQUIPMENT', 'available'),
(16, 'EQUIPMENT', 'available');

-- 3.1 Vehicle Details (Subclass)
INSERT INTO vehicle (resource_id, license_plate, vehicle_size, seat_count) VALUES
(1, 'กข-1234', 'รถบรรทุก 6 ล้อ บรรทุกได้ 10 ตัน', 3),
(2, 'ขก-5566', 'รถบรรทุก 6 ล้อ บรรทุกได้ 10 ตัน', 3),
(3, 'งง-8899', 'รถบรรทุก 10 ล้อ บรรทุกได้ 20 ตัน', 2),
(4, 'ตต-001',  'รถแทรกเตอร์ กำลัง 75 แรงม้า',       1),
(5, 'ตต-002',  'รถแทรกเตอร์ กำลัง 90 แรงม้า',       1),
(6, 'กข-5678', 'รถกระบะ 4 ประตู',                  5),
(7, 'กข-9012', 'รถกระบะ 2 ประตู ขนของ',             2),
(8, 'ลล-111',  'รถตัก (Loader)',                   1);

-- 3.2 Equipment Details (Subclass)
INSERT INTO equipment (resource_id, equip_id, equipment_name) VALUES
(9,  'EQ-001', 'เครื่องตัดปาล์ม รุ่น X200 เลขที่ 01'),
(10, 'EQ-002', 'เครื่องตัดปาล์ม รุ่น X200 เลขที่ 02'),
(11, 'EQ-003', 'มีดตัดทะลายปาล์ม ชุด 1'),
(12, 'EQ-004', 'มีดตัดทะลายปาล์ม ชุด 2'),
(13, 'EQ-005', 'สายยางรดน้ำ ชุด A (50 เมตร)'),
(14, 'EQ-006', 'ปั๊มน้ำไฟฟ้า เลขที่ 01 (3 แรงม้า)'),
(15, 'EQ-007', 'เครื่องพ่นยา ชุด 1 (สะพายหลัง 20 ลิตร)'),
(16, 'EQ-008', 'เลื่อยยนต์ เลขที่ 01');

-- 4. Tasks
INSERT INTO task
    (description, department, is_complete, is_allocated, is_ordered, requisition_status, work_ordered_date, deadline, plantation_id)
VALUES
    -- ══════════════════════════════════════════════════
    -- งานเก่าที่เสร็จแล้ว (is_complete=TRUE)
    -- ใช้ทดสอบ getLatestTaskPerGroup → ต้องไม่แสดงอันนี้
    -- ถ้ายังมีงานใหม่ของ dept+สวนเดิมอยู่
    -- ══════════════════════════════════════════════════

    -- [เก่า] HARVEST/แปลง A: เก็บเกี่ยว ก.ค. → เสร็จแล้ว
    ('เก็บเกี่ยวปาล์มรอบ ก.ค.',      'HARVEST',   TRUE,  TRUE,  TRUE,  TRUE,  '2026-07-01', '2026-07-15', 1),
    -- [เก่า] HARVEST/แปลง A: เก็บเกี่ยว ส.ค. → เสร็จแล้ว
    ('เก็บเกี่ยวปาล์มรอบ ส.ค.',      'HARVEST',   TRUE,  TRUE,  TRUE,  TRUE,  '2026-08-01', '2026-08-15', 1),
    -- [เก่า] HARVEST/แปลง A: เก็บเกี่ยว ก.ย. → เสร็จแล้ว
    ('เก็บเกี่ยวปาล์มรอบ ก.ย.',      'HARVEST',   TRUE,  TRUE,  TRUE,  TRUE,  '2026-09-01', '2026-09-15', 1),

    -- [เก่า] PALM_CARE/แปลง B: รดน้ำ ส.ค. → เสร็จแล้ว
    ('รดน้ำและใส่ปุ๋ยปาล์มแปลง B',   'PALM_CARE', TRUE,  TRUE,  TRUE,  TRUE,  '2026-08-03', '2026-08-10', 2),
    -- [เก่า] PALM_CARE/แปลง B: รดน้ำ ก.ย. → เสร็จแล้ว
    ('รดน้ำและใส่ปุ๋ยปาล์มแปลง B',   'PALM_CARE', TRUE,  TRUE,  TRUE,  TRUE,  '2026-09-03', '2026-09-10', 2),

    -- [เก่า] QUALITY/แปลง A: ตรวจสอบ ก.ย. → เสร็จแล้ว
    ('ตรวจสอบคุณภาพปาล์มรอบ ก.ย.',  'QUALITY',   TRUE,  TRUE,  TRUE,  TRUE,  '2026-09-05', '2026-09-12', 1),

    -- [เก่า] CUTTING/แปลง B: ตัดปาล์ม ก.ย. → เสร็จแล้ว (คนละสวนกับปัจจุบัน)
    ('ตัดปาล์มแปลง B รอบ ก.ย.',     'CUTTING',   TRUE,  TRUE,  TRUE,  TRUE,  '2026-09-02', '2026-09-14', 2),

    -- [เก่า] HARVEST/แปลง B: ขนส่ง ก.ย. → เสร็จแล้ว
    ('ขนส่งปาล์มแปลง B ไปโรงงาน',   'HARVEST',   TRUE,  TRUE,  TRUE,  TRUE,  '2026-09-08', '2026-09-20', 2),

    -- [เก่า] PALM_CARE/แปลง A: ดูแลปาล์ม ก.ย. → เสร็จแล้ว (ยังไม่มีงานใหม่แปลง A)
    ('ดูแลและตัดแต่งปาล์มแปลง A',    'PALM_CARE', TRUE,  TRUE,  TRUE,  TRUE,  '2026-09-10', '2026-09-20', 1),

    -- ══════════════════════════════════════════════════
    -- งานปัจจุบัน (บางอันยังไม่เสร็จ)
    -- getLatestTaskPerGroup ควรแสดงอันเหล่านี้แทนงานเก่า
    -- ══════════════════════════════════════════════════

    -- task [ปัจจุบัน]: HARVEST/แปลง A → เก็บเกี่ยว ต.ค. ยังไม่เสร็จ
    ('เก็บเกี่ยวปาล์มรอบ ต.ค.',      'HARVEST',   FALSE, TRUE,  TRUE,  FALSE, '2026-10-22', '2026-11-05', 1),
    -- task [ปัจจุบัน]: PALM_CARE/แปลง B → รดน้ำ ต.ค. ยังไม่เสร็จ
    ('รดน้ำและใส่ปุ๋ยปาล์มแปลง B',   'PALM_CARE', FALSE, TRUE,  TRUE,  FALSE, '2026-10-22', '2026-11-03', 2),
    -- task [ปัจจุบัน]: QUALITY/แปลง A → ตรวจสอบ ต.ค. ยังไม่เสร็จ
    ('ตรวจสอบคุณภาพปาล์มรอบ ต.ค.',  'QUALITY',   FALSE, FALSE, FALSE, FALSE, '2026-10-22', '2026-11-04', 1),
    -- task [ปัจจุบัน]: CUTTING/แปลง A → ตัดปาล์ม ต.ค. ยังไม่เสร็จ
    ('ตัดปาล์มแปลง A รอบ ต.ค.',     'CUTTING',   FALSE, FALSE, FALSE, FALSE, '2026-10-22', '2026-11-02', 1),
    -- task [ปัจจุบัน]: HARVEST/แปลง B → ขนส่ง ต.ค. ยังไม่เสร็จ
    ('ขนส่งปาล์มแปลง B ไปโรงงาน',   'HARVEST',   FALSE, TRUE,  TRUE,  FALSE, '2026-10-22', '2026-11-06', 2);
    -- หมายเหตุ: PALM_CARE/แปลง A ไม่มีงานปัจจุบัน
    -- → getLatestTaskPerGroup ควรแสดงงานเก่าที่เสร็จแล้ว (deadline ล่าสุดคือ ก.ย.)

-- 5. Reservation Lines
INSERT INTO reservation_line (task_id, resource_id, date, used_status, return_status) VALUES
-- task 1: เครื่องตัดปาล์ม X200-01 (id=9), รถกระบะ กข-5678 (id=6)
(1, 9,  '2026-10-15', 'pending', 'not_returned'),
(1, 6,  '2026-10-15', 'pending', 'not_returned'),
-- task 2: สายยางรดน้ำ ชุด A (id=13)
(2, 13, '2026-10-10', 'pending', 'not_returned'),
-- task 5: รถบรรทุก 6 ล้อ กข-1234 (id=1)
(5, 1,  '2026-10-16', 'pending', 'not_returned');

-- อัปเดตสถานะ resource ตามการจอง
UPDATE resource SET status = 'reserved' WHERE resource_id IN (1, 6, 9, 13);

-- 6. Bills
-- ยังไม่จัดเก็บ (is_stored=FALSE)
INSERT INTO bills (bill_image_path, is_stored, plantation_id) VALUES
('/path/to/bill_oct_001.jpg', FALSE, 1),
('/path/to/bill_oct_002.jpg', FALSE, 2);

-- จัดเก็บแล้ว (is_stored=TRUE)
INSERT INTO bills (bill_number, bill_image_path, is_stored, sale_date, buyer_name, palm_quantity, price_per_unit, total_amount, plantation_id) VALUES
('BILL-2026-001', '/path/to/bill_sep_001.jpg', TRUE, '2026-09-20', 'บริษัท โรงงานสกัดปาล์ม จำกัด', 5000.00, 6.50, 32500.00, 1),
('BILL-2026-002', '/path/to/bill_sep_002.jpg', TRUE, '2026-09-28', 'บริษัท ปาล์มออยล์ จำกัด',      3200.00, 6.80, 21760.00, 2);

-- 7. Quality Inspections
INSERT INTO quality_inspection (plantation_id, task_id, quality_id, defect_details, date, inspector_name, inspection_date, amount) VALUES
(1, 3,    'A', 'ไม่พบข้อบกพร่อง',                     '2026-10-05', 'วิชัย ตรวจดี', '2026-10-05', 4800.00),
(2, NULL, 'B', 'พบปาล์มสุกน้อยบางส่วน ประมาณ 5%', '2026-09-15', 'วิชัย ตรวจดี', '2026-09-15', 3100.00);

-- =========================================================
-- QUERY REFERENCE
-- =========================================================

-- 1. ดูรายการยานพาหนะทั้งหมด พร้อมสถานะ:
-- SELECT r.resource_id, v.license_plate, v.vehicle_size, v.seat_count, r.status
-- FROM resource r
-- JOIN vehicle v ON r.resource_id = v.resource_id;

-- 2. ดูรายการอุปกรณ์ทำสวนทั้งหมด พร้อมสถานะ:
-- SELECT r.resource_id, e.equip_id, e.equipment_name, r.status
-- FROM resource r
-- JOIN equipment e ON r.resource_id = e.resource_id;

-- 3. ดูการจองทรัพยากรใน Task (แสดงได้ทั้งรถและอุปกรณ์อัตโนมัติ):
-- SELECT 
--     rl.reserved_id,
--     rl.task_id,
--     t.description AS task_description,
--     r.resource_type,
--     COALESCE(v.license_plate, e.equipment_name) AS resource_name,
--     rl.date,
--     rl.used_status,
--     rl.return_status
-- FROM reservation_line rl
-- JOIN resource r ON rl.resource_id = r.resource_id
-- JOIN task t ON rl.task_id = t.task_id
-- LEFT JOIN vehicle v ON r.resource_id = v.resource_id
-- LEFT JOIN equipment e ON r.resource_id = e.resource_id;

-- 4. Task ล่าสุดต่อกลุ่ม (dept + plantation):
-- SELECT t.*, p.name AS plantation_name FROM task t
-- LEFT JOIN palm_plantation p ON t.plantation_id = p.plantation_id
-- INNER JOIN (SELECT department, plantation_id, MAX(deadline) AS max_deadline
--             FROM task GROUP BY department, plantation_id) latest
-- ON t.department = latest.department 
-- AND t.plantation_id = latest.plantation_id 
-- AND t.deadline = latest.max_deadline;