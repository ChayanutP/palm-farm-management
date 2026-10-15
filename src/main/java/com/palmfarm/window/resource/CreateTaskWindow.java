package com.palmfarm.window.resource;

import com.palmfarm.util.WindowUtil;
import javafx.stage.Stage;

public class CreateTaskWindow {
    private Stage stage;
    public CreateTaskWindow() {
        stage = WindowUtil.createStage(
            "/com/palmfarm/view/resource/CreateTaskView.fxml",
            "สร้างใบสั่งงาน");
    }
    public void show() { if (stage != null) stage.show(); }
    public Stage getStage() { return stage; }
}
