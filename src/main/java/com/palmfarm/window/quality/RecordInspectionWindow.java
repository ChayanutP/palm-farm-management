package com.palmfarm.window.quality;
import com.palmfarm.util.WindowUtil;
import javafx.stage.Stage;
public class RecordInspectionWindow {
    private Stage stage;
    public RecordInspectionWindow() {
        stage = WindowUtil.createStage("/com/palmfarm/view/quality/RecordInspectionView.fxml","บันทึกผลตรวจสอบคุณภาพ");
    }
    public void show() { if (stage != null) stage.show(); }
    public Stage getStage() { return stage; }
}
