package com.palmfarm.controller;

import com.palmfarm.controller.model.*;
import com.palmfarm.model.*;
import com.palmfarm.util.DBConnection;
import com.palmfarm.util.SessionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HarvestController {

    private final TaskModelController            taskMC = new TaskModelController();
    private final ReservationLineModelController rlMC   = new ReservationLineModelController();
    private final EmployeeModelController        empMC  = new EmployeeModelController();

    private String errorMessage   = "";
    private String successMessage = "";

    // ============================================================
    // TASKS
    // ============================================================

    public List<TaskModel> getHarvestTasks() {
        List<TaskModel> list = new ArrayList<>();
        String dept = empMC.getDepartment(SessionManager.getCurrentUser());
        String sql  = """
            SELECT t.*, p.name AS plantation_name
            FROM task t
            LEFT JOIN palm_plantation p ON t.plantation_id = p.plantation_id
            WHERE t.department = ? AND t.is_complete = FALSE
            ORDER BY t.deadline ASC
            """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dept);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(taskMC.mapFromResultSet(rs));
        } catch (SQLException e) {
            errorMessage = "ดึงข้อมูลงานไม่สำเร็จ: " + e.getMessage();
        }
        return list;
    }

    public List<ReservationLineModel> getReservationsForTask(int taskId) {
        List<ReservationLineModel> list = new ArrayList<>();
        String sql = """
            SELECT rl.*, r.resource_name, r.resource_type
            FROM reservation_line rl
            JOIN resource r ON rl.resource_id = r.resource_id
            WHERE rl.task_id = ?
            """;
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

    public boolean markResourcesUsed(int taskId) {
        String sql1 = "UPDATE reservation_line SET used_status='used' WHERE task_id=?";
        String sql2 = "UPDATE task SET requisition_status=TRUE WHERE task_id=?";
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps1 = con.prepareStatement(sql1);
                 PreparedStatement ps2 = con.prepareStatement(sql2)) {
                ps1.setInt(1, taskId); ps1.executeUpdate();
                ps2.setInt(1, taskId); ps2.executeUpdate();
                con.commit();
                successMessage = "บันทึกการใช้ทรัพยากรสำเร็จ";
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

    public boolean completeTask(int taskId) {
        String sql1 = "UPDATE task SET is_complete=TRUE WHERE task_id=?";
        String sql2 = "UPDATE reservation_line SET return_status='returned' WHERE task_id=?";
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
                successMessage = "บันทึกงานเสร็จสิ้นสำเร็จ";
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

    public TaskModelController            task() { return taskMC; }
    public ReservationLineModelController rl()   { return rlMC;   }

    // ============================================================
    // Messages
    // ============================================================

    public String getErrorMessage()   { return errorMessage; }
    public String getSuccessMessage() { return successMessage; }
    public void clearMessages()       { errorMessage = ""; successMessage = ""; }
}
