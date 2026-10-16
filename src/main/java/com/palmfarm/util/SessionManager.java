package com.palmfarm.util;

import com.palmfarm.model.EmployeeModel;

public class SessionManager {
    private static EmployeeModel currentUser = null;

    public static void setCurrentUser(EmployeeModel user) {
        currentUser = user;
    }

    public static EmployeeModel getCurrentUser() {
        return currentUser;
    }

    public static String getDepartment() {
        return currentUser != null ? currentUser.department : "";
    }

    public static void clearSession() {
        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}
