package com.palmfarm.window.accounting;
import com.palmfarm.util.WindowUtil;
import javafx.stage.Stage;
public class StoreBillWindow {
    private Stage stage;
    public StoreBillWindow() {
        stage = WindowUtil.createStage("/com/palmfarm/view/accounting/StoreBillView.fxml","จัดเก็บบิล");
    }
    public void show() { if (stage != null) stage.show(); }
    public Stage getStage() { return stage; }
}
