-- phpMyAdmin SQL Dump
-- version 5.2.3
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Nov 06, 2026 at 08:46 AM
-- Server version: 10.11.16-MariaDB
-- PHP Version: 8.5.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
CREATE DATABASE IF NOT EXISTS palm_farm
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE palm_farm;
--

-- --------------------------------------------------------

--
-- Table structure for table `bills`
--

CREATE TABLE `bills` (
  `bill_id` int(11) NOT NULL,
  `bill_number` varchar(50) DEFAULT NULL,
  `bill_image_path` varchar(500) DEFAULT NULL,
  `is_stored` tinyint(1) NOT NULL DEFAULT 0,
  `sale_date` date DEFAULT NULL,
  `buyer_name` varchar(100) DEFAULT NULL,
  `palm_quantity` decimal(10,2) DEFAULT 0.00,
  `price_per_unit` decimal(8,2) DEFAULT 0.00,
  `total_amount` decimal(12,2) DEFAULT 0.00,
  `stored_by` char(13) DEFAULT NULL,
  `uploaded_at` timestamp NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `bills`
--

INSERT INTO `bills` (`bill_id`, `bill_number`, `bill_image_path`, `is_stored`, `sale_date`, `buyer_name`, `palm_quantity`, `price_per_unit`, `total_amount`, `stored_by`, `uploaded_at`) VALUES
(1, NULL, 'src/main/resources/bills/bill_unstored_001.png', 1, NULL, NULL, 0.00, 0.00, 0.00, '1100100000004', '2026-11-05 05:52:43'),
(2, NULL, 'src/main/resources/bills/bill_unstored_002.png', 0, NULL, NULL, 0.00, 0.00, 0.00, NULL, '2026-11-05 05:52:43'),
(3, 'BILL-2026-001', 'src/main/resources/bills/bill_001.png', 1, '2026-09-20', 'Palm Oil Factory Co., Ltd.', 5000.00, 6.50, 32500.00, '1100100000007', '2026-11-05 05:52:43'),
(4, 'BILL-2026-002', 'src/main/resources/bills/bill_002.png', 1, '2026-09-28', 'Thai Palm Oil Co., Ltd.', 3200.00, 6.80, 21760.00, '1100100000007', '2026-11-05 05:52:43'),
(5, NULL, 'src/main/resources/bills/bill_unstored_003.png', 1, NULL, NULL, 0.00, 0.00, 0.00, '1100100000004', '2026-11-06 07:43:31');

-- --------------------------------------------------------

--
-- Table structure for table `employee`
--

CREATE TABLE `employee` (
  `national_id` char(13) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `first_name` varchar(60) NOT NULL,
  `last_name` varchar(60) NOT NULL,
  `department` enum('RESOURCE','PALM_CARE','ACCOUNTING','HARVEST','CUTTING') NOT NULL,
  `position` varchar(80) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `employee`
--

INSERT INTO `employee` (`national_id`, `username`, `password`, `first_name`, `last_name`, `department`, `position`, `phone`, `created_at`) VALUES
('1100100000001', 'resource01', 'resource_123', 'สมชาย', 'ใจดี', 'RESOURCE', 'ผู้จัดสรรทรัพยากร', '081-111-1111', '2026-11-05 05:52:42'),
('1100100000002', 'palmcare01', 'palmcare_123', 'สมหญิง', 'รักต้นไม้', 'PALM_CARE', 'พนักงานดูแลปาล์ม', '082-222-2222', '2026-11-05 05:52:42'),
('1100100000004', 'account01', 'account_123', 'มาลี', 'บัญชีเก่ง', 'ACCOUNTING', 'นักบัญชี', '084-444-4444', '2026-11-05 05:52:42'),
('1100100000005', 'harvest01', 'harvest_123', 'ประสิทธิ์', 'เก็บเกี่ยว', 'HARVEST', 'พนักงานเก็บเกี่ยว', '085-555-5555', '2026-11-05 05:52:42'),
('1100100000006', 'cutting01', 'cutting_123', 'วรวุฒิ', 'ตัดดี', 'CUTTING', 'พนักงานตัด/ปลูก', '086-777-7777', '2026-11-05 05:52:42'),
('1100100000007', 'harvest02', 'harvest_123', 'สุดา', 'ขนส่งดี', 'HARVEST', 'คนขับรถขนส่ง', '085-666-6666', '2026-11-05 05:52:42');

-- --------------------------------------------------------

--
-- Table structure for table `equipment`
--

CREATE TABLE `equipment` (
  `resource_id` int(11) NOT NULL,
  `equip_id` varchar(50) DEFAULT NULL,
  `equipment_name` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `equipment`
--

INSERT INTO `equipment` (`resource_id`, `equip_id`, `equipment_name`) VALUES
(9, 'EQ-001', 'เครื่องตัดปาล์ม รุ่น X200 เลขที่ 01'),
(10, 'EQ-002', 'เครื่องตัดปาล์ม รุ่น X200 เลขที่ 02'),
(11, 'EQ-003', 'มีดตัดทะลายปาล์ม ชุด 1'),
(12, 'EQ-004', 'มีดตัดทะลายปาล์ม ชุด 2'),
(13, 'EQ-005', 'สายยางรดน้ำ ชุด A (50 เมตร)'),
(14, 'EQ-006', 'ปั๊มน้ำไฟฟ้า เลขที่ 01 (3 แรงม้า)'),
(15, 'EQ-007', 'เครื่องพ่นยา ชุด 1 (สะพายหลัง 20 ลิตร)'),
(16, 'EQ-008', 'เลื่อยยนต์ เลขที่ 01');

-- --------------------------------------------------------

--
-- Table structure for table `palm_plantation`
--

CREATE TABLE `palm_plantation` (
  `plantation_id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `area_rai` decimal(8,2) DEFAULT NULL,
  `location` varchar(200) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `palm_plantation`
--

INSERT INTO `palm_plantation` (`plantation_id`, `name`, `area_rai`, `location`) VALUES
(1, 'แปลงปาล์ม A', 50.00, 'หมู่ 1 ต.บ้านสวน อ.เมือง จ.กระบี่'),
(2, 'แปลงปาล์ม B', 80.00, 'หมู่ 3 ต.คลองประสงค์ อ.เมือง จ.กระบี่'),
(3, 'แปลงปาล์ม C', 30.00, 'หมู่ 5 ต.อ่าวนาง อ.เมือง จ.กระบี่'),
(4, 'แปลงปาล์ม D', 40.00, 'หมู่ 7 ต.โตนด อ.เมือง จ.กระบี่'),
(5, 'แปลงปาล์ม E', 45.00, 'หมู่ 9 ต.สำลี อ.เมือง จ.กระบี่');

-- --------------------------------------------------------

--
-- Table structure for table `reservation_line`
--

CREATE TABLE `reservation_line` (
  `reserved_id` int(11) NOT NULL,
  `task_id` int(11) NOT NULL,
  `resource_id` int(11) NOT NULL,
  `date` date DEFAULT NULL,
  `status` enum('pending','used','returned') DEFAULT 'pending'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `reservation_line`
--

INSERT INTO `reservation_line` (`reserved_id`, `task_id`, `resource_id`, `date`, `status`) VALUES
(1, 1, 11, '2026-07-15', 'returned'),
(2, 1, 7, '2026-07-15', 'returned'),
(3, 2, 12, '2026-08-15', 'returned'),
(4, 2, 7, '2026-08-15', 'returned'),
(5, 3, 11, '2026-09-15', 'returned'),
(6, 3, 7, '2026-09-15', 'returned'),
(7, 4, 14, '2026-08-10', 'returned'),
(8, 4, 2, '2026-08-10', 'returned'),
(9, 5, 14, '2026-09-10', 'returned'),
(10, 6, 10, '2026-09-14', 'returned'),
(11, 6, 4, '2026-09-14', 'returned'),
(12, 7, 2, '2026-09-20', 'returned'),
(13, 7, 8, '2026-09-20', 'returned'),
(14, 8, 16, '2026-09-20', 'returned'),
(15, 8, 5, '2026-09-20', 'returned'),
(16, 9, 9, '2026-10-22', 'pending'),
(17, 9, 6, '2026-10-22', 'pending'),
(18, 10, 13, '2026-10-22', 'pending'),
(19, 12, 1, '2026-10-22', 'pending');

-- --------------------------------------------------------

--
-- Table structure for table `resource`
--

CREATE TABLE `resource` (
  `resource_id` int(11) NOT NULL,
  `resource_type` enum('VEHICLE','EQUIPMENT') NOT NULL,
  `status` enum('available','reserved','in_use','maintenance') DEFAULT 'available'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `resource`
--

INSERT INTO `resource` (`resource_id`, `resource_type`, `status`) VALUES
(1, 'VEHICLE', 'reserved'),
(2, 'VEHICLE', 'available'),
(3, 'VEHICLE', 'available'),
(4, 'VEHICLE', 'available'),
(5, 'VEHICLE', 'available'),
(6, 'VEHICLE', 'reserved'),
(7, 'VEHICLE', 'available'),
(8, 'VEHICLE', 'available'),
(9, 'EQUIPMENT', 'reserved'),
(10, 'EQUIPMENT', 'available'),
(11, 'EQUIPMENT', 'available'),
(12, 'EQUIPMENT', 'available'),
(13, 'EQUIPMENT', 'reserved'),
(14, 'EQUIPMENT', 'available'),
(15, 'EQUIPMENT', 'available'),
(16, 'EQUIPMENT', 'available');

-- --------------------------------------------------------

--
-- Table structure for table `task`
--

CREATE TABLE `task` (
  `task_id` int(11) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `department` enum('RESOURCE','PALM_CARE','ACCOUNTING','HARVEST','CUTTING') NOT NULL,
  `is_complete` tinyint(1) NOT NULL DEFAULT 0,
  `is_allocated` tinyint(1) NOT NULL DEFAULT 0,
  `is_ordered` tinyint(1) NOT NULL DEFAULT 0,
  `requisition_status` tinyint(1) NOT NULL DEFAULT 0,
  `work_ordered_date` date DEFAULT NULL,
  `deadline` date DEFAULT NULL,
  `plantation_id` int(11) NOT NULL,
  `created_by` char(13) NOT NULL,
  `assigned_to` char(13) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `task`
--

INSERT INTO `task` (`task_id`, `description`, `department`, `is_complete`, `is_allocated`, `is_ordered`, `requisition_status`, `work_ordered_date`, `deadline`, `plantation_id`, `created_by`, `assigned_to`) VALUES
(1, 'เก็บเกี่ยวปาล์มรอบ ก.ค.', 'HARVEST', 1, 1, 1, 1, '2026-07-01', '2026-07-15', 1, '1100100000001', '1100100000005'),
(2, 'เก็บเกี่ยวปาล์มรอบ ส.ค.', 'HARVEST', 1, 1, 1, 1, '2026-08-01', '2026-08-15', 1, '1100100000001', '1100100000005'),
(3, 'เก็บเกี่ยวปาล์มรอบ ก.ย.', 'HARVEST', 1, 1, 1, 1, '2026-09-01', '2026-09-15', 1, '1100100000001', '1100100000005'),
(4, 'รดน้ำและใส่ปุ๋ยปาล์มแปลง B', 'PALM_CARE', 1, 1, 1, 1, '2026-08-03', '2026-08-10', 2, '1100100000001', '1100100000002'),
(5, 'รดน้ำและใส่ปุ๋ยปาล์มแปลง B', 'PALM_CARE', 1, 1, 1, 1, '2026-09-03', '2026-09-10', 2, '1100100000001', '1100100000002'),
(6, 'ตัดปาล์มแปลง B รอบ ก.ย.', 'CUTTING', 1, 1, 1, 1, '2026-09-02', '2026-09-14', 2, '1100100000001', '1100100000006'),
(7, 'ขนส่งปาล์มแปลง B ไปโรงงาน', 'HARVEST', 1, 1, 1, 1, '2026-09-08', '2026-09-20', 2, '1100100000001', '1100100000007'),
(8, 'ดูแลและตัดแต่งปาล์มแปลง A', 'PALM_CARE', 1, 1, 1, 1, '2026-09-10', '2026-09-20', 1, '1100100000001', '1100100000002'),
(9, 'เก็บเกี่ยวปาล์มรอบ ต.ค.', 'HARVEST', 0, 1, 1, 0, '2026-10-22', '2026-11-05', 1, '1100100000001', NULL),
(10, 'รดน้ำและใส่ปุ๋ยปาล์มแปลง B', 'PALM_CARE', 0, 1, 1, 0, '2026-10-22', '2026-11-03', 2, '1100100000001', NULL),
(11, 'ตัดปาล์มแปลง A รอบ ต.ค.', 'CUTTING', 0, 0, 0, 0, '2026-10-22', '2026-11-02', 1, '1100100000001', NULL),
(12, 'ขนส่งปาล์มแปลง B ไปโรงงาน', 'HARVEST', 0, 1, 1, 0, '2026-10-22', '2026-11-06', 2, '1100100000001', NULL),
(13, 'รดน้ำและใส่ปุ๋ย', 'PALM_CARE', 0, 0, 0, 0, '2026-11-05', '2026-11-30', 3, '1100100000001', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `vehicle`
--

CREATE TABLE `vehicle` (
  `resource_id` int(11) NOT NULL,
  `license_plate` varchar(50) NOT NULL,
  `vehicle_size` varchar(100) DEFAULT NULL,
  `seat_count` int(11) DEFAULT 2
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `vehicle`
--

INSERT INTO `vehicle` (`resource_id`, `license_plate`, `vehicle_size`, `seat_count`) VALUES
(1, 'กข-1234', 'รถบรรทุก 6 ล้อ บรรทุกได้ 10 ตัน', 3),
(2, 'ขก-5566', 'รถบรรทุก 6 ล้อ บรรทุกได้ 10 ตัน', 3),
(3, 'งง-8899', 'รถบรรทุก 10 ล้อ บรรทุกได้ 20 ตัน', 2),
(4, 'ตต-001', 'รถแทรกเตอร์ กำลัง 75 แรงม้า', 1),
(5, 'ตต-002', 'รถแทรกเตอร์ กำลัง 90 แรงม้า', 1),
(6, 'กข-5678', 'รถกระบะ 4 ประตู', 5),
(7, 'กข-9012', 'รถกระบะ 2 ประตู ขนของ', 2),
(8, 'ลล-111', 'รถตัก (Loader)', 1);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `bills`
--
ALTER TABLE `bills`
  ADD PRIMARY KEY (`bill_id`),
  ADD KEY `stored_by` (`stored_by`);

--
-- Indexes for table `employee`
--
ALTER TABLE `employee`
  ADD PRIMARY KEY (`national_id`),
  ADD UNIQUE KEY `username` (`username`);

--
-- Indexes for table `equipment`
--
ALTER TABLE `equipment`
  ADD PRIMARY KEY (`resource_id`);

--
-- Indexes for table `palm_plantation`
--
ALTER TABLE `palm_plantation`
  ADD PRIMARY KEY (`plantation_id`);

--
-- Indexes for table `reservation_line`
--
ALTER TABLE `reservation_line`
  ADD PRIMARY KEY (`reserved_id`),
  ADD KEY `task_id` (`task_id`),
  ADD KEY `resource_id` (`resource_id`);

--
-- Indexes for table `resource`
--
ALTER TABLE `resource`
  ADD PRIMARY KEY (`resource_id`);

--
-- Indexes for table `task`
--
ALTER TABLE `task`
  ADD PRIMARY KEY (`task_id`),
  ADD KEY `plantation_id` (`plantation_id`),
  ADD KEY `created_by` (`created_by`),
  ADD KEY `assigned_to` (`assigned_to`);

--
-- Indexes for table `vehicle`
--
ALTER TABLE `vehicle`
  ADD PRIMARY KEY (`resource_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `bills`
--
ALTER TABLE `bills`
  MODIFY `bill_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `palm_plantation`
--
ALTER TABLE `palm_plantation`
  MODIFY `plantation_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `reservation_line`
--
ALTER TABLE `reservation_line`
  MODIFY `reserved_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=21;

--
-- AUTO_INCREMENT for table `resource`
--
ALTER TABLE `resource`
  MODIFY `resource_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- AUTO_INCREMENT for table `task`
--
ALTER TABLE `task`
  MODIFY `task_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `bills`
--
ALTER TABLE `bills`
  ADD CONSTRAINT `bills_ibfk_1` FOREIGN KEY (`stored_by`) REFERENCES `employee` (`national_id`);

--
-- Constraints for table `equipment`
--
ALTER TABLE `equipment`
  ADD CONSTRAINT `equipment_ibfk_1` FOREIGN KEY (`resource_id`) REFERENCES `resource` (`resource_id`) ON DELETE CASCADE;

--
-- Constraints for table `reservation_line`
--
ALTER TABLE `reservation_line`
  ADD CONSTRAINT `reservation_line_ibfk_1` FOREIGN KEY (`task_id`) REFERENCES `task` (`task_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `reservation_line_ibfk_2` FOREIGN KEY (`resource_id`) REFERENCES `resource` (`resource_id`);

--
-- Constraints for table `task`
--
ALTER TABLE `task`
  ADD CONSTRAINT `task_ibfk_1` FOREIGN KEY (`plantation_id`) REFERENCES `palm_plantation` (`plantation_id`),
  ADD CONSTRAINT `task_ibfk_2` FOREIGN KEY (`created_by`) REFERENCES `employee` (`national_id`),
  ADD CONSTRAINT `task_ibfk_3` FOREIGN KEY (`assigned_to`) REFERENCES `employee` (`national_id`);

--
-- Constraints for table `vehicle`
--
ALTER TABLE `vehicle`
  ADD CONSTRAINT `vehicle_ibfk_1` FOREIGN KEY (`resource_id`) REFERENCES `resource` (`resource_id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
