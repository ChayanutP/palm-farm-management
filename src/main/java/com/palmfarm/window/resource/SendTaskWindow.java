package com.palmfarm.window.resource;

import com.palmfarm.util.WindowUtil;
import javafx.stage.Stage;

public class SendTaskWindow {

    private Stage stage;

    public SendTaskWindow() {
        stage = WindowUtil.createStage(
            "/com/palmfarm/view/resource/SendTaskView.fxml",
            "ส่งใบสั่งงาน");
    }

    public void show() { if (stage != null) stage.show(); }
    public Stage getStage() { return stage; }
}
