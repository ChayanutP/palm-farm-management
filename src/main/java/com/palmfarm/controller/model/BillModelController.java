package com.palmfarm.controller.model;

import com.palmfarm.model.BillModel;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Controller สำหรับ BillModel
 * ทำหน้าที่: getter, setter, validation เบื้องต้น, mapper จาก ResultSet
 */
public class BillModelController {

    // 
    // GETTERS
    // 

    public int getBillId(BillModel b) {
        return b.bill_id;
    }

    public String getBillNumber(BillModel b) {
        return b.bill_number;
    }

    public String getBillImagePath(BillModel b) {
        return b.bill_image_path;
    }

    public boolean isStored(BillModel b) {
        return b.is_stored;
    }

    public String getSaleDate(BillModel b) {
        return b.sale_date;
    }

    public String getBuyerName(BillModel b) {
        return b.buyer_name;
    }

    public double getPalmQuantity(BillModel b) {
        return b.palm_quantity;
    }

    public double getPricePerUnit(BillModel b) {
        return b.price_per_unit;
    }

    public double getTotalAmount(BillModel b) {
        return b.total_amount;
    }

    public String getUploadedAt(BillModel b) {
        return b.uploaded_at;
    }

    public int getPlantationId(BillModel b) {
        return b.plantation_id;
    }

    /** คำนวณยอดรวมจาก quantity × price (ยังไม่ save ลง model) */
    public double calcTotal(BillModel b) {
        return b.palm_quantity * b.price_per_unit;
    }

    /** ข้อความแสดงยอดรวมแบบ formatted */
    public String getTotalFormatted(BillModel b) {
        return String.format("%.2f บาท", b.total_amount);
    }

    // 
    // SETTERS
    // 

    public void setBillId(BillModel b, int v) {
        b.bill_id = v;
    }

    public void setBillNumber(BillModel b, String v) {
        b.bill_number = v;
    }

    public void setBillImagePath(BillModel b, String v) {
        b.bill_image_path = v;
    }

    public void setIsStored(BillModel b, boolean v) {
        b.is_stored = v;
    }

    public void setSaleDate(BillModel b, String v) {
        b.sale_date = v;
    }

    public void setBuyerName(BillModel b, String v) {
        b.buyer_name = v;
    }

    public void setPalmQuantity(BillModel b, double v) {
        b.palm_quantity = v;
    }

    public void setPricePerUnit(BillModel b, double v) {
        b.price_per_unit = v;
    }

    /** Set total_amount โดยคำนวณจาก quantity × price อัตโนมัติ */
    public void computeAndSetTotalAmount(BillModel b) {
        b.total_amount = b.palm_quantity * b.price_per_unit;
    }

    public void setTotalAmount(BillModel b, double v) {
        b.total_amount = v;
    }

    public void setUploadedAt(BillModel b, String v) {
        b.uploaded_at = v;
    }

    public void setPlantationId(BillModel b, int v) {
        b.plantation_id = v;
    }

    // 
    // VALIDATION
    // 

    /**
     * ตรวจสอบความครบถ้วนของข้อมูลบิล (UC17)
     * @return null ถ้าผ่าน, หรือ error message ถ้าไม่ผ่าน
     */
    public String validate(BillModel b) {
        if (b.bill_number == null || b.bill_number.isBlank())
            return "กรุณากรอกเลขที่บิล";
        if (b.sale_date == null || b.sale_date.isBlank())
            return "กรุณากรอกวันที่ขาย";
        if (b.buyer_name == null || b.buyer_name.isBlank())
            return "กรุณากรอกชื่อผู้รับซื้อ";
        if (b.palm_quantity <= 0)
            return "ปริมาณปาล์มต้องมากกว่า 0";
        if (b.price_per_unit <= 0)
            return "ราคาต่อหน่วยต้องมากกว่า 0";
        return null;
    }

    // 
    // FACTORY / MAPPER
    // 

    public BillModel createEmpty() {
        BillModel b = new BillModel();
        b.is_stored     = false;
        b.palm_quantity = 0;
        b.price_per_unit= 0;
        b.total_amount  = 0;
        return b;
    }

    /** Map ข้อมูลจาก ResultSet → BillModel */
    public BillModel mapFromResultSet(ResultSet rs) throws SQLException {
        BillModel b = new BillModel();
        b.bill_id        = rs.getInt("bill_id");
        b.bill_number    = rs.getString("bill_number");
        b.bill_image_path= rs.getString("bill_image_path");
        b.is_stored      = rs.getBoolean("is_stored");
        b.sale_date      = rs.getString("sale_date");
        b.buyer_name     = rs.getString("buyer_name");
        b.palm_quantity  = rs.getDouble("palm_quantity");
        b.price_per_unit = rs.getDouble("price_per_unit");
        b.total_amount   = rs.getDouble("total_amount");
        b.uploaded_at    = rs.getString("uploaded_at");
        try { b.plantation_id = rs.getInt("plantation_id"); } catch (SQLException ignored) {}
        return b;
    }
}
