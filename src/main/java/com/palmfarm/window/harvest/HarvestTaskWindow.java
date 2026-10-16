package com.palmfarm.window.harvest;
import com.palmfarm.util.WindowUtil;
import javafx.stage.Stage;
public class HarvestTaskWindow {
    private Stage stage;
    public HarvestTaskWindow() {
        stage = WindowUtil.createStage("/com/palmfarm/view/harvest/HarvestTaskView.fxml","งานเก็บเกี่ยว / ขนส่งปาล์ม");
    }
    public void show() { if (stage != null) stage.show(); }
    public Stage getStage() { return stage; }
}
