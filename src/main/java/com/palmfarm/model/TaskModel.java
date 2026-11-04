package com.palmfarm.model;

public class TaskModel {
    public int     task_id;
    public String  description;
    public String  department;
    public boolean is_complete;    // false = กำลังดำเนินการ, true = เสร็จสิ้น
    public boolean is_allocated;   // false = รอจัดสรร,      true = จัดสรรแล้ว
    public boolean is_ordered;     // false = ยังไม่ได้ส่ง,  true = ส่งงานแล้ว
    public boolean requisition_status;
    public String  work_ordered_date;
    public String  deadline;
    public int     plantation_id;
    public String  assigned_to;     // national_id ของผู้รับผิดชอบ (NULL = ยังไม่มีผู้รับคำสั่ง)
    // join field — ไม่มีใน DB column ใช้แสดงผลอย่างเดียว
    public String  plantation_name;
    public String  assignee_name;   // join field — ชื่อ-สกุลผู้รับผิดชอบ ใช้แสดงผลอย่างเดียว
}
