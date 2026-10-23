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
            SELECT t.*, p.name AS plantation_name
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

    public List<TaskModel> getTasksForReturn() {
        List<TaskModel> list = new ArrayList<>();
        String dept = empMC.getDepartment(SessionManager.getCurrentUser());
        String sql  = """
            SELECT t.*, p.name AS plantation_name
            FROM task t
            LEFT JOIN palm_plantation p ON t.plantation_id = p.plantation_id
            WHERE t.is_complete = FALSE AND t.is_ordered = TRUE AND t.is_allocated = TRUE AND t.department = ? AND t.requisition_status = TRUE
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

    public List<ReservationLineModel> getReservationsForTask(int taskId) {
        List<ReservationLineModel> list = new ArrayList<>();
        String sql = "SELECT rl.*, r.resource_type, " +
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

    // แก้ไข: บันทึกผู้รับงานลง assigned_to และเปลี่ยนสถานะ resource เป็น 'in_use'
    public boolean markResourceAsUsed(int taskId) {
        String sql1 = "UPDATE reservation_line SET status = 'used' WHERE task_id = ?";
        String sql2 = "UPDATE task SET requisition_status = TRUE, assigned_to = COALESCE(assigned_to, ?) WHERE task_id = ?";
        String sql3 = """
            UPDATE resource r
            JOIN reservation_line rl ON r.resource_id = rl.resource_id
            SET r.status = 'in_use' WHERE rl.task_id = ?
            """;
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps1 = con.prepareStatement(sql1);
                 PreparedStatement ps2 = con.prepareStatement(sql2);
                 PreparedStatement ps3 = con.prepareStatement(sql3)) {
                String assignedTo = getCurrentUserNationalId(con);

                ps1.setInt(1, taskId);
                ps1.executeUpdate();

                ps2.setString(1, assignedTo);
                ps2.setInt(2, taskId);
                ps2.executeUpdate();

                ps3.setInt(1, taskId);
                ps3.executeUpdate();

                con.commit();
                successMessage = "บันทึกการใช้ทรัพยากรสำเร็จ";
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

    // ============================================================
    // UC: คืนทรัพยากร
    // ============================================================

    public List<TaskModel> getAssignedTasks() {
        List<TaskModel> list = new ArrayList<>();
        String sql = """
            SELECT t.*, p.name AS plantation_name
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

    public boolean completeTaskAndReturnResources(int taskId) {
       String sql1 = "UPDATE reservation_line SET status = 'returned' WHERE task_id = ?";
        String sql2 = "UPDATE task SET is_complete=TRUE WHERE task_id=?";
        String sql3 = """
            UPDATE resource r
            JOIN reservation_line rl ON r.resource_id = rl.resource_id
            SET r.status = 'available' WHERE rl.task_id = ?
            """;
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps1 = con.prepareStatement(sql1);
                 PreparedStatement ps2 = con.prepareStatement(sql2);
                 PreparedStatement ps3 = con.prepareStatement(sql3)) {
                ps1.setInt(1, taskId); ps1.executeUpdate();
                ps2.setInt(1, taskId); ps2.executeUpdate();
                ps3.setInt(1, taskId); ps3.executeUpdate();
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