package com.palmfarm.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

/** Helper สำหรับสร้าง Stage จาก FXML — ใช้ร่วมกันทุก Window */
public class WindowUtil {

    public static Stage createStage(String fxmlPath, String title,
                                    double width, double height) {
        try {
            FXMLLoader loader = new FXMLLoader(
                WindowUtil.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root, width, height);
            String css = WindowUtil.class.getResource(
                "/com/palmfarm/view/style.css").toExternalForm();
            scene.getStylesheets().add(css);

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.setMinWidth(width * 0.8);
            stage.setMinHeight(height * 0.8);
            stage.centerOnScreen();
            return stage;
        } catch (IOException e) {
            System.err.println("WindowUtil error [" + fxmlPath + "]: " + e.getMessage());
            return null;
        }
    }

    public static Stage createStage(String fxmlPath, String title) {
        return createStage(fxmlPath, title, 1000, 650);
    }
}
