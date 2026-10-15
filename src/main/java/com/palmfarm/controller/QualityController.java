package com.palmfarm.controller;

import com.palmfarm.controller.model.*;
import com.palmfarm.model.*;
import com.palmfarm.util.DBConnection;
import com.palmfarm.util.SessionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QualityController {

    private final TaskModelController              taskMC       = new TaskModelController();
    private final ReservationLineModelController   rlMC         = new ReservationLineModelController();
    private final EmployeeModelController          empMC        = new EmployeeModelController();
    private final QualityInspectionModelController qiMC         = new QualityInspectionModelController();
    private final PlantationModelController        plantationMC = new PlantationModelController();

    private String errorMessage   = "";
    private String successMessage = "";

    // ── TASKS (รับคำสั่ง / เบิก / คืน) ──────────────────────────

    public List<TaskModel> getQualityTasks() {
        List<TaskModel> list = new ArrayList<>();
        String sql = """
            SELECT t.*, p.name AS plantation_name
            FROM task t
            LEFT JOIN palm_plantation p ON t.plantation_id = p.plantation_id
            WHERE t.department = 'QUALITY' AND t.is_complete = FALSE
            ORDER BY t.deadline ASC
            """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(taskMC.mapFromResultSet(rs));
        } catch (SQLException e) {
            errorMessage = "ดึงข้อมูล Task ไม่สำเร็จ: " + e.getMessage();
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

    public boolean markResourceAsUsed(int taskId) {
        String sql1 = "UPDATE reservation_line SET used_status='used' WHERE task_id=?";
        String sql2 = "UPDATE task SET requisition_status=TRUE WHERE task_id=?";
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps1 = con.prepareStatement(sql1);
                 PreparedStatement ps2 = con.prepareStatement(sql2)) {
                ps1.setInt(1, taskId); ps1.executeUpdate();
                ps2.setInt(1, taskId); ps2.executeUpdate();
                con.commit();
                successMessage = "บันทึกการเบิกอุปกรณ์สำเร็จ";
                return true;
            } catch (SQLException e) { con.rollback(); throw e; }
            finally { con.setAutoCommit(true); }
        } catch (SQLException e) {
            errorMessage = "บันทึกไม่สำเร็จ: " + e.getMessage();
            return false;
        }
    }

    public boolean completeTaskAndReturnResources(int taskId) {
        String sql1 = "UPDATE reservation_line SET return_status='returned', used_status='returned' WHERE task_id=?";
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
            } catch (SQLException e) { con.rollback(); throw e; }
            finally { con.setAutoCommit(true); }
        } catch (SQLException e) {
            errorMessage = "บันทึกไม่สำเร็จ: " + e.getMessage();
            return false;
        }
    }

    // ── PLANTATIONS ───────────────────────────────────────────────

    public List<PlantationModel> getAllPlantations() {
        List<PlantationModel> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM palm_plantation ORDER BY name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(plantationMC.mapFromResultSet(rs));
        } catch (SQLException e) {
            errorMessage = "ดึงข้อมูลสวนไม่สำเร็จ: " + e.getMessage();
        }
        return list;
    }

    // ── QUALITY INSPECTION ────────────────────────────────────────

    public List<QualityInspectionModel> getAllInspections() {
        List<QualityInspectionModel> list = new ArrayList<>();
        // JOIN plantation เพื่อแสดงชื่อสวน
        String sql = """
            SELECT qi.*, p.name AS plantation_name
            FROM quality_inspection qi
            LEFT JOIN palm_plantation p ON qi.plantation_id = p.plantation_id
            ORDER BY qi.date DESC
            """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(qiMC.mapFromResultSet(rs));
        } catch (SQLException e) {
            errorMessage = "ดึงผลตรวจสอบไม่สำเร็จ: " + e.getMessage();
        }
        return list;
    }

    /**
     * บันทึกผลตรวจสอบต้นปาล์ม — ไม่ผูก task_id
     * เลือกสวน + เกรด + รายละเอียด + ผู้ตรวจ + วันที่
     */
    public boolean saveInspection(QualityInspectionModel qi) {
        String sql = """
            INSERT INTO quality_inspection
              (plantation_id, quality_id, defect_details, date, inspector_name, inspection_date)
            VALUES (?,?,?,?,?,?)
            """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1,    qiMC.getPlantationId(qi));
            ps.setString(2, qiMC.getQualityId(qi));
            ps.setString(3, qiMC.getDefectDetails(qi));
            ps.setString(4, qiMC.getDate(qi));
            ps.setString(5, qiMC.getInspectorName(qi));
            ps.setString(6, qiMC.getInspectionDate(qi));
            ps.executeUpdate();
            successMessage = "บันทึกผลตรวจสอบสำเร็จ";
            return true;
        } catch (SQLException e) {
            errorMessage = "บันทึกไม่สำเร็จ: " + e.getMessage();
            return false;
        }
    }

    // ── Accessors ─────────────────────────────────────────────────

    public TaskModelController              task()       { return taskMC;       }
    public ReservationLineModelController   rl()         { return rlMC;         }
    public QualityInspectionModelController inspection() { return qiMC;         }
    public PlantationModelController        plantation() { return plantationMC; }

    public String getErrorMessage()   { return errorMessage; }
    public String getSuccessMessage() { return successMessage; }
    public void clearMessages()       { errorMessage = ""; successMessage = ""; }
}
