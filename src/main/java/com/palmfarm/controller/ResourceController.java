package com.palmfarm.controller;

import com.palmfarm.controller.model.*;
import com.palmfarm.model.*;
import com.palmfarm.util.DBConnection;

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

    //  PLANTATION 
    public List<PlantationModel> getAllPlantations() {
        List<PlantationModel> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM palm_plantation ORDER BY name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(plantationMC.mapFromResultSet(rs));
        } catch (SQLException e) { errorMessage = "ดึงข้อมูลสวนไม่สำเร็จ: " + e.getMessage(); }
        return list;
    }

    //  TASKS 
    private static final String TASK_SELECT =
        "SELECT t.*, p.name AS plantation_name " +
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

    /** task ที่ยังไม่เสร็จทุกตัว — ใช้ในหน้า Scheduling */
    public List<TaskModel> getIncompleteTasks() {
        List<TaskModel> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 TASK_SELECT + "WHERE t.is_complete = FALSE ORDER BY t.deadline ASC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(taskMC.mapFromResultSet(rs));
        } catch (SQLException e) {
            errorMessage = "ดึงข้อมูล Task ไม่สำเร็จ: " + e.getMessage();
            System.err.println("[getIncompleteTasks ERROR] " + e.getMessage());
        }
        return list;
    }

    /** task ล่าสุด 1 รายการต่อกลุ่ม (dept+plantation) — สำหรับหน้า CreateTask */
    public List<TaskModel> getLatestTaskPerGroup() {
        List<TaskModel> list = new ArrayList<>();
        String sql = TASK_SELECT +
            "INNER JOIN (" +
            " SELECT department, plantation_id, MAX(deadline) AS max_deadline" +
            " FROM task GROUP BY department, plantation_id" +
            ") latest" +
            " ON  t.department    = latest.department" +
            " AND t.plantation_id = latest.plantation_id" +
            " AND t.deadline      = latest.max_deadline " +
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

    /** task ที่ยังไม่ส่ง (is_ordered = false) */
    public List<TaskModel> getUnorderedTasks() {
        List<TaskModel> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 TASK_SELECT + "WHERE t.is_ordered = FALSE ORDER BY t.work_ordered_date ASC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(taskMC.mapFromResultSet(rs));
        } catch (SQLException e) { errorMessage = "ดึงข้อมูล Task ไม่สำเร็จ: " + e.getMessage(); }
        return list;
    }

    /** is_complete=FALSE, is_allocated=TRUE, is_ordered=FALSE — สำหรับหน้า Deallocate */
    public List<TaskModel> getAllocatedUnorderedTasks() {
        List<TaskModel> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 TASK_SELECT +
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

    /** สร้าง Task — ตรวจก่อนว่าแผนก+สวนยังมี task ที่ยังไม่เสร็จ */
    public boolean createTask(TaskModel t) {
        String checkSql = "SELECT COUNT(*) FROM task WHERE department=? AND plantation_id=? AND is_complete=FALSE";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement chk = con.prepareStatement(checkSql)) {
            chk.setString(1, taskMC.getDepartment(t));
            chk.setInt(2, taskMC.getPlantationId(t));
            ResultSet rs = chk.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                errorMessage = "ยังมีงานที่ยังไม่เสร็จอยู่ในแผนกนี้และสวนนี้ กรุณาเสร็จสิ้นงานเดิมก่อน";
                return false;
            }
        } catch (SQLException e) { errorMessage = "ตรวจสอบข้อมูลไม่สำเร็จ: " + e.getMessage(); return false; }

        String sql = "INSERT INTO task (description,department,is_complete,is_allocated,is_ordered," +
                     "requisition_status,work_ordered_date,deadline,plantation_id) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, taskMC.getDescription(t));
            ps.setString(2, taskMC.getDepartment(t));
            ps.setBoolean(3, taskMC.isComplete(t));
            ps.setBoolean(4, taskMC.isAllocated(t));
            ps.setBoolean(5, taskMC.isOrdered(t));
            ps.setBoolean(6, taskMC.getRequisitionStatus(t));
            ps.setString(7, taskMC.getWorkOrderedDate(t));
            ps.setString(8, taskMC.getDeadline(t));
            ps.setInt(9, taskMC.getPlantationId(t));
            ps.executeUpdate();
            successMessage = "สร้าง Task สำเร็จ";
            return true;
        } catch (SQLException e) { errorMessage = "สร้าง Task ไม่สำเร็จ: " + e.getMessage(); return false; }
    }

    public boolean updateTaskComplete(int taskId, boolean complete) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE task SET is_complete=? WHERE task_id=?")) {
            ps.setBoolean(1, complete); ps.setInt(2, taskId); ps.executeUpdate();
            successMessage = "อัปเดตสถานะ Task สำเร็จ"; return true;
        } catch (SQLException e) { errorMessage = "อัปเดต Task ไม่สำเร็จ: " + e.getMessage(); return false; }
    }

    /** เลื่อน deadline */
    public boolean updateDeadline(int taskId, String newDeadline) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "UPDATE task SET deadline=? WHERE task_id=?")) {
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

    /** ส่งงาน: is_ordered = true */
    public boolean sendTask(int taskId) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE task SET is_ordered=TRUE WHERE task_id=?")) {
            ps.setInt(1, taskId);
            int rows = ps.executeUpdate();
            if (rows > 0) { successMessage = "ส่งงานสำเร็จ"; return true; }
            else { errorMessage = "ไม่พบ Task ที่ระบุ"; return false; }
        } catch (SQLException e) { errorMessage = "ส่งงานไม่สำเร็จ: " + e.getMessage(); return false; }
    }

    //  DEALLOCATE 
    public boolean deallocateTask(int taskId) {
        String sqlGetRes  = "SELECT resource_id FROM reservation_line WHERE task_id=?";
        String sqlDelRl   = "DELETE FROM reservation_line WHERE task_id=?";
        String sqlUpdRes  = "UPDATE resource SET status='available' WHERE resource_id=?";
        String sqlUpdTask = "UPDATE task SET is_allocated=FALSE WHERE task_id=?";
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                List<Integer> rids = new ArrayList<>();
                try (PreparedStatement ps = con.prepareStatement(sqlGetRes)) {
                    ps.setInt(1, taskId); ResultSet rs = ps.executeQuery();
                    while (rs.next()) rids.add(rs.getInt("resource_id"));
                }
                try (PreparedStatement ps = con.prepareStatement(sqlDelRl)) {
                    ps.setInt(1, taskId); ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(sqlUpdRes)) {
                    for (int rid : rids) { ps.setInt(1, rid); ps.addBatch(); }
                    ps.executeBatch();
                }
                try (PreparedStatement ps = con.prepareStatement(sqlUpdTask)) {
                    ps.setInt(1, taskId); ps.executeUpdate();
                }
                con.commit();
                successMessage = "ยกเลิกการจัดสรรสำเร็จ คืนทรัพยากร " + rids.size() + " รายการ";
                return true;
            } catch (SQLException e) { con.rollback(); throw e; }
            finally { con.setAutoCommit(true); }
        } catch (SQLException e) { errorMessage = "ยกเลิกการจัดสรรไม่สำเร็จ: " + e.getMessage(); return false; }
    }

    //  RESOURCES 
    public List<ResourceModel> getAllResources() {
        List<ResourceModel> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM resource ORDER BY resource_id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(resourceMC.mapFromResultSet(rs));
        } catch (SQLException e) { errorMessage = "ดึงข้อมูล Resource ไม่สำเร็จ: " + e.getMessage(); }
        return list;
    }

    public List<ResourceModel> getAvailableResources() {
        List<ResourceModel> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "SELECT * FROM resource WHERE status='available' ORDER BY resource_type,resource_name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(resourceMC.mapFromResultSet(rs));
        } catch (SQLException e) { errorMessage = "ดึง Resource ว่างไม่สำเร็จ: " + e.getMessage(); }
        return list;
    }

    public boolean addResource(ResourceModel r) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "INSERT INTO resource (resource_name,resource_type,status,description) VALUES (?,?,?,?)")) {
            ps.setString(1, resourceMC.getResourceName(r)); ps.setString(2, resourceMC.getResourceType(r));
            ps.setString(3, resourceMC.getStatus(r));       ps.setString(4, resourceMC.getDescription(r));
            ps.executeUpdate(); successMessage = "เพิ่ม Resource สำเร็จ"; return true;
        } catch (SQLException e) { errorMessage = "เพิ่ม Resource ไม่สำเร็จ: " + e.getMessage(); return false; }
    }

    //  RESERVATION LINES 
    public List<ReservationLineModel> getReservationsByTask(int taskId) {
        List<ReservationLineModel> list = new ArrayList<>();
        String sql = "SELECT rl.*, r.resource_name, r.resource_type " +
                     "FROM reservation_line rl " +
                     "JOIN resource r ON rl.resource_id = r.resource_id " +
                     "WHERE rl.task_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, taskId); ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(rlMC.mapFromResultSet(rs));
        } catch (SQLException e) { errorMessage = "ดึง Reservation ไม่สำเร็จ: " + e.getMessage(); }
        return list;
    }

    public boolean assignResourcesToTask(int taskId, List<Integer> resourceIds) {
        if (resourceIds.isEmpty()) { errorMessage = "กรุณาเลือกทรัพยากรอย่างน้อย 1 รายการ"; return false; }
        String dateStr = java.time.LocalDate.now().toString();
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement dp = con.prepareStatement("SELECT deadline FROM task WHERE task_id=?");
            dp.setInt(1, taskId); ResultSet dr = dp.executeQuery();
            if (dr.next() && dr.getString("deadline") != null) dateStr = dr.getString("deadline");
        } catch (SQLException ignored) {}
        String sqlIns = "INSERT INTO reservation_line (task_id,resource_id,date,used_status,return_status) VALUES (?,?,?,'pending','not_returned')";
        String sqlRes = "UPDATE resource SET status='reserved' WHERE resource_id=?";
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ins = con.prepareStatement(sqlIns);
                 PreparedStatement upd = con.prepareStatement(sqlRes)) {
                for (int rid : resourceIds) {
                    ins.setInt(1, taskId); ins.setInt(2, rid); ins.setString(3, dateStr); ins.addBatch();
                    upd.setInt(1, rid); upd.addBatch();
                }
                ins.executeBatch(); upd.executeBatch(); con.commit();
                successMessage = "จัดสรรทรัพยากร " + resourceIds.size() + " รายการสำเร็จ"; return true;
            } catch (SQLException e) { con.rollback(); throw e; }
            finally { con.setAutoCommit(true); }
        } catch (SQLException e) { errorMessage = "จัดสรรทรัพยากรไม่สำเร็จ: " + e.getMessage(); return false; }
    }

    //  EMPLOYEES 
    public List<EmployeeModel> getAllEmployees() {
        List<EmployeeModel> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM employee ORDER BY department,employee_id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(empMC.mapFromResultSet(rs));
        } catch (SQLException e) { errorMessage = "ดึงข้อมูลพนักงานไม่สำเร็จ: " + e.getMessage(); }
        return list;
    }

    public boolean addEmployee(EmployeeModel emp) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "INSERT INTO employee (username,password,first_name,last_name,department,position,phone) VALUES (?,?,?,?,?,?,?)")) {
            ps.setString(1,empMC.getUsername(emp)); ps.setString(2,empMC.getPassword(emp));
            ps.setString(3,empMC.getFirstName(emp)); ps.setString(4,empMC.getLastName(emp));
            ps.setString(5,empMC.getDepartment(emp)); ps.setString(6,empMC.getPosition(emp));
            ps.setString(7,empMC.getPhone(emp));
            ps.executeUpdate(); successMessage = "เพิ่มพนักงานสำเร็จ"; return true;
        } catch (SQLException e) { errorMessage = "เพิ่มพนักงานไม่สำเร็จ: " + e.getMessage(); return false; }
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
}
