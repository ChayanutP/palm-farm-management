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

    public String getUsedStatus(ReservationLineModel rl) {
        return rl.used_status;
    }

    public String getReturnStatus(ReservationLineModel rl) {
        return rl.return_status;
    }

    /** ตรวจว่าถูกใช้งานแล้ว */
    public boolean isUsed(ReservationLineModel rl) {
        return "used".equals(rl.used_status) || "returned".equals(rl.used_status);
    }

    /** ตรวจว่าคืนแล้ว */
    public boolean isReturned(ReservationLineModel rl) {
        return "returned".equals(rl.return_status);
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

    public void setUsedStatus(ReservationLineModel rl, String v) {
        rl.used_status = v;
    }

    public void setReturnStatus(ReservationLineModel rl, String v) {
        rl.return_status = v;
    }

    // 
    // FACTORY / MAPPER
    // 

    /** สร้าง ReservationLineModel เปล่าพร้อม default status */
    public ReservationLineModel createEmpty() {
        ReservationLineModel rl = new ReservationLineModel();
        rl.used_status   = "pending";
        rl.return_status = "not_returned";
        return rl;
    }

    /**
     * Map ข้อมูลจาก ResultSet → ReservationLineModel
     * ใช้กับ query ที่ JOIN resource เพื่อให้ได้ resource_name, resource_type ด้วย
     */
    public ReservationLineModel mapFromResultSet(ResultSet rs) throws SQLException {
        ReservationLineModel rl = new ReservationLineModel();
        rl.reserved_id   = rs.getInt("reserved_id");
        rl.task_id       = rs.getInt("task_id");
        rl.resource_id   = rs.getInt("resource_id");
        rl.date          = rs.getString("date");
        rl.used_status   = rs.getString("used_status");
        rl.return_status = rs.getString("return_status");
        // อาจมีหรือไม่มีก็ได้ (ขึ้นอยู่กับว่า query JOIN หรือเปล่า)
        try { rl.resource_name = rs.getString("resource_name"); } catch (SQLException ignored) {}
        try { rl.resource_type = rs.getString("resource_type"); } catch (SQLException ignored) {}
        return rl;
    }
}
