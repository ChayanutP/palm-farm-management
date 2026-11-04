package com.palmfarm.controller;

import com.palmfarm.controller.model.*;
import com.palmfarm.model.*;
import com.palmfarm.util.DBConnection;
import com.palmfarm.util.SessionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PalmCareController {

    private final TaskModelController            taskMC = new TaskModelController();
    private final ReservationLineModelController rlMC   = new ReservationLineModelController();
    private final EmployeeModelController        empMC  = new EmployeeModelController();

    private String errorMessage   = "";
    private String successMessage = "";

    // Helper: ดึง national_id ของพนักงานที่กำลังล็อกอินอยู่
    private String getCurrentUserNationalId(Connection con) {
    EmployeeModel user = SessionManager.getCurrentUser();
    if (user == null) return null;

    return user.national_id; // หรือชื่อเมธอด getter ใน EmployeeModel เช่น user.getNational_id()
}

    // ============================================================
    // UC: บันทึกการใช้ทรัพยากร
    // ============================================================

    public List<TaskModel> getUncompletedTasksForCurrentDept() {
        List<TaskModel> list = new ArrayList<>();
        String dept = empMC.getDepartment(SessionManager.getCurrentUser());
        String sql  = """
            SELECT t.task_id, t.description, t.department, t.is_complete, t.is_allocated, t.is_ordered,
                   t.requisition_status, t.work_ordered_date, t.deadline, t.plantation_id, t.assigned_to,
                   p.name AS plantation_name
            FROM task t
            LEFT JOIN palm_plantation p ON t.plantation_id = p.plantation_id
            WHERE t.is_complete = FALSE AND t.is_ordered = TRUE AND t.is_allocated = TRUE AND t.department = ? AND t.requisition_status = FALSE
            ORDER BY t.deadline ASC
            """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dept);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(taskMC.mapFromResultSet(rs));
        } catch (SQLException e) {
            errorMessage = "ดึงข้อมูล Task ไม่สำเร็จ: " + e.getMessage();
        }
        return list;
    }

    /** งานที่รอคืนทรัพยากร/จบงาน — เห็นเฉพาะงานที่ผู้ใช้ปัจจุบันเป็นผู้รับผิดชอบ (assigned_to) เท่านั้น */
    public List<TaskModel> getTasksForReturn() {
        List<TaskModel> list = new ArrayList<>();
        EmployeeModel user = SessionManager.getCurrentUser();
        if (user == null) return list;
        String sql  = """
            SELECT t.task_id, t.description, t.department, t.is_complete, t.is_allocated, t.is_ordered,
                   t.requisition_status, t.work_ordered_date, t.deadline, t.plantation_id, t.assigned_to,
                   p.name AS plantation_name
            FROM task t
            LEFT JOIN palm_plantation p ON t.plantation_id = p.plantation_id
            WHERE t.is_complete = FALSE AND t.is_ordered = TRUE AND t.is_allocated = TRUE
              AND t.department = ? AND t.requisition_status = TRUE AND t.assigned_to = ?
            ORDER BY t.deadline ASC
            """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, empMC.getDepartment(user));
            ps.setString(2, user.national_id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(taskMC.mapFromResultSet(rs));
        } catch (SQLException e) {
            errorMessage = "ดึงข้อมูล Task ไม่สำเร็จ: " + e.getMessage();
        }
        return list;
    }

    public List<ReservationLineModel> getReservationsForTask(int taskId) {
        List<ReservationLineModel> list = new ArrayList<>();
        String sql = "SELECT rl.reserved_id, rl.task_id, rl.resource_id, rl.date, rl.status, r.resource_type, " +
                     "COALESCE(CONCAT(v.vehicle_size, ' (', v.license_plate, ')'), e.equipment_name) AS resource_name " +
                     "FROM reservation_line rl " +
                     "JOIN resource r ON rl.resource_id = r.resource_id " +
                     "LEFT JOIN vehicle v ON r.resource_id = v.resource_id " +
                     "LEFT JOIN equipment e ON r.resource_id = e.resource_id " +
                     "WHERE rl.task_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, taskId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(rlMC.mapFromResultSet(rs));
        } catch (SQLException e) {
            errorMessage = "ดึง Reservation ไม่สำเร็จ: " + e.getMessage();
        }
        return list;
    }

    /**
     * รับคำสั่ง + เบิกทรัพยากร
     * ทุกคนในฝ่ายเห็นงานนี้ได้จนกว่าจะมีคนกดรับ — คนแรกที่กดสำเร็จจะเป็นผู้รับผิดชอบ (assigned_to)
     * การแย่งรับทำด้วย UPDATE เดียวที่มีเงื่อนไข requisition_status = FALSE:
     * ถ้ามีสองคนกดพร้อมกัน ฐานข้อมูลจะให้ผ่านได้คนเดียว (affected rows = 1) อีกคนได้ 0 และจะถูกแจ้งว่ามีผู้รับไปแล้ว
     */
    public boolean markResourceAsUsed(int taskId) {
        EmployeeModel user = SessionManager.getCurrentUser();
        if (user == null) {
            errorMessage = "ไม่พบผู้ใช้ที่ล็อกอิน กรุณาเข้าสู่ระบบใหม่";
            return false;
        }
        String sqlClaim = """
            UPDATE task
            SET requisition_status = TRUE, assigned_to = ?
            WHERE task_id = ? AND department = ?
              AND is_ordered = TRUE AND is_allocated = TRUE
              AND is_complete = FALSE AND requisition_status = FALSE
            """;
        String sqlLines = "UPDATE reservation_line SET status = 'used' WHERE task_id = ?";
        String sqlRes = """
            UPDATE resource r
            JOIN reservation_line rl ON r.resource_id = rl.resource_id
            SET r.status = 'in_use' WHERE rl.task_id = ?
            """;
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int claimed;
                try (PreparedStatement ps = con.prepareStatement(sqlClaim)) {
                    ps.setString(1, user.national_id);
                    ps.setInt(2, taskId);
                    ps.setString(3, empMC.getDepartment(user));
                    claimed = ps.executeUpdate();
                }
                if (claimed == 0) {                      // มีคนรับไปก่อนแล้ว หรืองานไม่อยู่ในสถานะที่รับได้
                    con.rollback();
                    errorMessage = describeClaimFailure(con, taskId);
                    return false;
                }
                try (PreparedStatement ps = con.prepareStatement(sqlLines)) {
                    ps.setInt(1, taskId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(sqlRes)) {
                    ps.setInt(1, taskId);
                    ps.executeUpdate();
                }
                con.commit();
                successMessage = "รับคำสั่งสำเร็จ คุณเป็นผู้รับผิดชอบงานนี้ (คืนทรัพยากรได้ที่เมนู \"คืนทรัพยากร\")";
                return true;
            } catch (SQLException e) {
                con.rollback(); throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            errorMessage = "บันทึกการใช้ทรัพยากรไม่สำเร็จ: " + e.getMessage();
            return false;
        }
    }

    /** หาสาเหตุที่รับงานไม่สำเร็จ เพื่อแจ้งผู้ใช้ให้ตรงกับความจริงล่าสุดในฐานข้อมูล */
    private String describeClaimFailure(Connection con, int taskId) throws SQLException {
        String sql = """
            SELECT t.requisition_status, CONCAT(e.first_name, ' ', e.last_name) AS owner_name
            FROM task t LEFT JOIN employee e ON t.assigned_to = e.national_id
            WHERE t.task_id = ?
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, taskId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getBoolean("requisition_status") && rs.getString("owner_name") != null)
                    return "งานนี้มีผู้รับไปแล้ว: " + rs.getString("owner_name");
            }
        }
        return "ไม่สามารถรับงานนี้ได้ งานอาจเสร็จสิ้นแล้วหรือไม่ใช่งานของฝ่ายคุณ";
    }

    // ============================================================
    // UC: คืนทรัพยากร
    // ============================================================

    public List<TaskModel> getAssignedTasks() {
        List<TaskModel> list = new ArrayList<>();
        String sql = """
            SELECT t.task_id, t.description, t.department, t.is_complete, t.is_allocated, t.is_ordered,
                   t.requisition_status, t.work_ordered_date, t.deadline, t.plantation_id, t.assigned_to,
                   p.name AS plantation_name
            FROM task t
            LEFT JOIN palm_plantation p ON t.plantation_id = p.plantation_id
            WHERE t.department = ? AND t.is_complete = FALSE
            ORDER BY t.deadline DESC
            """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String dept = empMC.getDepartment(SessionManager.getCurrentUser());
            ps.setString(1, dept);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(taskMC.mapFromResultSet(rs));
        } catch (SQLException e) {
            errorMessage = "ดึงข้อมูลงานไม่สำเร็จ: " + e.getMessage();
        }
        return list;
    }

    /** คืนทรัพยากรและจบงาน — ทำได้เฉพาะผู้รับผิดชอบงานนั้น (assigned_to) เท่านั้น */
    public boolean completeTaskAndReturnResources(int taskId) {
        EmployeeModel user = SessionManager.getCurrentUser();
        if (user == null) {
            errorMessage = "ไม่พบผู้ใช้ที่ล็อกอิน กรุณาเข้าสู่ระบบใหม่";
            return false;
        }
        String sqlTask = "UPDATE task SET is_complete = TRUE " +
                         "WHERE task_id = ? AND assigned_to = ? AND requisition_status = TRUE AND is_complete = FALSE";
        String sqlLines = "UPDATE reservation_line SET status = 'returned' WHERE task_id = ?";
        String sqlRes = """
            UPDATE resource r
            JOIN reservation_line rl ON r.resource_id = rl.resource_id
            SET r.status = 'available' WHERE rl.task_id = ?
            """;
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int done;
                try (PreparedStatement ps = con.prepareStatement(sqlTask)) {
                    ps.setInt(1, taskId);
                    ps.setString(2, user.national_id);
                    done = ps.executeUpdate();
                }
                if (done == 0) {
                    con.rollback();
                    errorMessage = "ไม่สามารถคืนทรัพยากรได้ คุณไม่ใช่ผู้รับผิดชอบงานนี้ หรืองานนี้เสร็จสิ้นไปแล้ว";
                    return false;
                }
                try (PreparedStatement ps = con.prepareStatement(sqlLines)) {
                    ps.setInt(1, taskId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(sqlRes)) {
                    ps.setInt(1, taskId);
                    ps.executeUpdate();
                }
                con.commit();
                successMessage = "คืนทรัพยากรและงานเสร็จสิ้นสำเร็จ";
                return true;
            } catch (SQLException e) {
                con.rollback(); throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            errorMessage = "บันทึกไม่สำเร็จ: " + e.getMessage();
            return false;
        }
    }

    // ============================================================
    // Exposed Model Controller Accessors
    // ============================================================

    public TaskModelController            task()     { return taskMC; }
    public ReservationLineModelController rl()       { return rlMC;   }

    // ============================================================
    // Messages
    // ============================================================

    public String getErrorMessage()   { return errorMessage; }
    public String getSuccessMessage() { return successMessage; }
    public void clearMessages()       { errorMessage = ""; successMessage = ""; }
}