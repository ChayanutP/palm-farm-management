package com.palmfarm.window.harvest;
import com.palmfarm.util.WindowUtil;
import javafx.stage.Stage;
public class UseResourceWindow {
    private Stage stage;
    public UseResourceWindow() {
        stage = WindowUtil.createStage("/com/palmfarm/view/harvest/UseResourceView.fxml", "รับคำสั่ง / เบิกอุปกรณ์");
    }
    public void show() { if (stage != null) stage.show(); }
    public Stage getStage() { return stage; }
}
