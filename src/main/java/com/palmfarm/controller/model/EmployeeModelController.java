package com.palmfarm.controller.model;

import com.palmfarm.model.EmployeeModel;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EmployeeModelController {

    // ── GETTERS ──────────────────────────────────────────────────

    public String getNationalId(EmployeeModel e)  { return e.national_id; }
    public String getUsername(EmployeeModel e)     { return e.username; }
    public String getPassword(EmployeeModel e)     { return e.password; }
    public String getFirstName(EmployeeModel e)    { return e.first_name; }
    public String getLastName(EmployeeModel e)     { return e.last_name; }
    public String getFullName(EmployeeModel e)     { return e.first_name + " " + e.last_name; }
    public String getDepartment(EmployeeModel e)   { return e.department; }
    public String getPosition(EmployeeModel e)     { return e.position; }
    public String getPhone(EmployeeModel e)        { return e.phone; }

    // ── SETTERS ──────────────────────────────────────────────────

    public void setNationalId(EmployeeModel e, String v)  { e.national_id = v; }
    public void setUsername(EmployeeModel e, String v)    { e.username = v; }
    public void setPassword(EmployeeModel e, String v)    { e.password = v; }
    public void setFirstName(EmployeeModel e, String v)   { e.first_name = v; }
    public void setLastName(EmployeeModel e, String v)    { e.last_name = v; }
    public void setDepartment(EmployeeModel e, String v)  { e.department = v; }
    public void setPosition(EmployeeModel e, String v)    { e.position = v; }
    public void setPhone(EmployeeModel e, String v)       { e.phone = v; }

    // ── FACTORY / MAPPER ─────────────────────────────────────────

    public EmployeeModel createEmpty() { return new EmployeeModel(); }

    public EmployeeModel mapFromResultSet(ResultSet rs) throws SQLException {
        EmployeeModel e = new EmployeeModel();
        e.national_id = rs.getString("national_id");
        e.first_name  = rs.getString("first_name");
        e.last_name   = rs.getString("last_name");
        e.department  = rs.getString("department");
        e.position    = rs.getString("position");
        e.phone       = rs.getString("phone");
        return e;
    }
}
