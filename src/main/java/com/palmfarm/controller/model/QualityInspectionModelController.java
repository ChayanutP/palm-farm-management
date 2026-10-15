package com.palmfarm.controller.model;

import com.palmfarm.model.QualityInspectionModel;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Controller สำหรับ QualityInspectionModel
 * ทำหน้าที่: getter, setter, mapper จาก ResultSet
 */
public class QualityInspectionModelController {

    // 
    // GETTERS
    // 

    public int getInspectionId(QualityInspectionModel qi) {
        return qi.inspection_id;
    }

    public int getPlantationId(QualityInspectionModel qi) {
        return qi.plantation_id;
    }

    /** ชื่อสวนจาก join field plantation_name */
    public String getPlantationName(QualityInspectionModel qi) {
        return qi.plantation_name != null ? qi.plantation_name : "";
    }

    public int getTaskId(QualityInspectionModel qi) {
        return qi.task_id;
    }

    public String getQualityId(QualityInspectionModel qi) {
        return qi.quality_id;
    }

    public String getDefectDetails(QualityInspectionModel qi) {
        return qi.defect_details;
    }

    public String getDate(QualityInspectionModel qi) {
        return qi.date;
    }

    public String getInspectorName(QualityInspectionModel qi) {
        return qi.inspector_name;
    }

    public String getInspectionDate(QualityInspectionModel qi) {
        return qi.inspection_date;
    }

    public double getAmount(QualityInspectionModel qi) {
        return qi.amount;
    }

    /** ข้อความแสดงผลปริมาณ + หน่วย */
    public String getAmountFormatted(QualityInspectionModel qi) {
        return String.format("%.2f กก.", qi.amount);
    }

    /** ตรวจว่าเกรดผ่านหรือไม่ (A,B,C = ผ่าน, D,REJECT = ไม่ผ่าน) */
    public boolean isPassed(QualityInspectionModel qi) {
        return qi.quality_id != null &&
               (qi.quality_id.equals("A") || qi.quality_id.equals("B") || qi.quality_id.equals("C"));
    }

    // 
    // SETTERS
    // 

    public void setInspectionId(QualityInspectionModel qi, int v) {
        qi.inspection_id = v;
    }

    public void setPlantationId(QualityInspectionModel qi, int v) {
        qi.plantation_id = v;
    }

    public void setTaskId(QualityInspectionModel qi, int v) {
        qi.task_id = v;
    }

    public void setQualityId(QualityInspectionModel qi, String v) {
        qi.quality_id = v;
    }

    public void setDefectDetails(QualityInspectionModel qi, String v) {
        qi.defect_details = v;
    }

    public void setDate(QualityInspectionModel qi, String v) {
        qi.date = v;
    }

    public void setInspectorName(QualityInspectionModel qi, String v) {
        qi.inspector_name = v;
    }

    public void setInspectionDate(QualityInspectionModel qi, String v) {
        qi.inspection_date = v;
    }

    public void setAmount(QualityInspectionModel qi, double v) {
        qi.amount = v;
    }

    // 
    // FACTORY / MAPPER
    // 

    public QualityInspectionModel createEmpty() {
        QualityInspectionModel qi = new QualityInspectionModel();
        qi.date = java.time.LocalDate.now().toString();
        return qi;
    }

    /** Map ข้อมูลจาก ResultSet → QualityInspectionModel */
    public QualityInspectionModel mapFromResultSet(ResultSet rs) throws SQLException {
        QualityInspectionModel qi = new QualityInspectionModel();
        qi.inspection_id   = rs.getInt("inspection_id");
        qi.plantation_id   = rs.getInt("plantation_id");
        qi.task_id         = rs.getInt("task_id");
        qi.quality_id      = rs.getString("quality_id");
        qi.defect_details  = rs.getString("defect_details");
        qi.date            = rs.getString("date");
        qi.inspector_name  = rs.getString("inspector_name");
        qi.inspection_date = rs.getString("inspection_date");
        qi.amount          = rs.getDouble("amount");
        try { qi.plantation_name = rs.getString("plantation_name"); } catch (java.sql.SQLException ignored) {}
        return qi;
    }
}
