package com.palmfarm.model;

public class WorkOrderModel {
    public int order_id;
    public String description;
    public String assigned_date;
    public String status;       // pending / in_progress / completed
    public int assigned_employee_id;
    public int plantation_id;
    public String task_type;
}
