package com.palmfarm.model;

public class ReservationLineModel {
    public int reserved_id;
    public int task_id;
    public int resource_id;
    public String resource_name;
    public String resource_type;
    public String date;
    public String status;           // pending / used / returned (สถานะของ reservation_line)

    // Extra fields สำหรับ getActiveResourceAllocations() — populated เฉพาะเมื่อ JOIN เพิ่ม
    public String resource_status;  // available / reserved / in_use (สถานะของ resource จริงๆ)
    public String task_description;
    public String task_deadline;
    public String plantation_name;
}
 