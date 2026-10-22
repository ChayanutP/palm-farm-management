package com.palmfarm.model;

public class ResourceModel {
    // Superclass: resource
    public int resource_id;
    public String resource_type;   // VEHICLE / EQUIPMENT
    public String status;          // available / reserved / in_use / maintenance

    // Subclass: vehicle
    public String license_plate;
    public String vehicle_size;
    public int seat_count;

    // Subclass: equipment
    public String equip_id;
    public String equipment_name;

    // Derived fields (สำหรับแสดงผลบน TableView เดิมโดยไม่ต้องแก้ FXML)
    public String resource_name;
    public String description;
}