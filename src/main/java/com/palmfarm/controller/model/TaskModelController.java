package com.palmfarm.controller.model;

import com.palmfarm.model.TaskModel;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Controller สำหรับ TaskModel
 * - is_complete / is_allocated / is_ordered เก็บเป็น boolean ใน Model
 * - display helpers แปลง boolean → ข้อความไทยสำหรับแสดงผลใน View
 * - DB column ยังเป็น BOOLEAN (0/1) ตรงกัน
 */
public class TaskModelController {

    // 
    // GETTERS
    // 

    public int    getTaskId(TaskModel t)          { return t.task_id; }
    public String getDescription(TaskModel t)     { return t.description; }
    public String getDepartment(TaskModel t)      { return t.department; }
    public boolean isComplete(TaskModel t)        { return t.is_complete; }
    public boolean isAllocated(TaskModel t)       { return t.is_allocated; }
    public boolean isOrdered(TaskModel t)         { return t.is_ordered; }
    public boolean getRequisitionStatus(TaskModel t) { return t.requisition_status; }
    public String getWorkOrderedDate(TaskModel t) { return t.work_ordered_date; }
    public String getDeadline(TaskModel t)        { return t.deadline; }
    public int    getPlantationId(TaskModel t)    { return t.plantation_id; }
    public String getPlantationName(TaskModel t)  {
        return t.plantation_name != null ? t.plantation_name : "";
    }
    public String getAssignedTo(TaskModel t)      { return t.assigned_to; }

    /** ชื่อผู้รับผิดชอบ: แสดงเฉพาะเมื่อมีการรับคำสั่งแล้ว (requisition_status = TRUE) */
    public String getResponsibleDisplay(TaskModel t) {
        if (t.requisition_status && t.assignee_name != null && !t.assignee_name.isBlank())
            return t.assignee_name;
        return "ยังไม่มีผู้รับคำสั่ง";
    }

    // 
    // SETTERS
    // 

    public void setTaskId(TaskModel t, int v)            { t.task_id = v; }
    public void setDescription(TaskModel t, String v)    { t.description = v; }
    public void setDepartment(TaskModel t, String v)     { t.department = v; }
    public void setIsComplete(TaskModel t, boolean v)    { t.is_complete = v; }
    public void setIsAllocated(TaskModel t, boolean v)   { t.is_allocated = v; }
    public void setIsOrdered(TaskModel t, boolean v)     { t.is_ordered = v; }
    public void setRequisitionStatus(TaskModel t, boolean v) { t.requisition_status = v; }
    public void setWorkOrderedDate(TaskModel t, String v){ t.work_ordered_date = v; }
    public void setDeadline(TaskModel t, String v)       { t.deadline = v; }
    public void setPlantationId(TaskModel t, int v)      { t.plantation_id = v; }
    public void setPlantationName(TaskModel t, String v) { t.plantation_name = v; }

    // 
    // DISPLAY HELPERS — boolean → ข้อความไทย (ใช้ใน View เท่านั้น)
    // 

    /** แปลงชื่อฝ่ายจาก DB → ภาษาไทยสำหรับแสดงผล */
    public String getDepartmentDisplay(TaskModel t) {
        return getDepartmentDisplay(t.department);
    }

    /** แปลงรหัสฝ่าย (เช่น HARVEST) → ชื่อไทย ใช้ได้กับค่าที่ไม่ได้มาจาก TaskModel เช่น ฝ่ายของพนักงาน */
    public String getDepartmentDisplay(String dept) {
        if (dept == null) return "";
        return switch (dept.toUpperCase()) {
            case "RESOURCE"   -> "ฝ่ายจัดสรรทรัพยากร";
            case "PALM_CARE"  -> "ฝ่ายดูแล";
            case "ACCOUNTING" -> "ฝ่ายบัญชี";
            case "HARVEST"    -> "ฝ่ายเก็บเกี่ยว/ขนส่ง";
            case "CUTTING"    -> "ฝ่ายตัด/ปลูก";
            default           -> dept;
        };
    }

    /** is_complete: false → "กำลังดำเนินการ" / true → "เสร็จสิ้น" */
    public String getCompleteStatusDisplay(TaskModel t) {
        return t.is_complete ? "เสร็จสิ้น" : "กำลังดำเนินการ";
    }

    /** is_ordered: false → " ยังไม่ได้ส่ง" / true → "ส่งงานแล้ว" */
    public String getOrderedStatusDisplay(TaskModel t) {
        return t.is_ordered ? "มอบหมายงานแล้ว" : "ยังไม่ได้มอบหมาย";
    }

    /** is_allocated: false → "⏳ รอจัดสรร" / true → "จัดสรรแล้ว" */
    public String getAllocatedStatusDisplay(TaskModel t) {
        return t.is_allocated ? "จัดสรรแล้ว" : "รอจัดสรร";
    }

    // 
    // FACTORY / MAPPER
    // 

    /** สร้าง TaskModel เปล่าพร้อม default: ยังไม่เสร็จ / ยังไม่จัดสรร / ยังไม่ส่ง */
    public TaskModel createEmpty() {
        TaskModel t = new TaskModel();
        t.is_complete        = false;
        t.is_allocated       = false;
        t.is_ordered         = false;
        t.requisition_status = false;
        t.work_ordered_date  = java.time.LocalDate.now().toString();
        return t;
    }

    /** Map ResultSet → TaskModel (DB column เป็น BOOLEAN 0/1) */
    public TaskModel mapFromResultSet(ResultSet rs) throws SQLException {
        TaskModel t = new TaskModel();
        t.task_id            = rs.getInt("task_id");
        t.description        = rs.getString("description");
        t.department         = rs.getString("department");
        t.is_complete        = rs.getBoolean("is_complete");
        t.is_allocated       = rs.getBoolean("is_allocated");
        t.is_ordered         = rs.getBoolean("is_ordered");
        t.requisition_status = rs.getBoolean("requisition_status");
        t.work_ordered_date  = rs.getString("work_ordered_date");
        t.deadline           = rs.getString("deadline");
        t.plantation_id      = rs.getInt("plantation_id");
        try { t.plantation_name = rs.getString("plantation_name"); }
        catch (SQLException ignored) {}
        try { t.assigned_to = rs.getString("assigned_to"); }
        catch (SQLException ignored) {}
        try { t.assignee_name = rs.getString("assignee_name"); }
        catch (SQLException ignored) {}
        return t;
    }
}
