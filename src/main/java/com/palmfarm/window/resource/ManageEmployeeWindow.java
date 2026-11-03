package com.palmfarm.window.resource;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ManageEmployeeWindow {
    private Stage stage;
    public ManageEmployeeWindow() { this.stage = new Stage(); }
    public void show() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/palmfarm/view/resource/ManageEmployeeView.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            String css = getClass().getResource("/com/palmfarm/view/style.css").toExternalForm();
            scene.getStylesheets().add(css);
            stage.setTitle("จัดการพนักงาน");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception ex) { ex.printStackTrace(); }
    }
    public Stage getStage() { return stage; }
}
