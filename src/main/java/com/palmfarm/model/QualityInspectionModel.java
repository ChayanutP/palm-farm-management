package com.palmfarm.model;

public class QualityInspectionModel {
    public int inspection_id;
    public int plantation_id;
    public int task_id;
    public String quality_id;
    public String defect_details;
    public String date;
    public String inspector_name;
    public String inspection_date;
    public double amount;
    // join field
    public String plantation_name;
}
