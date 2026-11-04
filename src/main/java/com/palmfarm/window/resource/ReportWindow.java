package com.palmfarm.window.resource;

import com.palmfarm.util.WindowUtil;
import javafx.stage.Stage;

public class ReportWindow {

    private Stage stage;

    public ReportWindow() {
        stage = WindowUtil.createStage(
            "/com/palmfarm/view/resource/ReportView.fxml",
            "ออกรายงาน", 480, 660);
    }

    public void show() { if (stage != null) stage.show(); }
    public Stage getStage() { return stage; }
}
