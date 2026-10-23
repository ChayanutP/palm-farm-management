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
    department    ENUM('RESOURCE','PALM_CARE','ACCOUNTING','HARVEST','CUTTING') NOT NULL,
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
-- แก้ไข: เพิ่ม FK เชื่อมกับ Employee (สร้าง และ รับงาน)
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS task (
    task_id            INT AUTO_INCREMENT PRIMARY KEY,
    description        VARCHAR(255),
    department         ENUM('RESOURCE','PALM_CARE','ACCOUNTING','HARVEST','CUTTING') NOT NULL,
    is_complete        BOOLEAN NOT NULL DEFAULT FALSE,  -- false=กำลังดำเนินการ, true=เสร็จสิ้น
    is_allocated       BOOLEAN NOT NULL DEFAULT FALSE,  -- false=รอจัดสรร,       true=จัดสรรแล้ว
    is_ordered         BOOLEAN NOT NULL DEFAULT FALSE,  -- false=ยังไม่ส่งงาน,    true=ส่งงานแล้ว
    requisition_status BOOLEAN NOT NULL DEFAULT FALSE,  -- false=ยังไม่เบิก,      true=เบิกแล้ว
    work_ordered_date  DATE,
    deadline           DATE,
    plantation_id      INT NOT NULL,                    -- FK จากความสัมพันธ์ "has" (เส้นคู่ = NOT NULL)
    created_by         CHAR(13) NOT NULL,               -- FK จากความสัมพันธ์ "สร้าง" (เส้นคู่ = NOT NULL)
    assigned_to        CHAR(13) NULL,                   -- FK จากความสัมพันธ์ "รับงาน" (เส้นเดี่ยว = NULL ได้ถ้ารอจ่ายงาน)
    FOREIGN KEY (plantation_id) REFERENCES palm_plantation(plantation_id),
    FOREIGN KEY (created_by)    REFERENCES employee(national_id),
    FOREIGN KEY (assigned_to)   REFERENCES employee(national_id)
);

-- ─────────────────────────────────────────
-- 5. RESERVATION LINE
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS reservation_line (
    reserved_id   INT AUTO_INCREMENT PRIMARY KEY,
    task_id       INT NOT NULL,
    resource_id   INT NOT NULL,
    date          DATE,
    status        ENUM('pending', 'used', 'returned') DEFAULT 'pending', -- ยุบรวมเหลือตัวเดียว
    FOREIGN KEY (task_id)     REFERENCES task(task_id) ON DELETE CASCADE,
    FOREIGN KEY (resource_id) REFERENCES resource(resource_id)
);

-- ─────────────────────────────────────────
-- 6. BILLS
-- แก้ไข: เอา plantation_id ออก และเพิ่ม FK เชื่อมกับ Employee (จัดเก็บ)
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
    stored_by       CHAR(13) NULL,                      -- FK จากความสัมพันธ์ "จัดเก็บ" เชื่อมกับ Employee
    uploaded_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (stored_by) REFERENCES employee(national_id)
);

-- =========================================================
-- MOCK DATA
-- =========================================================

-- 1. Employees (เอาพนักงานแผนก QUALITY ออกเพื่อให้ตรงกับการลบตาราง)
INSERT INTO employee (national_id, username, password, first_name, last_name, department, position, phone) VALUES
('1100100000001', 'resource01', 'resource_123', 'สมชาย',   'ใจดี',      'RESOURCE',   'ผู้จัดสรรทรัพยากร',        '081-111-1111'),
('1100100000002', 'palmcare01', 'palmcare_123', 'สมหญิง',  'รักต้นไม้',  'PALM_CARE',  'พนักงานดูแลปาล์ม',         '082-222-2222'),
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
(6, 'กข-5678', 'รถกระบะ 4 ประตู',                   5),
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

-- 4. Tasks (เพิ่ม created_by และ assigned_to / เอา QUALITY ออก)
INSERT INTO task
    (description, department, is_complete, is_allocated, is_ordered, requisition_status, work_ordered_date, deadline, plantation_id, created_by, assigned_to)
VALUES
    -- ══════════════════════════════════════════════════
    -- งานเก่าที่เสร็จแล้ว (is_complete=TRUE) — ไว้ทดสอบ getLatestTaskPerGroup
    -- ══════════════════════════════════════════════════

    -- HARVEST / แปลง A — 3 รอบที่ผ่านมา
    ('เก็บเกี่ยวปาล์มรอบ ก.ค.',       'HARVEST',   TRUE, TRUE, TRUE, TRUE, '2026-07-01', '2026-07-15', 1, '1100100000001', '1100100000005'),
    ('เก็บเกี่ยวปาล์มรอบ ส.ค.',       'HARVEST',   TRUE, TRUE, TRUE, TRUE, '2026-08-01', '2026-08-15', 1, '1100100000001', '1100100000005'),
    ('เก็บเกี่ยวปาล์มรอบ ก.ย.',       'HARVEST',   TRUE, TRUE, TRUE, TRUE, '2026-09-01', '2026-09-15', 1, '1100100000001', '1100100000005'),

    -- PALM_CARE / แปลง B — 2 รอบที่ผ่านมา
    ('รดน้ำและใส่ปุ๋ยปาล์มแปลง B',    'PALM_CARE', TRUE, TRUE, TRUE, TRUE, '2026-08-03', '2026-08-10', 2, '1100100000001', '1100100000002'),
    ('รดน้ำและใส่ปุ๋ยปาล์มแปลง B',    'PALM_CARE', TRUE, TRUE, TRUE, TRUE, '2026-09-03', '2026-09-10', 2, '1100100000001', '1100100000002'),

    -- PALM_CARE / แปลง A — ยังไม่มีงานใหม่ (ทดสอบว่าแสดงงานเก่าแทน)
    ('ดูแลและตัดแต่งปาล์มแปลง A',     'PALM_CARE', TRUE, TRUE, TRUE, TRUE, '2026-09-10', '2026-09-20', 1, '1100100000001', '1100100000002'),

    -- CUTTING / แปลง B — 1 รอบที่ผ่านมา
    ('ตัดปาล์มแปลง B รอบ ก.ย.',      'CUTTING',   TRUE, TRUE, TRUE, TRUE, '2026-09-02', '2026-09-14', 2, '1100100000001', '1100100000006'),

    -- HARVEST / แปลง B — 1 รอบที่ผ่านมา
    ('ขนส่งปาล์มแปลง B รอบ ก.ย.',    'HARVEST',   TRUE, TRUE, TRUE, TRUE, '2026-09-08', '2026-09-20', 2, '1100100000001', '1100100000007'),

    -- CUTTING / แปลง C — ยังไม่มีงานใหม่ (ทดสอบ)
    ('ตัดปาล์มแปลง C รอบ ก.ย.',      'CUTTING',   TRUE, TRUE, TRUE, TRUE, '2026-09-15', '2026-09-28', 3, '1100100000001', '1100100000006'),

    -- ══════════════════════════════════════════════════
    -- งานปัจจุบัน (deadline หลัง 25 ต.ค. 2026 ทุกงาน)
    -- ══════════════════════════════════════════════════

    -- [9]  HARVEST/แปลง A: เก็บเกี่ยว ต.ค. — จัดสรรแล้ว ส่งแล้ว รอเบิก
    ('เก็บเกี่ยวปาล์มรอบ ต.ค.',       'HARVEST',   FALSE, TRUE,  TRUE,  FALSE, '2026-10-23', '2026-11-05', 1, '1100100000001', '1100100000005'),

    -- [10] PALM_CARE/แปลง B: รดน้ำ ต.ค. — จัดสรรแล้ว ส่งแล้ว รอเบิก
    ('รดน้ำและใส่ปุ๋ยปาล์มแปลง B',    'PALM_CARE', FALSE, TRUE,  TRUE,  FALSE, '2026-10-23', '2026-11-03', 2, '1100100000001', '1100100000002'),

    -- [11] HARVEST/แปลง B: ขนส่ง ต.ค. — จัดสรรแล้ว ส่งแล้ว รอเบิก
    ('ขนส่งปาล์มแปลง B ไปโรงงาน',    'HARVEST',   FALSE, TRUE,  TRUE,  FALSE, '2026-10-23', '2026-11-06', 2, '1100100000001', '1100100000007'),

    -- [12] CUTTING/แปลง A: ตัดปาล์ม ต.ค. — จัดสรรแล้ว ยังไม่ส่ง (ทดสอบ Deallocate / SendTask)
    ('ตัดปาล์มแปลง A รอบ ต.ค.',      'CUTTING',   FALSE, TRUE,  FALSE, FALSE, '2026-10-23', '2026-11-02', 1, '1100100000001', NULL),

    -- [13] PALM_CARE/แปลง C: ดูแลปาล์ม — ยังไม่จัดสรร ยังไม่ส่ง (ทดสอบ CreateTask / AssignResource)
    ('ดูแลและตัดแต่งปาล์มแปลง C',     'PALM_CARE', FALSE, FALSE, FALSE, FALSE, '2026-10-23', '2026-11-10', 3, '1100100000001', NULL),

    -- [14] CUTTING/แปลง B: ตัดปาล์ม — ยังไม่จัดสรร ยังไม่ส่ง (ทดสอบ CreateTask / AssignResource)
    ('ตัดปาล์มแปลง B รอบ ต.ค.',      'CUTTING',   FALSE, FALSE, FALSE, FALSE, '2026-10-23', '2026-11-08', 2, '1100100000001', NULL);

-- 5. Reservation Lines
-- ใช้ subquery เพื่อหา task_id จริง แทนการ hardcode
-- Resource reference:
--   VEHICLE  : 1=รถบรรทุก 6ล้อ กข-1234, 3=รถบรรทุก 10ล้อ งง-8899
--              4=รถแทรกเตอร์ ตต-001, 6=รถกระบะ 4ประตู กข-5678, 7=รถกระบะ 2ประตู กข-9012
--   EQUIPMENT: 9=เครื่องตัดปาล์ม X200-01, 10=เครื่องตัดปาล์ม X200-02
--              11=มีดตัดทะลาย ชุด 1, 13=สายยางรดน้ำ ชุด A, 14=ปั๊มน้ำไฟฟ้า

-- HARVEST/แปลง A (เก็บเกี่ยว): เครื่องตัดปาล์ม X200-01 + มีดตัดทะลาย ชุด 1 (ไม่มีรถ)
INSERT INTO reservation_line (task_id, resource_id, date, status)
SELECT t.task_id, 9, '2026-10-23', 'pending' FROM task t
JOIN palm_plantation p ON t.plantation_id = p.plantation_id
WHERE t.description = 'เก็บเกี่ยวปาล์มรอบ ต.ค.' AND t.department = 'HARVEST' AND p.name = 'แปลงปาล์ม A' AND t.is_complete = FALSE;

INSERT INTO reservation_line (task_id, resource_id, date, status)
SELECT t.task_id, 11, '2026-10-23', 'pending' FROM task t
JOIN palm_plantation p ON t.plantation_id = p.plantation_id
WHERE t.description = 'เก็บเกี่ยวปาล์มรอบ ต.ค.' AND t.department = 'HARVEST' AND p.name = 'แปลงปาล์ม A' AND t.is_complete = FALSE;

-- PALM_CARE/แปลง B (รดน้ำใส่ปุ๋ย): สายยางรดน้ำ + ปั๊มน้ำไฟฟ้า
INSERT INTO reservation_line (task_id, resource_id, date, status)
SELECT t.task_id, 13, '2026-10-23', 'pending' FROM task t
JOIN palm_plantation p ON t.plantation_id = p.plantation_id
WHERE t.description = 'รดน้ำและใส่ปุ๋ยปาล์มแปลง B' AND t.department = 'PALM_CARE' AND t.is_complete = FALSE;

INSERT INTO reservation_line (task_id, resource_id, date, status)
SELECT t.task_id, 14, '2026-10-23', 'pending' FROM task t
JOIN palm_plantation p ON t.plantation_id = p.plantation_id
WHERE t.description = 'รดน้ำและใส่ปุ๋ยปาล์มแปลง B' AND t.department = 'PALM_CARE' AND t.is_complete = FALSE;

-- HARVEST/แปลง B (ขนส่ง): รถบรรทุก 10ล้อ + รถกระบะ (ไม่มีเครื่องมือ)
INSERT INTO reservation_line (task_id, resource_id, date, status)
SELECT t.task_id, 3, '2026-10-23', 'pending' FROM task t
JOIN palm_plantation p ON t.plantation_id = p.plantation_id
WHERE t.description = 'ขนส่งปาล์มแปลง B ไปโรงงาน' AND t.department = 'HARVEST' AND t.is_complete = FALSE;

INSERT INTO reservation_line (task_id, resource_id, date, status)
SELECT t.task_id, 7, '2026-10-23', 'pending' FROM task t
JOIN palm_plantation p ON t.plantation_id = p.plantation_id
WHERE t.description = 'ขนส่งปาล์มแปลง B ไปโรงงาน' AND t.department = 'HARVEST' AND t.is_complete = FALSE;

-- CUTTING/แปลง A (ตัดปาล์ม): เครื่องตัดปาล์ม X200-02 + มีดตัดทะลาย ชุด 2 + เลื่อยยนต์
INSERT INTO reservation_line (task_id, resource_id, date, status)
SELECT t.task_id, 10, '2026-10-23', 'pending' FROM task t
JOIN palm_plantation p ON t.plantation_id = p.plantation_id
WHERE t.description = 'ตัดปาล์มแปลง A รอบ ต.ค.' AND t.department = 'CUTTING' AND t.is_complete = FALSE;

INSERT INTO reservation_line (task_id, resource_id, date, status)
SELECT t.task_id, 12, '2026-10-23', 'pending' FROM task t
JOIN palm_plantation p ON t.plantation_id = p.plantation_id
WHERE t.description = 'ตัดปาล์มแปลง A รอบ ต.ค.' AND t.department = 'CUTTING' AND t.is_complete = FALSE;

INSERT INTO reservation_line (task_id, resource_id, date, status)
SELECT t.task_id, 16, '2026-10-23', 'pending' FROM task t
JOIN palm_plantation p ON t.plantation_id = p.plantation_id
WHERE t.description = 'ตัดปาล์มแปลง A รอบ ต.ค.' AND t.department = 'CUTTING' AND t.is_complete = FALSE;

-- อัปเดต resource status ตามที่จองไว้
UPDATE resource SET status = 'reserved' WHERE resource_id IN (3, 7, 9, 10, 11, 12, 13, 14, 16);

-- อัปเดต task.is_allocated = TRUE สำหรับ task ที่มี reservation แล้ว
UPDATE task SET is_allocated = TRUE WHERE task_id IN (
    SELECT DISTINCT task_id FROM reservation_line
);

-- 6. Bills (เปลี่ยนจาก plantation_id เป็น stored_by ที่เชื่อมกับพนักงานบัญชี)
-- ยังไม่จัดเก็บ (is_stored=FALSE, stored_by=NULL)
INSERT INTO bills (bill_image_path, is_stored, stored_by) VALUES
('/path/to/bill_oct_001.jpg', FALSE, NULL),
('/path/to/bill_oct_002.jpg', FALSE, NULL);

-- จัดเก็บแล้วโดยพนักงานบัญชี '1100100000004' (is_stored=TRUE)
INSERT INTO bills (bill_number, bill_image_path, is_stored, sale_date, buyer_name, palm_quantity, price_per_unit, total_amount, stored_by) VALUES
('BILL-2026-001', '/path/to/bill_sep_001.jpg', TRUE, '2026-09-20', 'บริษัท โรงงานสกัดปาล์ม จำกัด', 5000.00, 6.50, 32500.00, '1100100000004'),
('BILL-2026-002', '/path/to/bill_sep_002.jpg', TRUE, '2026-09-28', 'บริษัท ปาล์มออยล์ จำกัด',      3200.00, 6.80, 21760.00, '1100100000004');