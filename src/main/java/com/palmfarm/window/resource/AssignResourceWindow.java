package com.palmfarm.window.resource;

import com.palmfarm.util.WindowUtil;
import javafx.stage.Stage;

public class AssignResourceWindow {
    private Stage stage;
    public AssignResourceWindow() {
        stage = WindowUtil.createStage(
            "/com/palmfarm/view/resource/AssignResourceView.fxml",
            "จัดสรรทรัพยากร");
    }
    public void show() { if (stage != null) stage.show(); }
    public Stage getStage() { return stage; }
}
