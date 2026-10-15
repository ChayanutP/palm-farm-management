package com.palmfarm.controller.model;

import com.palmfarm.model.EmployeeModel;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Controller สำหรับ EmployeeModel
 * ทำหน้าที่: getter, setter, mapper จาก ResultSet
 * Model จะทำแค่เก็บค่า — การเข้าถึงข้อมูลทั้งหมดต้องผ่าน Controller นี้
 */
public class EmployeeModelController {

    // 
    // GETTERS
    // 

    public int getEmployeeId(EmployeeModel e) {
        return e.employee_id;
    }

    public String getUsername(EmployeeModel e) {
        return e.username;
    }

    public String getPassword(EmployeeModel e) {
        return e.password;
    }

    public String getFirstName(EmployeeModel e) {
        return e.first_name;
    }

    public String getLastName(EmployeeModel e) {
        return e.last_name;
    }

    /** ชื่อเต็ม first_name + last_name */
    public String getFullName(EmployeeModel e) {
        return e.first_name + " " + e.last_name;
    }

    public String getDepartment(EmployeeModel e) {
        return e.department;
    }

    public String getPosition(EmployeeModel e) {
        return e.position;
    }

    public String getPhone(EmployeeModel e) {
        return e.phone;
    }

    // 
    // SETTERS
    // 

    public void setEmployeeId(EmployeeModel e, int v) {
        e.employee_id = v;
    }

    public void setUsername(EmployeeModel e, String v) {
        e.username = v;
    }

    public void setPassword(EmployeeModel e, String v) {
        e.password = v;
    }

    public void setFirstName(EmployeeModel e, String v) {
        e.first_name = v;
    }

    public void setLastName(EmployeeModel e, String v) {
        e.last_name = v;
    }

    public void setDepartment(EmployeeModel e, String v) {
        e.department = v;
    }

    public void setPosition(EmployeeModel e, String v) {
        e.position = v;
    }

    public void setPhone(EmployeeModel e, String v) {
        e.phone = v;
    }

    // 
    // FACTORY / MAPPER
    // 

    /** สร้าง EmployeeModel เปล่า */
    public EmployeeModel createEmpty() {
        return new EmployeeModel();
    }

    /** Map ข้อมูลจาก ResultSet → EmployeeModel */
    public EmployeeModel mapFromResultSet(ResultSet rs) throws SQLException {
        EmployeeModel e = new EmployeeModel();
        e.employee_id = rs.getInt("employee_id");
        e.username    = rs.getString("username");
        e.password    = rs.getString("password");
        e.first_name  = rs.getString("first_name");
        e.last_name   = rs.getString("last_name");
        e.department  = rs.getString("department");
        e.position    = rs.getString("position");
        e.phone       = rs.getString("phone");
        return e;
    }
}
