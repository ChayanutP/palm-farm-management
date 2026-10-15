package com.palmfarm.window.accounting;
import com.palmfarm.util.WindowUtil;
import javafx.stage.Stage;
public class RecordBillWindow {
    private Stage stage;
    public RecordBillWindow() {
        stage = WindowUtil.createStage("/com/palmfarm/view/accounting/RecordBillView.fxml","บันทึกข้อมูลบิล");
    }
    public void show() { if (stage != null) stage.show(); }
    public Stage getStage() { return stage; }
}
