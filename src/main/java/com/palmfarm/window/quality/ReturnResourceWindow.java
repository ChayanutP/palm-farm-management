package com.palmfarm.window.quality;
import com.palmfarm.util.WindowUtil;
import javafx.stage.Stage;
public class ReturnResourceWindow {
    private Stage stage;
    public ReturnResourceWindow() {
        stage = WindowUtil.createStage("/com/palmfarm/view/quality/ReturnResourceView.fxml", "คืนทรัพยากร");
    }
    public void show() { if (stage != null) stage.show(); }
    public Stage getStage() { return stage; }
}
