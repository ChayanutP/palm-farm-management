package com.palmfarm.controller;

import com.palmfarm.controller.model.BillModelController;
import com.palmfarm.model.BillModel;
import com.palmfarm.model.EmployeeModel;
import com.palmfarm.util.DBConnection;
import com.palmfarm.util.SessionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountingController {

    private final BillModelController billMC = new BillModelController();

    private String errorMessage   = "";
    private String successMessage = "";

    // Helper: ดึง national_id ของพนักงานบัญชีที่กำลังล็อกอินอยู่
    private String getCurrentUserNationalId(Connection con) {
    EmployeeModel user = SessionManager.getCurrentUser();
    if (user == null) return null;

    return user.national_id; // หรือชื่อเมธอด getter ใน EmployeeModel เช่น user.getNational_id()
}

    // ============================================================
    // UC16: จัดเก็บบิล
    // ============================================================

    public List<BillModel> getUnstoredBills() {
        List<BillModel> list = new ArrayList<>();
        String sql = "SELECT * FROM bills WHERE is_stored = FALSE ORDER BY uploaded_at DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(billMC.mapFromResultSet(rs));
        } catch (SQLException e) {
            errorMessage = "ดึงบิลไม่สำเร็จ: " + e.getMessage();
        }
        return list;
    }

    public boolean storeBill(int billId) {
        String sql = "UPDATE bills SET is_stored = TRUE, stored_by = ? WHERE bill_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String storedBy = getCurrentUserNationalId(con);
            ps.setString(1, storedBy);
            ps.setInt(2, billId);
            ps.executeUpdate();
            successMessage = "จัดเก็บบิลสำเร็จ";
            return true;
        } catch (SQLException e) {
            errorMessage = "จัดเก็บบิลไม่สำเร็จ: " + e.getMessage();
            return false;
        }
    }

    public boolean uploadBillImage(String imagePath) {
        BillModel b = billMC.createEmpty();
        billMC.setBillImagePath(b, imagePath);

        String sql = "INSERT INTO bills (bill_image_path, is_stored, stored_by, uploaded_at) VALUES (?, FALSE, NULL, NOW())";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, billMC.getBillImagePath(b));
            ps.executeUpdate();
            successMessage = "อัปโหลดรูปบิลสำเร็จ";
            return true;
        } catch (SQLException e) {
            errorMessage = "อัปโหลดไม่สำเร็จ: " + e.getMessage();
            return false;
        }
    }

    // ============================================================
    // UC17: บันทึกข้อมูลบิล
    // ============================================================

    public List<BillModel> getStoredBills() {
        List<BillModel> list = new ArrayList<>();
        String sql = "SELECT * FROM bills WHERE is_stored = TRUE ORDER BY uploaded_at DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(billMC.mapFromResultSet(rs));
        } catch (SQLException e) {
            errorMessage = "ดึงบิลไม่สำเร็จ: " + e.getMessage();
        }
        return list;
    }

    public boolean saveBillData(BillModel bill) {
        // ตรวจสอบผ่าน BillModelController
        String validationError = billMC.validate(bill);
        if (validationError != null) {
            errorMessage = validationError;
            return false;
        }
        // คำนวณยอดรวมผ่าน BillModelController
        billMC.computeAndSetTotalAmount(bill);

        String sql = """
            UPDATE bills
            SET bill_number=?, sale_date=?, buyer_name=?,
                palm_quantity=?, price_per_unit=?, total_amount=?,
                stored_by=COALESCE(stored_by, ?)
            WHERE bill_id=?
            """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String storedBy = getCurrentUserNationalId(con);
            ps.setString(1, billMC.getBillNumber(bill));
            ps.setString(2, billMC.getSaleDate(bill));
            ps.setString(3, billMC.getBuyerName(bill));
            ps.setDouble(4, billMC.getPalmQuantity(bill));
            ps.setDouble(5, billMC.getPricePerUnit(bill));
            ps.setDouble(6, billMC.getTotalAmount(bill));
            ps.setString(7, storedBy);
            ps.setInt(8,    billMC.getBillId(bill));
            ps.executeUpdate();
            successMessage = "บันทึกข้อมูลบิลสำเร็จ";
            return true;
        } catch (SQLException e) {
            errorMessage = "บันทึกไม่สำเร็จ: " + e.getMessage();
            return false;
        }
    }

    // ============================================================
    // Exposed Model Controller Accessor
    // ============================================================

    public BillModelController bill() { return billMC; }

    // ============================================================
    // Messages
    // ============================================================

    public String getErrorMessage()   { return errorMessage; }
    public String getSuccessMessage() { return successMessage; }
    public void clearMessages()       { errorMessage = ""; successMessage = ""; }
}