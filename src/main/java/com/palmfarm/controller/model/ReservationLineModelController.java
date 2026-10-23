package com.palmfarm.controller.model;

import com.palmfarm.model.ReservationLineModel;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Controller สำหรับ ReservationLineModel
 * ทำหน้าที่: getter, setter, mapper จาก ResultSet
 */
public class ReservationLineModelController {

    // 
    // GETTERS
    // 

    public int getReservedId(ReservationLineModel rl) {
        return rl.reserved_id;
    }

    public int getTaskId(ReservationLineModel rl) {
        return rl.task_id;
    }

    public int getResourceId(ReservationLineModel rl) {
        return rl.resource_id;
    }

    public String getResourceName(ReservationLineModel rl) {
        return rl.resource_name;
    }

    public String getResourceType(ReservationLineModel rl) {
        return rl.resource_type;
    }

    public String getDate(ReservationLineModel rl) {
        return rl.date;
    }

    public String getStatus(ReservationLineModel rl) {
        return rl.status;
    }

    public String getReturnStatus(ReservationLineModel rl) {
    if (rl == null || rl.status == null) return "not_returned";
    return "returned".equalsIgnoreCase(rl.status) ? "returned" : "not_returned";
}

    /** ตรวจว่าอยู่ระหว่างรอดำเนินการ */
    public boolean isPending(ReservationLineModel rl) {
        return "pending".equals(rl.status);
    }

    /** ตรวจว่าถูกใช้งานแล้ว (รวมถึงสถานะที่ถูกคืนแล้ว) */
    public boolean isUsed(ReservationLineModel rl) {
        return "used".equals(rl.status) || "returned".equals(rl.status);
    }

    /** ตรวจว่าคืนแล้ว */
    public boolean isReturned(ReservationLineModel rl) {
        return "returned".equals(rl.status);
    }

    // 
    // SETTERS
    // 

    public void setReservedId(ReservationLineModel rl, int v) {
        rl.reserved_id = v;
    }

    public void setTaskId(ReservationLineModel rl, int v) {
        rl.task_id = v;
    }

    public void setResourceId(ReservationLineModel rl, int v) {
        rl.resource_id = v;
    }

    public void setResourceName(ReservationLineModel rl, String v) {
        rl.resource_name = v;
    }

    public void setResourceType(ReservationLineModel rl, String v) {
        rl.resource_type = v;
    }

    public void setDate(ReservationLineModel rl, String v) {
        rl.date = v;
    }

    public void setStatus(ReservationLineModel rl, String v) {
        rl.status = v;
    }

    // 
    // FACTORY / MAPPER
    // 

    /** สร้าง ReservationLineModel เปล่าพร้อม default status */
    public ReservationLineModel createEmpty() {
        ReservationLineModel rl = new ReservationLineModel();
        rl.status = "pending";
        return rl;
    }

    /**
     * Map ข้อมูลจาก ResultSet → ReservationLineModel
     * ใช้กับ query ที่ JOIN resource เพื่อให้ได้ resource_name, resource_type ด้วย
     */
    public ReservationLineModel mapFromResultSet(ResultSet rs) throws SQLException {
        ReservationLineModel rl = new ReservationLineModel();
        rl.reserved_id = rs.getInt("reserved_id");
        rl.task_id     = rs.getInt("task_id");
        rl.resource_id = rs.getInt("resource_id");
        rl.date        = rs.getString("date");
        rl.status      = rs.getString("status");

        // Optional columns จาก JOIN
        try { rl.resource_name = rs.getString("resource_name"); } catch (SQLException ignored) {}
        try { rl.resource_type = rs.getString("resource_type"); } catch (SQLException ignored) {}

        return rl;
    }
}