package com.palmfarm.window.palmcare;
import com.palmfarm.util.WindowUtil;
import javafx.stage.Stage;
public class ReturnResourceWindow {
    private Stage stage;
    public ReturnResourceWindow() {
        stage = WindowUtil.createStage("/com/palmfarm/view/palmcare/ReturnResourceView.fxml","คืนทรัพยากร");
    }
    public void show() { if (stage != null) stage.show(); }
    public Stage getStage() { return stage; }
}
