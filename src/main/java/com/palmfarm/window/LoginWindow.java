package com.palmfarm.window;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginWindow {

    private Stage stage;

    public LoginWindow(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/palmfarm/view/LoginView.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 500, 400);
            scene.getStylesheets().add(
                getClass().getResource("/com/palmfarm/view/style.css").toExternalForm());

            stage.setTitle("ระบบจัดการสวนปาล์ม — เข้าสู่ระบบ");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            System.err.println("LoginWindow Error: " + e.getMessage());
        }
    }

    public Stage getStage() { return stage; }
}
