package com.palmfarm.model;

public class ReservationLineModel {
    public int reserved_id;
    public int task_id;
    public int resource_id;
    public String resource_name;
    public String resource_type;
    public String date;
    public String status;      // pending / used / returned
}
