package com.palmfarm.controller;

import com.palmfarm.controller.model.EmployeeModelController;
import com.palmfarm.model.EmployeeModel;
import com.palmfarm.util.DBConnection;
import com.palmfarm.util.SessionManager;
import com.palmfarm.window.*;

import java.sql.*;

public class LoginController {

    private final EmployeeModelController empMC = new EmployeeModelController();
    private String errorMessage = "";

    public boolean authenticate(String username, String password) {
        String sql = "SELECT national_id, username, first_name, last_name, department, position, phone " +
                     "FROM employee WHERE username = ? AND password = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                EmployeeModel emp = empMC.mapFromResultSet(rs);
                SessionManager.setCurrentUser(emp);
                return true;
            } else {
                errorMessage = "ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง";
                return false;
            }
        } catch (SQLException e) {
            errorMessage = "ไม่สามารถเชื่อมต่อฐานข้อมูล: " + e.getMessage();
            return false;
        }
    }

    public void openDepartmentWindow() {
        String dept = empMC.getDepartment(SessionManager.getCurrentUser());
        switch (dept) {
            case "RESOURCE" -> new ResourceWindow().show();
            case "PALM_CARE" -> new PalmCareWindow().show();
            case "QUALITY" -> new QualityWindow().show();
            case "ACCOUNTING" -> new AccountingWindow().show();
            case "HARVEST" -> new HarvestWindow().show();
            case "CUTTING" -> new CuttingWindow().show();
            default           -> errorMessage = "ไม่พบฝ่ายที่กำหนด: " + dept;
        }
    }

    public String getErrorMessage()       { return errorMessage; }
    public EmployeeModelController empMC(){ return empMC; }
}
