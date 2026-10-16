package com.palmfarm.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * FXML Controller สำหรับ LoginView.fxml
 * ใช้ LoginController เป็น business logic
 */
public class LoginViewController implements Initializable {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;
    @FXML private Button btnLogin;

    private final LoginController loginController = new LoginController();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblError.setText("");
        // Enter key on password field triggers login
        txtPassword.setOnAction(e -> handleLogin(null));
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("กรุณากรอกชื่อผู้ใช้และรหัสผ่าน");
            return;
        }

        btnLogin.setDisable(true);
        lblError.setText("กำลังตรวจสอบ...");

        boolean ok = loginController.authenticate(username, password);

        if (ok) {
            // Close login window
            Stage loginStage = (Stage) btnLogin.getScene().getWindow();
            loginStage.close();
            // Open department window
            loginController.openDepartmentWindow();
        } else {
            lblError.setText(loginController.getErrorMessage());
            txtPassword.clear();
            btnLogin.setDisable(false);
        }
    }
}
