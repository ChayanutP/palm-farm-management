package com.palmfarm.window.resource;

import com.palmfarm.util.WindowUtil;
import javafx.stage.Stage;

public class DeallocateTaskWindow {

    private Stage stage;

    public DeallocateTaskWindow() {
        stage = WindowUtil.createStage(
            "/com/palmfarm/view/resource/DeallocateTaskView.fxml",
            "ยกเลิกการจัดสรรทรัพยากร");
    }

    public void show()           { if (stage != null) stage.show(); }
    public Stage getStage()      { return stage; }
}
