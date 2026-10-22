package com.palmfarm.controller.model;

import com.palmfarm.model.ResourceModel;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Controller สำหรับ ResourceModel (รองรับ Superclass Resource และ Subclass Vehicle / Equipment)
 * ทำหน้าที่: getter, setter, mapper จาก ResultSet
 */
public class ResourceModelController {

    // ─────────────────────────────────────────
    // GETTERS (Superclass & Derived)
    // ─────────────────────────────────────────

    public int getResourceId(ResourceModel r) {
        return r.resource_id;
    }

    public String getResourceName(ResourceModel r) {
        if (r.resource_name != null && !r.resource_name.isBlank()) {
            return r.resource_name;
        }
        // กรณีดึงจาก Subclass โดยตรง
        if ("VEHICLE".equalsIgnoreCase(r.resource_type)) {
            String size  = (r.vehicle_size != null && !r.vehicle_size.isBlank()) ? r.vehicle_size : "ยานพาหนะ";
            String plate = (r.license_plate != null && !r.license_plate.isBlank()) ? " ทะเบียน " + r.license_plate : "";
            return size + plate;
        } else {
            String eqId   = (r.equip_id != null && !r.equip_id.isBlank()) ? "[" + r.equip_id + "] " : "";
            String eqName = (r.equipment_name != null) ? r.equipment_name : "";
            return eqId + eqName;
        }
    }

    public String getResourceType(ResourceModel r) {
        return r.resource_type;
    }

    public String getStatus(ResourceModel r) {
        return r.status;
    }

    public String getDescription(ResourceModel r) {
        if (r.description != null && !r.description.isBlank()) {
            return r.description;
        }
        if ("VEHICLE".equalsIgnoreCase(r.resource_type)) {
            return "จำนวนที่นั่ง: " + r.seat_count + " ที่นั่ง";
        } else {
            return "รหัสอุปกรณ์: " + (r.equip_id != null ? r.equip_id : "-");
        }
    }

    // ─────────────────────────────────────────
    // GETTERS (Subclass: Vehicle & Equipment)
    // ─────────────────────────────────────────

    public String getLicensePlate(ResourceModel r) {
        return r.license_plate;
    }

    public String getVehicleSize(ResourceModel r) {
        return r.vehicle_size;
    }

    public int getSeatCount(ResourceModel r) {
        return r.seat_count;
    }

    public String getEquipId(ResourceModel r) {
        return r.equip_id;
    }

    public String getEquipmentName(ResourceModel r) {
        return r.equipment_name;
    }

    /** ตรวจว่าพร้อมใช้งาน */
    public boolean isAvailable(ResourceModel r) {
        return "available".equals(r.status);
    }

    /** ชื่อแสดงผล: ชื่อ [ประเภท] */
    public String getDisplayName(ResourceModel r) {
        return getResourceName(r) + " [" + r.resource_type + "]";
    }

    // ─────────────────────────────────────────
    // SETTERS
    // ─────────────────────────────────────────

    public void setResourceId(ResourceModel r, int v) {
        r.resource_id = v;
    }

    public void setResourceName(ResourceModel r, String v) {
        r.resource_name = v;
        // กระจายค่าลงฟิลด์ของ Subclass อัตโนมัติหากยังไม่ได้กำหนดแยก
        if ("VEHICLE".equalsIgnoreCase(r.resource_type)) {
            if (r.license_plate == null || r.license_plate.isBlank()) {
                r.license_plate = v;
            }
            if (r.vehicle_size == null || r.vehicle_size.isBlank()) {
                r.vehicle_size = "ยานพาหนะทั่วไป";
            }
        } else {
            if (r.equipment_name == null || r.equipment_name.isBlank()) {
                r.equipment_name = v;
            }
        }
    }

    public void setResourceType(ResourceModel r, String v) {
        r.resource_type = v;
        // หากเคยตั้ง resource_name ไว้ก่อนหน้า ให้ซิงค์เข้า Subclass ด้วย
        if (r.resource_name != null && !r.resource_name.isBlank()) {
            setResourceName(r, r.resource_name);
        }
    }

    public void setStatus(ResourceModel r, String v) {
        r.status = v;
    }

    public void setDescription(ResourceModel r, String v) {
        r.description = v;
    }

    public void setLicensePlate(ResourceModel r, String v) {
        r.license_plate = v;
    }

    public void setVehicleSize(ResourceModel r, String v) {
        r.vehicle_size = v;
    }

    public void setSeatCount(ResourceModel r, int v) {
        r.seat_count = v;
    }

    public void setEquipId(ResourceModel r, String v) {
        r.equip_id = v;
    }

    public void setEquipmentName(ResourceModel r, String v) {
        r.equipment_name = v;
    }

    // ─────────────────────────────────────────
    // FACTORY / MAPPER
    // ─────────────────────────────────────────

    /** สร้าง ResourceModel เปล่าพร้อม default status */
    public ResourceModel createEmpty() {
        ResourceModel r = new ResourceModel();
        r.status     = "available";
        r.seat_count = 2;
        return r;
    }

    /** Map ข้อมูลจาก ResultSet (ที่ LEFT JOIN vehicle และ equipment) → ResourceModel */
    public ResourceModel mapFromResultSet(ResultSet rs) throws SQLException {
        ResourceModel r = new ResourceModel();
        r.resource_id   = rs.getInt("resource_id");
        r.resource_type = rs.getString("resource_type");
        r.status        = rs.getString("status");

        // ดึงข้อมูลจากตารางลูก (Vehicle & Equipment)
        try { r.license_plate  = rs.getString("license_plate"); }  catch (SQLException ignored) {}
        try { r.vehicle_size   = rs.getString("vehicle_size"); }   catch (SQLException ignored) {}
        try { r.seat_count     = rs.getInt("seat_count"); }        catch (SQLException ignored) {}
        try { r.equip_id       = rs.getString("equip_id"); }       catch (SQLException ignored) {}
        try { r.equipment_name = rs.getString("equipment_name"); } catch (SQLException ignored) {}

        // สร้าง resource_name และ description สำหรับหน้า UI ที่เรียกใช้เมธอดเดิม
        if ("VEHICLE".equalsIgnoreCase(r.resource_type)) {
            String size  = (r.vehicle_size != null && !r.vehicle_size.isBlank()) ? r.vehicle_size : "ยานพาหนะ";
            String plate = (r.license_plate != null && !r.license_plate.isBlank()) ? " ทะเบียน " + r.license_plate : "";
            r.resource_name = size + plate;
            r.description   = "จำนวนที่นั่ง: " + r.seat_count + " ที่นั่ง";
        } else {
            String eqId   = (r.equip_id != null && !r.equip_id.isBlank()) ? "[" + r.equip_id + "] " : "";
            String eqName = (r.equipment_name != null) ? r.equipment_name : "";
            r.resource_name = eqId + eqName;
            r.description   = "รหัสอุปกรณ์: " + (r.equip_id != null ? r.equip_id : "-");
        }

        return r;
    }
}