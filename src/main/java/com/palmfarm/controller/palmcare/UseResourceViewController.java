package com.palmfarm.controller.palmcare;

import com.palmfarm.controller.PalmCareController;
import com.palmfarm.model.ReservationLineModel;
import com.palmfarm.model.TaskModel;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Duration;
import java.net.URL;
import java.util.ResourceBundle;

public class UseResourceViewController implements Initializable {

    @FXML private TableView<TaskModel>              tableTask;
    @FXML private TableColumn<TaskModel, Integer>   colTaskId;
    @FXML private TableColumn<TaskModel, String>    colTaskDesc;
    @FXML private TableColumn<TaskModel, String>    colTaskPlantation;
    @FXML private TableColumn<TaskModel, String>    colTaskStatus;
    @FXML private TableColumn<TaskModel, String>    colTaskOrdered;
    @FXML private TableColumn<TaskModel, String>    colTaskDeadline;

    @FXML private TableView<ReservationLineModel>           tableReservation;
    @FXML private TableColumn<ReservationLineModel, String> colRlName;
    @FXML private TableColumn<ReservationLineModel, String> colRlType;
    @FXML private TableColumn<ReservationLineModel, String> colRlUsed;

    @FXML private Button btnMarkUsed;
    @FXML private Label  lblMsg;

    private final PalmCareController ctrl = new PalmCareController();
    private TaskModel selectedTask;
    private Timeline autoRefresh;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadTasks();
        btnMarkUsed.setDisable(true);
        startAutoRefresh();
    }

    private void setupTable() {
        colTaskId.setCellValueFactory(d ->
            new SimpleIntegerProperty(ctrl.task().getTaskId(d.getValue())).asObject());
        colTaskDesc.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.task().getDescription(d.getValue())));
        colTaskPlantation.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.task().getPlantationName(d.getValue())));
        colTaskStatus.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.task().getCompleteStatusDisplay(d.getValue())));
        colTaskOrdered.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.task().getOrderedStatusDisplay(d.getValue())));
        colTaskDeadline.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.task().getDeadline(d.getValue())));
        colRlName.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.rl().getResourceName(d.getValue())));
        colRlType.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.rl().getResourceType(d.getValue())));
        colRlUsed.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.rl().getStatus(d.getValue())));

        tableTask.getSelectionModel().selectedItemProperty().addListener((obs, old, nw) -> {
            if (nw != null) {
                selectedTask = nw;
                tableReservation.setItems(FXCollections.observableArrayList(
                    ctrl.getReservationsForTask(ctrl.task().getTaskId(nw))));
                btnMarkUsed.setDisable(false);
                lblMsg.setText("");
            }
        });
    }

    private void loadTasks() {
        tableTask.setItems(FXCollections.observableArrayList(ctrl.getUncompletedTasksForCurrentDept()));
        tableReservation.getItems().clear();
        selectedTask = null;
        btnMarkUsed.setDisable(true);
    }

    /**
     * หลายคนในฝ่ายเดียวกันเปิดหน้านี้พร้อมกันได้ — โหลดรายการใหม่ทุก 10 วินาที
     * เพื่อให้งานที่ถูกรับไปแล้วหายจากรายการ และงานใหม่ที่เพิ่งส่งเข้ามาปรากฏ (หยุดอัตโนมัติเมื่อปิดหน้าต่าง)
     */
    private void startAutoRefresh() {
        autoRefresh = new Timeline(new KeyFrame(Duration.seconds(10), ev -> reloadKeepSelection()));
        autoRefresh.setCycleCount(Animation.INDEFINITE);
        tableTask.sceneProperty().addListener((o, oldScene, scene) -> {
            if (scene == null) return;
            scene.windowProperty().addListener((o2, oldWin, win) -> {
                if (win == null) return;
                win.showingProperty().addListener((o3, was, showing) -> {
                    if (showing) autoRefresh.play(); else autoRefresh.stop();
                });
                if (win.isShowing()) autoRefresh.play();
            });
        });
    }

    /** โหลดใหม่โดยพยายามคงแถวที่ผู้ใช้เลือกไว้ ถ้างานนั้นถูกคนอื่นรับไปแล้วจะแจ้งเตือน */
    private void reloadKeepSelection() {
        var fresh = ctrl.getUncompletedTasksForCurrentDept();
        if (!ctrl.getErrorMessage().isEmpty()) { ctrl.clearMessages(); return; }   // โหลดไม่สำเร็จ: คงรายการเดิมไว้
        int keepId = selectedTask == null ? -1 : ctrl.task().getTaskId(selectedTask);
        tableTask.setItems(FXCollections.observableArrayList(fresh));
        tableReservation.getItems().clear();
        selectedTask = null;
        btnMarkUsed.setDisable(true);
        if (keepId == -1) return;
        for (TaskModel t : fresh) {
            if (ctrl.task().getTaskId(t) == keepId) { tableTask.getSelectionModel().select(t); return; }
        }
        lblMsg.setStyle("-fx-text-fill:#ffcc66;");
        lblMsg.setText("งาน #" + keepId + " ถูกรับไปแล้วโดยผู้อื่น หรือไม่อยู่ในรายการแล้ว");
    }

    @FXML public void handleMarkUsed(ActionEvent e) {
        if (selectedTask == null) { lblMsg.setText("กรุณาเลือก Task"); return; }
        boolean ok = ctrl.markResourceAsUsed(ctrl.task().getTaskId(selectedTask));
        lblMsg.setStyle(ok ? "-fx-text-fill:#88ff88;" : "-fx-text-fill:#ff8888;");
        lblMsg.setText(ok ? ctrl.getSuccessMessage() : ctrl.getErrorMessage());
        ctrl.clearMessages();
        loadTasks();
    }

}
