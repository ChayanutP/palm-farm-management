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
    employee_id   INT AUTO_INCREMENT PRIMARY KEY,
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
-- 2. RESOURCE
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS resource (
    resource_id   INT AUTO_INCREMENT PRIMARY KEY,
    resource_name VARCHAR(100) NOT NULL,
    resource_type ENUM('VEHICLE','EQUIPMENT','OTHER') NOT NULL,
    status        ENUM('available','reserved','in_use','maintenance') DEFAULT 'available',
    description   VARCHAR(255)
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
    is_ordered         BOOLEAN NOT NULL DEFAULT FALSE,  -- false=ยังไม่ส่งงาน,  true=ส่งงานแล้ว
    requisition_status BOOLEAN NOT NULL DEFAULT FALSE,  -- false=ยังไม่เบิก,    true=เบิกแล้ว
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

-- Employees
-- username / password / ฝ่าย
INSERT INTO employee (username, password, first_name, last_name, department, position, phone) VALUES
('resource01', 'resource_123', 'สมชาย',   'ใจดี',      'RESOURCE',   'ผู้จัดสรรทรัพยากร',        '081-111-1111'),
('palmcare01', 'palmcare_123', 'สมหญิง',  'รักต้นไม้',  'PALM_CARE',  'พนักงานดูแลปาล์ม',         '082-222-2222'),
('quality01',  'quality_123',  'วิชัย',    'ตรวจดี',    'QUALITY',    'เจ้าหน้าที่ตรวจสอบคุณภาพ', '083-333-3333'),
('account01',  'account_123',  'มาลี',     'บัญชีเก่ง', 'ACCOUNTING', 'นักบัญชี',                 '084-444-4444'),
('harvest01',  'harvest_123',  'ประสิทธิ์', 'เก็บเกี่ยว', 'HARVEST',   'พนักงานเก็บเกี่ยว',        '085-555-5555'),
('cutting01',  'cutting_123',  'วรวุฒิ',  'ตัดดี',    'CUTTING',    'พนักงานตัด/ปลูก',          '086-777-7777'),
('harvest02',  'harvest_123',  'สุดา',     'ขนส่งดี',   'HARVEST',    'คนขับรถขนส่ง',              '085-666-6666');

-- Palm Plantations
INSERT INTO palm_plantation (name, area_rai, location) VALUES
('แปลงปาล์ม A', 50.00, 'หมู่ 1 ต.บ้านสวน อ.เมือง จ.กระบี่'),
('แปลงปาล์ม B', 80.00, 'หมู่ 3 ต.คลองประสงค์ อ.เมือง จ.กระบี่'),
('แปลงปาล์ม C', 30.00, 'หมู่ 5 ต.อ่าวนาง อ.เมือง จ.กระบี่');

-- Resources
-- resource_type: VEHICLE / EQUIPMENT / OTHER
-- status:        available / reserved / in_use / maintenance
INSERT INTO resource (resource_name, resource_type, status, description) VALUES
('รถบรรทุก 6 ล้อ ทะเบียน กข-1234', 'VEHICLE',   'available', 'รถบรรทุกขนาดใหญ่'),
('รถแทรกเตอร์ รุ่น A',             'VEHICLE',   'available', 'แทรกเตอร์เอนกประสงค์'),
('รถกระบะ ทะเบียน กข-5678',        'VEHICLE',   'available', 'รถกระบะใช้งานทั่วไป'),
('เครื่องตัดปาล์ม รุ่น X200',       'EQUIPMENT', 'available', 'เครื่องตัดปาล์มอัตโนมัติ'),
('เครื่องตัดปาล์ม รุ่น X100',       'EQUIPMENT', 'available', 'เครื่องตัดปาล์มมือสอง'),
('สายยางรดน้ำ ชุด A',              'EQUIPMENT', 'available', 'สายยาง 50 เมตร'),
('ปุ๋ยเคมี ชุด 1 (50 ถุง)',         'OTHER',     'available', 'ปุ๋ยสูตร 16-16-8'),
('ยาฆ่าแมลง ชุด A',                'OTHER',     'available', 'สำหรับกำจัดแมลงศัตรูปาล์ม');

-- Tasks
-- is_complete  : FALSE=กำลังดำเนินการ, TRUE=เสร็จสิ้น
-- is_allocated : FALSE=รอจัดสรร,       TRUE=จัดสรรแล้ว
-- is_ordered   : FALSE=ยังไม่ส่งงาน,  TRUE=ส่งงานแล้ว
INSERT INTO task
    (description, department, is_complete, is_allocated, is_ordered, requisition_status, work_ordered_date, deadline, plantation_id)
VALUES
    -- งานที่ยังไม่ส่ง (is_ordered=FALSE) → ยังไม่ผ่านการมอบหมาย
    ('เก็บเกี่ยวปาล์มรอบ ต.ค.',     'HARVEST',   FALSE, FALSE, FALSE, FALSE, '2026-10-01', '2026-10-15', 1),
    ('รดน้ำและใส่ปุ๋ยปาล์มแปลง B',  'PALM_CARE', FALSE, FALSE, FALSE, FALSE, '2026-10-03', '2026-10-10', 2),
    ('ตรวจสอบคุณภาพปาล์มรอบ ต.ค.', 'QUALITY',   FALSE, FALSE, FALSE, FALSE, '2026-10-05', '2026-10-12', 1),
    -- งานที่ส่งแล้ว (is_ordered=TRUE) → ผ่านการมอบหมาย พร้อมดำเนินการ
    ('ขนส่งปาล์มไปโรงงาน',          'HARVEST',   FALSE, TRUE,  TRUE,  FALSE, '2026-10-08', '2026-10-16', 1);

-- Reservation Lines
-- used_status   : pending / used / returned
-- return_status : not_returned / returned
INSERT INTO reservation_line (task_id, resource_id, date, used_status, return_status) VALUES
(4, 1, '2026-10-16', 'pending', 'not_returned'),  -- ขนส่ง → รถบรรทุก 6 ล้อ
(4, 3, '2026-10-16', 'pending', 'not_returned');  -- ขนส่ง → รถกระบะ

-- อัปเดต resource status ให้ตรงกับการจอง
UPDATE resource SET status = 'reserved' WHERE resource_id IN (1, 3);

-- Bills — ยังไม่จัดเก็บ (is_stored=FALSE)
INSERT INTO bills (bill_image_path, is_stored, plantation_id) VALUES
('/path/to/bill_oct_001.jpg', FALSE, 1),
('/path/to/bill_oct_002.jpg', FALSE, 2);

-- Bills — จัดเก็บแล้ว (is_stored=TRUE) รอกรอกข้อมูล
INSERT INTO bills (bill_number, bill_image_path, is_stored, sale_date, buyer_name, palm_quantity, price_per_unit, total_amount, plantation_id) VALUES
('BILL-2026-001', '/path/to/bill_sep_001.jpg', TRUE, '2026-09-20', 'บริษัท โรงงานสกัดปาล์ม จำกัด', 5000.00, 6.50, 32500.00, 1),
('BILL-2026-002', '/path/to/bill_sep_002.jpg', TRUE, '2026-09-28', 'บริษัท ปาล์มออยล์ จำกัด',      3200.00, 6.80, 21760.00, 2);

-- Quality Inspections
-- quality_id: A / B / C / D / REJECT
INSERT INTO quality_inspection (plantation_id, task_id, quality_id, defect_details, date, inspector_name, inspection_date, amount) VALUES
(1, 3, 'A', 'ไม่พบข้อบกพร่อง',                     '2026-10-05', 'วิชัย ตรวจดี', '2026-10-05', 4800.00),
(2, NULL, 'B', 'พบปาล์มสุกน้อยบางส่วน ประมาณ 5%', '2026-09-15', 'วิชัย ตรวจดี', '2026-09-15', 3100.00);

-- =========================================================
-- QUERY REFERENCE
-- =========================================================

-- Login:
--   SELECT * FROM employee WHERE username = ? AND password = ?;

-- หน้า CreateTask — task ล่าสุดต่อกลุ่ม (dept+plantation):
--   SELECT t.*, p.name AS plantation_name FROM task t
--   LEFT JOIN palm_plantation p ON t.plantation_id = p.plantation_id
--   INNER JOIN (SELECT department, plantation_id, MAX(deadline) AS max_deadline
--               FROM task GROUP BY department, plantation_id) latest
--   ON t.department=latest.department AND t.plantation_id=latest.plantation_id AND t.deadline=latest.max_deadline;

-- หน้า SendTask — task ที่ยังไม่ส่ง:
--   SELECT * FROM task WHERE is_ordered = FALSE;

-- หน้าฝ่ายต่างๆ — task ที่ยังไม่เสร็จของฝ่ายนี้:
--   SELECT * FROM task WHERE department = ? AND is_complete = FALSE;

-- หน้าคืนอุปกรณ์ / เสร็จสิ้นงาน — ต้อง is_ordered = TRUE ก่อนจึงกดได้:
--   UPDATE task SET is_complete = TRUE WHERE task_id = ?;
--   UPDATE resource SET status = 'available' WHERE resource_id IN (...);
