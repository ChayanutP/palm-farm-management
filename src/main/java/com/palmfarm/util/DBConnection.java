package com.palmfarm.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL =
        "jdbc:mysql://localhost:3306/palm_farm?useSSL=false&serverTimezone=Asia/Bangkok&characterEncoding=UTF-8";
    private static final String USER     = "root";
    private static final String PASSWORD = "";

    static {
        try { Class.forName("com.mysql.cj.jdbc.Driver"); }
        catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver not found: " + e.getMessage());
        }
    }

    /**
     * เปิด Connection ใหม่ทุกครั้ง — ใช้ใน try-with-resources
     * แก้ปัญหา Singleton connection ที่ทำให้ autoCommit ค้าง
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
