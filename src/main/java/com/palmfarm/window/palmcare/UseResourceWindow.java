package com.palmfarm.window.palmcare;
import com.palmfarm.util.WindowUtil;
import javafx.stage.Stage;
public class UseResourceWindow {
    private Stage stage;
    public UseResourceWindow() {
        stage = WindowUtil.createStage("/com/palmfarm/view/palmcare/UseResourceView.fxml","บันทึกการใช้ทรัพยากร");
    }
    public void show() { if (stage != null) stage.show(); }
    public Stage getStage() { return stage; }
}
