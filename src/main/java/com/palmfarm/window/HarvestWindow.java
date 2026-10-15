package com.palmfarm.window;

import com.palmfarm.controller.model.EmployeeModelController;
import com.palmfarm.util.SessionManager;
import com.palmfarm.window.harvest.ReturnResourceWindow;
import com.palmfarm.window.harvest.UseResourceWindow;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class HarvestWindow {
    private Stage stage;

    public HarvestWindow() { this.stage = new Stage(); }

    public void show() {
        var empMC = new EmployeeModelController();
        String name = empMC.getFullName(SessionManager.getCurrentUser());

        Label lblTitle = new Label("ฝ่ายเก็บเกี่ยว / ขนส่งปาล์ม");
        lblTitle.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:#4CAF50;");
        Label lblUser = new Label("ยินดีต้อนรับ: " + name);
        lblUser.setStyle("-fx-text-fill:#aaccaa;-fx-font-size:13px;");

        Button btnUse    = menuButton("รับคำสั่ง / เบิกอุปกรณ์",
            () -> new UseResourceWindow().show());
        Button btnReturn = menuButton("คืนทรัพยากร",
            () -> new ReturnResourceWindow().show());
        Button btnLogout = menuButton("ออกจากระบบ", () -> {
            SessionManager.clearSession(); stage.close();
            new LoginWindow(new Stage()).show();
        });
        btnLogout.setStyle(btnLogout.getStyle() + "-fx-background-color:#c0392b;");

        VBox root = new VBox(18, lblTitle, lblUser, btnUse, btnReturn, btnLogout);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color:#1a2a1a;");

        Scene scene = new Scene(root, 400, 320);
        scene.getStylesheets().add(
            getClass().getResource("/com/palmfarm/view/style.css").toExternalForm());
        stage.setTitle("ฝ่ายเก็บเกี่ยว / ขนส่ง");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }

    private Button menuButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setMaxWidth(300);
        btn.setStyle("-fx-background-color:#243224;-fx-text-fill:#ccddcc;" +
                     "-fx-font-size:14px;-fx-cursor:hand;-fx-padding:12 20;" +
                     "-fx-background-radius:8;-fx-alignment:CENTER_LEFT;");
        btn.setOnAction(e -> action.run());
        return btn;
    }

    public Stage getStage() { return stage; }
}
