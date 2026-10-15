package com.palmfarm.window.resource;

import com.palmfarm.util.WindowUtil;
import javafx.stage.Stage;

public class ManageResourceWindow {
    private Stage stage;
    public ManageResourceWindow() {
        stage = WindowUtil.createStage(
            "/com/palmfarm/view/resource/ManageResourceView.fxml",
            "จัดการทรัพยากร");
    }
    public void show() { if (stage != null) stage.show(); }
    public Stage getStage() { return stage; }
}
