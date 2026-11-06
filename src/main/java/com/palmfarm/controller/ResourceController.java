package com.palmfarm.controller;

import com.palmfarm.controller.model.*;
import com.palmfarm.model.*;
import com.palmfarm.util.DBConnection;
import com.palmfarm.util.SessionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResourceController {

    private final TaskModelController             taskMC       = new TaskModelController();
    private final ResourceModelController         resourceMC   = new ResourceModelController();
    private final ReservationLineModelController  rlMC         = new ReservationLineModelController();
    private final EmployeeModelController         empMC        = new EmployeeModelController();
    private final PlantationModelController       plantationMC = new PlantationModelController();

    private String errorMessage   = "";
    private String successMessage = "";

    // Helper: ดึง national_id ของผู้ใช้ปัจจุบันจาก SessionManager
    private String getCurrentUserNationalId(Connection con) {
    EmployeeModel user = SessionManager.getCurrentUser();
    if (user == null) return null;

    return user.national_id; // หรือชื่อเมธอด getter ใน EmployeeModel เช่น user.getNational_id()
}

    //  PLANTATION 
    public List<PlantationModel> getAllPlantations() {
        List<PlantationModel> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT plantation_id, name, area_rai, location FROM palm_plantation ORDER BY name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(plantationMC.mapFromResultSet(rs));
        } catch (SQLException e) { errorMessage = "ดึงข้อมูลสวนไม่สำเร็จ: " + e.getMessage(); }
        return list;
    }

    //  query TASKS แสดงชื่อผู้ออกคำสั่ง/ผู้รับผิดชอบ
    private static final String TASK_SELECT =
        "SELECT t.task_id, t.description, t.department, t.is_complete, t.is_allocated, t.is_ordered, " +
        "       t.requisition_status, t.work_ordered_date, t.deadline, t.plantation_id, " +
        "       t.created_by, t.assigned_to, " +
        "       p.name AS plantation_name, " +
        "       CONCAT(c.first_name, ' ', c.last_name) AS creator_name, " +
        "       CONCAT(a.first_name, ' ', a.last_name) AS assignee_name " +
        "FROM task t " +
        "LEFT JOIN palm_plantation p ON t.plantation_id = p.plantation_id " +
        "LEFT JOIN employee c ON t.created_by = c.national_id " +
        "LEFT JOIN employee a ON t.assigned_to = a.national_id ";

    /**
     * SELECT แบบสั้น — ใช้กับ query ที่หน้าจอไม่ได้แสดงชื่อผู้ออกคำสั่ง/ผู้รับผิดชอบ
     * ตัด JOIN employee ออก 2 ตัว ให้ดึงเฉพาะคอลัมน์ที่ถูกนำไปแสดงผลจริง
     */
    private static final String TASK_SELECT_BASIC =
        "SELECT t.task_id, t.description, t.department, t.is_complete, t.is_allocated, t.is_ordered, " +
        "       t.requisition_status, t.work_ordered_date, t.deadline, t.plantation_id, " +
        "       p.name AS plantation_name " +
        "FROM task t " +
        "LEFT JOIN palm_plantation p ON t.plantation_id = p.plantation_id ";

    public List<TaskModel> getAllTasks() {
        List<TaskModel> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(TASK_SELECT + "ORDER BY t.task_id DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(taskMC.mapFromResultSet(rs));
        } catch (SQLException e) {
            errorMessage = "ดึงข้อมูล Task ไม่สำเร็จ: " + e.getMessage();
            System.err.println("[getAllTasks ERROR] " + e.getMessage());
        }
        return list;
    }

    public List<TaskModel> getIncompleteTasks() {
        List<TaskModel> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 TASK_SELECT_BASIC + "WHERE t.is_complete = FALSE ORDER BY t.deadline ASC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(taskMC.mapFromResultSet(rs));
        } catch (SQLException e) {
            errorMessage = "ดึงข้อมูล Task ไม่สำเร็จ: " + e.getMessage();
            System.err.println("[getIncompleteTasks ERROR] " + e.getMessage());
        }
        return list;
    }

    /**
     * แสดง task ล่าสุด 1 รายการต่อกลุ่ม (department + plantation)
     * เรียงด้วย is_complete ASC (FALSE=0 ก่อน), deadline DESC
     * → งานค้างมาก่อนเสมอ ถ้าไม่มีงานค้างจึงแสดงงานเก่าที่ deadline ล่าสุด
     */
    public List<TaskModel> getLatestTaskPerGroup() {
        List<TaskModel> list = new ArrayList<>();
        String sql = TASK_SELECT_BASIC +
            "INNER JOIN (" +
            "  SELECT task_id" +
            "  FROM (" +
            "    SELECT task_id," +
            "           ROW_NUMBER() OVER (" +
            "             PARTITION BY department, plantation_id" +
            "             ORDER BY is_complete ASC, deadline DESC" +
            "           ) AS rn" +
            "    FROM task" +
            "  ) ranked" +
            "  WHERE rn = 1" +
            ") chosen ON t.task_id = chosen.task_id " +
            "ORDER BY t.department, p.name";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(taskMC.mapFromResultSet(rs));
        } catch (SQLException e) {
            errorMessage = "ดึงข้อมูล Task ไม่สำเร็จ: " + e.getMessage();
            System.err.println("[getLatestTaskPerGroup ERROR] " + e.getMessage());
        }
        return list;
    }

    public List<TaskModel> getAssignableTasks() {
        List<TaskModel> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 TASK_SELECT_BASIC +
                 "WHERE t.is_complete = FALSE " +
                 "  AND t.is_ordered = FALSE " +
                 "ORDER BY t.work_ordered_date ASC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(taskMC.mapFromResultSet(rs));
        } catch (SQLException e) {
            errorMessage = "ดึงข้อมูล Task ไม่สำเร็จ: " + e.getMessage();
        }
        return list;
    }

    public List<TaskModel> getUnorderedTasks() {
        List<TaskModel> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 TASK_SELECT_BASIC + "WHERE t.is_ordered = FALSE AND t.is_allocated = TRUE ORDER BY t.work_ordered_date ASC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(taskMC.mapFromResultSet(rs));
        } catch (SQLException e) { errorMessage = "ดึงข้อมูล Task ไม่สำเร็จ: " + e.getMessage(); }
        return list;
    }

    public List<TaskModel> getAllocatedUnorderedTasks() {
        List<TaskModel> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 TASK_SELECT_BASIC +
                 "WHERE t.is_complete = FALSE " +
                 " AND t.is_allocated = TRUE " +
                 " AND t.is_ordered = FALSE " +
                 "ORDER BY t.deadline ASC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(taskMC.mapFromResultSet(rs));
        } catch (SQLException e) {
            errorMessage = "ดึงข้อมูล Task ไม่สำเร็จ: " + e.getMessage();
            System.err.println("[getAllocatedUnorderedTasks ERROR] " + e.getMessage());
        }
        return list;
    }

    public boolean createTask(TaskModel t) {
        String checkSql = "SELECT COUNT(task_id) FROM task WHERE department=? AND plantation_id=? AND is_complete=FALSE";
        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement chk = con.prepareStatement(checkSql)) {
                chk.setString(1, taskMC.getDepartment(t));
                chk.setInt(2, taskMC.getPlantationId(t));
                try (ResultSet rs = chk.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        errorMessage = "ยังมีงานที่ยังไม่เสร็จอยู่ในแผนกนี้และสวนนี้ กรุณาเสร็จสิ้นงานเดิมก่อน";
                        return false;
                    }
                }
            }

            // ดึง national_id ของผู้สร้างงาน (ถ้าไม่มีใน Session ให้ใช้รหัสพนักงานจัดสรรเริ่มต้น)
            String createdBy = getCurrentUserNationalId(con);
            if (createdBy == null) {
                createdBy = "1100100000001";
            }

            String sql = "INSERT INTO task (description,department,is_complete,is_allocated,is_ordered," +
                         "requisition_status,work_ordered_date,deadline,plantation_id,created_by,assigned_to) " +
                         "VALUES (?,?,?,?,?,?,?,?,?,?,NULL)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, taskMC.getDescription(t));
                ps.setString(2, taskMC.getDepartment(t));
                ps.setBoolean(3, taskMC.isComplete(t));
                ps.setBoolean(4, taskMC.isAllocated(t));
                ps.setBoolean(5, taskMC.isOrdered(t));
                ps.setBoolean(6, taskMC.getRequisitionStatus(t));
                ps.setString(7, taskMC.getWorkOrderedDate(t));
                ps.setString(8, taskMC.getDeadline(t));
                ps.setInt(9, taskMC.getPlantationId(t));
                ps.setString(10, createdBy);
                ps.executeUpdate();
                successMessage = "สร้าง Task สำเร็จ";
                return true;
            }
        } catch (SQLException e) {
            errorMessage = "สร้าง Task ไม่สำเร็จ: " + e.getMessage();
            return false;
        }
    }

    public boolean updateTaskComplete(int taskId, boolean complete) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE task SET is_complete=? WHERE task_id=?")) {
            ps.setBoolean(1, complete); ps.setInt(2, taskId); ps.executeUpdate();
            successMessage = "อัปเดตสถานะ Task สำเร็จ"; return true;
        } catch (SQLException e) { errorMessage = "อัปเดต Task ไม่สำเร็จ: " + e.getMessage(); return false; }
    }

    public boolean updateDeadline(int taskId, String newDeadline) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE task SET deadline=? WHERE task_id=?")) {
            ps.setString(1, newDeadline);
            ps.setInt(2, taskId);
            ps.executeUpdate();
            successMessage = "เลื่อน Deadline สำเร็จ";
            return true;
        } catch (SQLException e) {
            errorMessage = "เลื่อน Deadline ไม่สำเร็จ: " + e.getMessage();
            return false;
        }
    }

    public boolean sendTask(int taskId) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE task SET is_ordered=TRUE WHERE task_id=?")) {
            ps.setInt(1, taskId);
            int rows = ps.executeUpdate();
            if (rows > 0) { successMessage = "ส่งงานสำเร็จ"; return true; }
            else { errorMessage = "ไม่พบ Task ที่ระบุ"; return false; }
        } catch (SQLException e) { errorMessage = "ส่งงานไม่สำเร็จ: " + e.getMessage(); return false; }
    }

    // DEALLOCATE 
    public boolean deallocateTask(int taskId) {
        String sqlUpdRes  = "UPDATE resource r " +
                            "INNER JOIN reservation_line rl ON r.resource_id = rl.resource_id " +
                            "SET r.status = 'available' " +
                            "WHERE rl.task_id = ?";
        String sqlDelRl   = "DELETE FROM reservation_line WHERE task_id = ?";
        String sqlUpdTask = "UPDATE task SET is_allocated = FALSE WHERE task_id = ?";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int releasedCount = 0;

                try (PreparedStatement ps = con.prepareStatement(sqlUpdRes)) {
                    ps.setQueryTimeout(5);
                    ps.setInt(1, taskId);
                    releasedCount = ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement(sqlDelRl)) {
                    ps.setQueryTimeout(5);
                    ps.setInt(1, taskId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement(sqlUpdTask)) {
                    ps.setQueryTimeout(5);
                    ps.setInt(1, taskId);
                    ps.executeUpdate();
                }

                con.commit();
                successMessage = "ยกเลิกการจัดสรรสำเร็จ คืนทรัพยากร " + releasedCount + " รายการ";
                return true;

            } catch (SQLException e) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            errorMessage = "ยกเลิกการจัดสรรไม่สำเร็จ: " + e.getMessage();
            return false;
        }
    }

    //  RESOURCES (JOIN Superclass + Subclasses)
    private static final String RESOURCE_SELECT =
        "SELECT r.resource_id, r.resource_type, r.status, " +
        "       v.license_plate, v.vehicle_size, v.seat_count, " +
        "       e.equip_id, e.equipment_name " +
        "FROM resource r " +
        "LEFT JOIN vehicle v ON r.resource_id = v.resource_id " +
        "LEFT JOIN equipment e ON r.resource_id = e.resource_id ";

    public List<ResourceModel> getAllResources() {
        List<ResourceModel> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(RESOURCE_SELECT + "ORDER BY r.resource_id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(resourceMC.mapFromResultSet(rs));
        } catch (SQLException e) { errorMessage = "ดึงข้อมูล Resource ไม่สำเร็จ: " + e.getMessage(); }
        return list;
    }

    public List<ResourceModel> getResourcesByType(String type) {
        List<ResourceModel> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                RESOURCE_SELECT + "WHERE r.resource_type = ? ORDER BY r.resource_id")) {
            ps.setString(1, type);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(resourceMC.mapFromResultSet(rs));
            }
        } catch (SQLException e) { errorMessage = "ดึงข้อมูล Resource ไม่สำเร็จ: " + e.getMessage(); }
        return list;
    }

    public List<ResourceModel> getAvailableResources() {
        List<ResourceModel> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 RESOURCE_SELECT + "WHERE r.status='available' ORDER BY r.resource_type, r.resource_id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(resourceMC.mapFromResultSet(rs));
        } catch (SQLException e) { errorMessage = "ดึง Resource ว่างไม่สำเร็จ: " + e.getMessage(); }
        return list;
    }

    //  RESERVATION LINES (ดึงชื่อจาก Subclass ด้วย COALESCE)
    public List<ReservationLineModel> getReservationsByTask(int taskId) {
        List<ReservationLineModel> list = new ArrayList<>();
        String sql = "SELECT rl.reserved_id, rl.task_id, rl.resource_id, rl.date, rl.status, " +
                     "       COALESCE(CONCAT(v.vehicle_size, ' (', v.license_plate, ')'), e.equipment_name) AS resource_name, " +
                     "       r.resource_type " +
                     "FROM reservation_line rl " +
                     "JOIN resource r ON rl.resource_id = r.resource_id " +
                     "LEFT JOIN vehicle v ON r.resource_id = v.resource_id " +
                     "LEFT JOIN equipment e ON r.resource_id = e.resource_id " +
                     "WHERE rl.task_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, taskId); ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(rlMC.mapFromResultSet(rs));
        } catch (SQLException e) { errorMessage = "ดึง Reservation ไม่สำเร็จ: " + e.getMessage(); }
        return list;
    }

    /**
     * ดึงรายการทรัพยากรทั้งหมดที่ถูกจัดสรรให้งานที่ยังไม่เสร็จ (is_complete = FALSE)
     * มุมมอง resource เป็นหลัก: แสดงว่าทรัพยากรแต่ละชิ้นอยู่ที่งานไหน สถานะอะไร
     * ใช้สำหรับ handleExportAllocation ใน ReportViewController
     */
    /**
     * ดึงทรัพยากรทั้งหมดพร้อมสถานะปัจจุบัน — มุมมอง resource เป็นหลัก
     * resource ที่ว่าง (available) จะแสดง task_id / description / plantation / deadline เป็น null
     * resource ที่ถูกจัดสรร (reserved/in_use) จะแสดงข้อมูลงานที่ผูกอยู่
     * กรองเฉพาะงานที่ยังไม่เสร็จ (is_complete = FALSE) ด้วย condition ใน LEFT JOIN
     */
    public List<ReservationLineModel> getAllResourcesWithCurrentTask() {
        List<ReservationLineModel> list = new ArrayList<>();
        String sql = "SELECT r.resource_id, r.resource_type, r.status AS resource_status, " +
                     "       COALESCE(CONCAT(v.vehicle_size, ' (', v.license_plate, ')'), e.equipment_name) AS resource_name, " +
                     "       rl.reserved_id, rl.task_id, rl.date, rl.status, " +
                     "       t.description AS task_description, " +
                     "       t.deadline    AS task_deadline, " +
                     "       p.name        AS plantation_name " +
                     "FROM resource r " +
                     "LEFT JOIN vehicle v           ON r.resource_id  = v.resource_id " +
                     "LEFT JOIN equipment e         ON r.resource_id  = e.resource_id " +
                     "LEFT JOIN reservation_line rl ON r.resource_id = rl.resource_id " +
                     "    AND rl.task_id IN (SELECT task_id FROM task WHERE is_complete = FALSE) " +
                     "LEFT JOIN task t              ON rl.task_id = t.task_id " +
                     "LEFT JOIN palm_plantation p   ON t.plantation_id = p.plantation_id " +
                     "ORDER BY r.resource_type, r.status = 'available' ASC, r.resource_id";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ReservationLineModel rl = new ReservationLineModel();
                rl.resource_id     = rs.getInt("resource_id");
                rl.resource_type   = rs.getString("resource_type");
                rl.resource_name   = rs.getString("resource_name");
                rl.resource_status = rs.getString("resource_status");
                // rl.status = status ของ reservation_line (อาจ null ถ้า resource ว่าง)
                rl.status          = rs.getString("status");
                rl.date            = rs.getString("date");
                // task fields — null ถ้า resource ว่าง
                int taskId = rs.getInt("task_id");
                rl.task_id          = rs.wasNull() ? 0 : taskId;
                rl.task_description = rs.getString("task_description");
                rl.task_deadline    = rs.getString("task_deadline");
                rl.plantation_name  = rs.getString("plantation_name");
                list.add(rl);
            }
        } catch (SQLException e) { errorMessage = "ดึงข้อมูลการจัดสรรทรัพยากรไม่สำเร็จ: " + e.getMessage(); }
        return list;
    }

    public boolean assignResourcesToTask(int taskId, List<Integer> resourceIds) {
        if (resourceIds.isEmpty()) {
            errorMessage = "กรุณาเลือกทรัพยากรอย่างน้อย 1 รายการ";
            return false;
        }
        String sqlDate  = "SELECT deadline FROM task WHERE task_id=?";
        String sqlIns = "INSERT INTO reservation_line (task_id, resource_id, date, status) VALUES (?, ?, ?, 'pending')";
        String sqlRes   = "UPDATE resource SET status='reserved' WHERE resource_id=?";
        String sqlAlloc = "UPDATE task SET is_allocated=TRUE WHERE task_id=?";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                String dateStr = java.time.LocalDate.now().toString();
                try (PreparedStatement dp = con.prepareStatement(sqlDate)) {
                    dp.setInt(1, taskId);
                    try (ResultSet dr = dp.executeQuery()) {
                        if (dr.next() && dr.getString("deadline") != null) {
                            dateStr = dr.getString("deadline");
                        }
                    }
                }

                try (PreparedStatement ins = con.prepareStatement(sqlIns);
                     PreparedStatement upd = con.prepareStatement(sqlRes);
                     PreparedStatement pa  = con.prepareStatement(sqlAlloc)) {
                    for (int rid : resourceIds) {
                        ins.setInt(1, taskId);
                        ins.setInt(2, rid);
                        ins.setString(3, dateStr);
                        ins.addBatch();

                        upd.setInt(1, rid);
                        upd.addBatch();
                    }
                    ins.executeBatch();
                    upd.executeBatch();

                    pa.setInt(1, taskId);
                    pa.executeUpdate();
                }

                con.commit();
                successMessage = "จัดสรรทรัพยากร " + resourceIds.size() + " รายการสำเร็จ";
                return true;
            } catch (SQLException e) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
                throw e;
            }
        } catch (SQLException e) {
            errorMessage = "จัดสรรทรัพยากรไม่สำเร็จ: " + e.getMessage();
            return false;
        }
    }

    //  EMPLOYEES 
    public List<EmployeeModel> getAllEmployees() {
        List<EmployeeModel> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT national_id, username, password, first_name, last_name, department, position, phone " +
                 "FROM employee ORDER BY department, national_id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(empMC.mapFromResultSet(rs));
        } catch (SQLException e) { errorMessage = "ดึงข้อมูลพนักงานไม่สำเร็จ: " + e.getMessage(); }
        return list;
    }

    // แก้ไข: เพิ่ม ps.setString(1, empMC.getNationalId(emp)) ให้ครบ 8 พารามิเตอร์
    public boolean addEmployee(EmployeeModel emp) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "INSERT INTO employee (national_id,username,password,first_name,last_name,department,position,phone) VALUES (?,?,?,?,?,?,?,?)")) {
            ps.setString(1, empMC.getNationalId(emp));
            ps.setString(2, empMC.getUsername(emp));
            ps.setString(3, empMC.getPassword(emp));
            ps.setString(4, empMC.getFirstName(emp));
            ps.setString(5, empMC.getLastName(emp));
            ps.setString(6, empMC.getDepartment(emp));
            ps.setString(7, empMC.getPosition(emp));
            ps.setString(8, empMC.getPhone(emp));
            ps.executeUpdate();
            successMessage = "เพิ่มพนักงานสำเร็จ";
            return true;
        } catch (SQLException e) {
            errorMessage = "เพิ่มพนักงานไม่สำเร็จ: " + e.getMessage();
            return false;
        }
    }

    //  ACCESSORS 
    public TaskModelController            task()       { return taskMC; }
    public ResourceModelController        resource()   { return resourceMC; }
    public ReservationLineModelController rl()         { return rlMC; }
    public EmployeeModelController        employee()   { return empMC; }
    public PlantationModelController      plantation() { return plantationMC; }

    public String getErrorMessage()   { return errorMessage; }
    public String getSuccessMessage() { return successMessage; }
    public void clearMessages()       { errorMessage = ""; successMessage = ""; }

    // ═══════════════════════════════════════════════
    // UC9: เพิ่มพนักงาน / UC10: แก้ไขพนักงาน
    // ═══════════════════════════════════════════════

    /**
     * แก้ไขข้อมูลพนักงาน (รวม username) — newPassword ว่าง = ไม่เปลี่ยนรหัสผ่านเดิม
     */
    public boolean updateEmployee(EmployeeModel emp, String newPassword) {
        boolean changePassword = newPassword != null && !newPassword.isBlank();
        String sql = "UPDATE employee SET username=?, first_name=?, last_name=?, department=?, position=?, phone=?" +
                     (changePassword ? ", password=?" : "") +
                     " WHERE national_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, empMC.getUsername(emp));
            ps.setString(i++, empMC.getFirstName(emp));
            ps.setString(i++, empMC.getLastName(emp));
            ps.setString(i++, empMC.getDepartment(emp));
            ps.setString(i++, empMC.getPosition(emp));
            ps.setString(i++, empMC.getPhone(emp));
            if (changePassword) ps.setString(i++, newPassword);
            ps.setString(i, empMC.getNationalId(emp));
            ps.executeUpdate();
            successMessage = "แก้ไขข้อมูลพนักงานสำเร็จ";
            return true;
        } catch (SQLException e) { errorMessage = e.getMessage(); return false; }
    }

    // ═══════════════════════════════════════════════
    // UC11: เพิ่มสวนปาล์ม / UC12: แก้ไขสวนปาล์ม
    // ═══════════════════════════════════════════════

    public boolean addPlantation(PlantationModel p) {
        String sql = "INSERT INTO palm_plantation (name, area_rai, location) VALUES (?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, plantation().getName(p));
            ps.setDouble(2, plantation().getAreaRai(p));
            ps.setString(3, plantation().getLocation(p));
            ps.executeUpdate();
            successMessage = "เพิ่มสวนปาล์มสำเร็จ";
            return true;
        } catch (SQLException e) { errorMessage = e.getMessage(); return false; }
    }

    public boolean updatePlantation(PlantationModel p) {
        String sql = "UPDATE palm_plantation SET name=?, area_rai=?, location=? WHERE plantation_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, plantation().getName(p));
            ps.setDouble(2, plantation().getAreaRai(p));
            ps.setString(3, plantation().getLocation(p));
            ps.setInt(4,    plantation().getPlantationId(p));
            ps.executeUpdate();
            successMessage = "แก้ไขสวนปาล์มสำเร็จ";
            return true;
        } catch (SQLException e) { errorMessage = e.getMessage(); return false; }
    }

    // ═══════════════════════════════════════════════
    // UC13: เพิ่มรถ / UC14: แก้ไขรถ
    // ═══════════════════════════════════════════════

    public boolean addVehicle(ResourceModel r) {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                PreparedStatement ps1 = con.prepareStatement(
                    "INSERT INTO resource (resource_type, status) VALUES ('VEHICLE','available')",
                    java.sql.Statement.RETURN_GENERATED_KEYS);
                ps1.executeUpdate();
                ResultSet rs = ps1.getGeneratedKeys();
                rs.next();
                int newId = rs.getInt(1);
                PreparedStatement ps2 = con.prepareStatement(
                    "INSERT INTO vehicle (resource_id, license_plate, vehicle_size, seat_count) VALUES (?,?,?,?)");
                ps2.setInt(1, newId);
                ps2.setString(2, resource().getLicensePlate(r));
                ps2.setString(3, resource().getVehicleSize(r));
                ps2.setInt(4,    resource().getSeatCount(r));
                ps2.executeUpdate();
                con.commit();
                successMessage = "เพิ่มรถสำเร็จ";
                return true;
            } catch (SQLException e) { con.rollback(); errorMessage = e.getMessage(); return false; }
        } catch (SQLException e) { errorMessage = e.getMessage(); return false; }
    }

    public boolean updateVehicle(ResourceModel r) {
        String sql = "UPDATE vehicle SET license_plate=?, vehicle_size=?, seat_count=? WHERE resource_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, resource().getLicensePlate(r));
            ps.setString(2, resource().getVehicleSize(r));
            ps.setInt(3,    resource().getSeatCount(r));
            ps.setInt(4,    resource().getResourceId(r));
            ps.executeUpdate();
            successMessage = "แก้ไขข้อมูลรถสำเร็จ";
            return true;
        } catch (SQLException e) { errorMessage = e.getMessage(); return false; }
    }

    // ═══════════════════════════════════════════════
    // UC15: เพิ่มอุปกรณ์ / UC16: แก้ไขอุปกรณ์
    // ═══════════════════════════════════════════════

    public boolean addEquipment(ResourceModel r) {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                PreparedStatement ps1 = con.prepareStatement(
                    "INSERT INTO resource (resource_type, status) VALUES ('EQUIPMENT','available')",
                    java.sql.Statement.RETURN_GENERATED_KEYS);
                ps1.executeUpdate();
                ResultSet rs = ps1.getGeneratedKeys();
                rs.next();
                int newId = rs.getInt(1);
                PreparedStatement ps2 = con.prepareStatement(
                    "INSERT INTO equipment (resource_id, equip_id, equipment_name) VALUES (?,?,?)");
                ps2.setInt(1, newId);
                ps2.setString(2, resource().getEquipId(r));
                ps2.setString(3, resource().getEquipmentName(r));
                ps2.executeUpdate();
                con.commit();
                successMessage = "เพิ่มอุปกรณ์สำเร็จ";
                return true;
            } catch (SQLException e) { con.rollback(); errorMessage = e.getMessage(); return false; }
        } catch (SQLException e) { errorMessage = e.getMessage(); return false; }
    }

    public boolean updateEquipment(ResourceModel r) {
        String sql = "UPDATE equipment SET equip_id=?, equipment_name=? WHERE resource_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, resource().getEquipId(r));
            ps.setString(2, resource().getEquipmentName(r));
            ps.setInt(3,    resource().getResourceId(r));
            ps.executeUpdate();
            successMessage = "แก้ไขข้อมูลอุปกรณ์สำเร็จ";
            return true;
        } catch (SQLException e) { errorMessage = e.getMessage(); return false; }
    }

}