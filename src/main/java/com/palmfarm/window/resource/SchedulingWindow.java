package com.palmfarm.window.resource;

import com.palmfarm.util.WindowUtil;
import javafx.stage.Stage;

public class SchedulingWindow {

    private Stage stage;

    public SchedulingWindow() {
        stage = WindowUtil.createStage(
            "/com/palmfarm/view/resource/SchedulingView.fxml",
            "เลื่อน Deadline");
    }

    public void show()      { if (stage != null) stage.show(); }
    public Stage getStage() { return stage; }
}
