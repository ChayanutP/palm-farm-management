package com.palmfarm.controller.model;

import com.palmfarm.model.WorkOrderModel;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Controller สำหรับ WorkOrderModel
 * ทำหน้าที่: getter, setter, mapper จาก ResultSet
 */
public class WorkOrderModelController {

    // 
    // GETTERS
    // 

    public int getOrderId(WorkOrderModel w) {
        return w.order_id;
    }

    public String getDescription(WorkOrderModel w) {
        return w.description;
    }

    public String getAssignedDate(WorkOrderModel w) {
        return w.assigned_date;
    }

    public String getStatus(WorkOrderModel w) {
        return w.status;
    }

    public int getAssignedEmployeeId(WorkOrderModel w) {
        return w.assigned_employee_id;
    }

    public int getPlantationId(WorkOrderModel w) {
        return w.plantation_id;
    }

    public String getTaskType(WorkOrderModel w) {
        return w.task_type;
    }

    /** ตรวจว่างานเสร็จสิ้นแล้ว */
    public boolean isCompleted(WorkOrderModel w) {
        return "completed".equals(w.status);
    }

    // 
    // SETTERS
    // 

    public void setOrderId(WorkOrderModel w, int v) {
        w.order_id = v;
    }

    public void setDescription(WorkOrderModel w, String v) {
        w.description = v;
    }

    public void setAssignedDate(WorkOrderModel w, String v) {
        w.assigned_date = v;
    }

    public void setStatus(WorkOrderModel w, String v) {
        w.status = v;
    }

    public void setAssignedEmployeeId(WorkOrderModel w, int v) {
        w.assigned_employee_id = v;
    }

    public void setPlantationId(WorkOrderModel w, int v) {
        w.plantation_id = v;
    }

    public void setTaskType(WorkOrderModel w, String v) {
        w.task_type = v;
    }

    // 
    // FACTORY / MAPPER
    // 

    public WorkOrderModel createEmpty() {
        WorkOrderModel w = new WorkOrderModel();
        w.status        = "pending";
        w.assigned_date = java.time.LocalDate.now().toString();
        return w;
    }

    /** Map ข้อมูลจาก ResultSet → WorkOrderModel */
    public WorkOrderModel mapFromResultSet(ResultSet rs) throws SQLException {
        WorkOrderModel w = new WorkOrderModel();
        w.order_id             = rs.getInt("order_id");
        w.description          = rs.getString("description");
        w.assigned_date        = rs.getString("assigned_date");
        w.status               = rs.getString("status");
        w.assigned_employee_id = rs.getInt("assigned_employee_id");
        w.plantation_id        = rs.getInt("plantation_id");
        w.task_type            = rs.getString("task_type");
        return w;
    }
}
