package com.palmfarm.controller.model;

import com.palmfarm.model.ResourceModel;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Controller สำหรับ ResourceModel
 * ทำหน้าที่: getter, setter, mapper จาก ResultSet
 */
public class ResourceModelController {

    // 
    // GETTERS
    // 

    public int getResourceId(ResourceModel r) {
        return r.resource_id;
    }

    public String getResourceName(ResourceModel r) {
        return r.resource_name;
    }

    public String getResourceType(ResourceModel r) {
        return r.resource_type;
    }

    public String getStatus(ResourceModel r) {
        return r.status;
    }

    public String getDescription(ResourceModel r) {
        return r.description;
    }

    /** ตรวจว่าพร้อมใช้งาน */
    public boolean isAvailable(ResourceModel r) {
        return "available".equals(r.status);
    }

    /** ชื่อแสดงผล: ชื่อ [ประเภท] */
    public String getDisplayName(ResourceModel r) {
        return r.resource_name + " [" + r.resource_type + "]";
    }

    // 
    // SETTERS
    // 

    public void setResourceId(ResourceModel r, int v) {
        r.resource_id = v;
    }

    public void setResourceName(ResourceModel r, String v) {
        r.resource_name = v;
    }

    public void setResourceType(ResourceModel r, String v) {
        r.resource_type = v;
    }

    public void setStatus(ResourceModel r, String v) {
        r.status = v;
    }

    public void setDescription(ResourceModel r, String v) {
        r.description = v;
    }

    // 
    // FACTORY / MAPPER
    // 

    /** สร้าง ResourceModel เปล่าพร้อม default status */
    public ResourceModel createEmpty() {
        ResourceModel r = new ResourceModel();
        r.status = "available";
        return r;
    }

    /** Map ข้อมูลจาก ResultSet → ResourceModel */
    public ResourceModel mapFromResultSet(ResultSet rs) throws SQLException {
        ResourceModel r = new ResourceModel();
        r.resource_id   = rs.getInt("resource_id");
        r.resource_name = rs.getString("resource_name");
        r.resource_type = rs.getString("resource_type");
        r.status        = rs.getString("status");
        r.description   = rs.getString("description");
        return r;
    }
}
