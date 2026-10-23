package com.palmfarm.controller.resource;

import com.palmfarm.controller.ResourceController;
import com.palmfarm.model.TaskModel;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * Controller สำหรับหน้าเลื่อน Deadline
 *
 * Sequence:
 * 1. initialize() → reqLatestOrdersByDeptAndGarden() → โหลด task ที่ is_complete=FALSE
 * 2. กดปุ่ม "เลื่อน" ในแถว → open(taskAndSelected) → getDeadline() → display(deadline)
 *    → reqSchedule() พร้อมแสดงปฏิทิน
 * 3. เลือกวันและกดยืนยัน → setDeadline(data) → UPDATE + notifyUpdate() → refreshAndDisplay()
 */
public class SchedulingViewController implements Initializable {

    @FXML private TableView<TaskModel>            tableTask;
    @FXML private TableColumn<TaskModel, Integer> colId;
    @FXML private TableColumn<TaskModel, String>  colDesc;
    @FXML private TableColumn<TaskModel, String>  colPlantation;
    @FXML private TableColumn<TaskModel, String>  colDept;
    @FXML private TableColumn<TaskModel, String>  colComplete;
    @FXML private TableColumn<TaskModel, String>  colOrdered;
    @FXML private TableColumn<TaskModel, String>  colDeadline;
    @FXML private TableColumn<TaskModel, Void>    colAction;   // ปุ่มเลื่อน

    @FXML private Label lblMsg;
    @FXML private Button btnRefresh;

    private final ResourceController ctrl = new ResourceController();

    // 
    // Step 1: initialize → reqLatestOrdersByDeptAndGarden()
    // 
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        reqLatestOrdersByDeptAndGarden();
    }

    // reqLatestOrdersByDeptAndGarden() — โหลด task is_complete=FALSE
    private void reqLatestOrdersByDeptAndGarden() {
        var tasks = ctrl.getIncompleteTasks();
        tableTask.setItems(FXCollections.observableArrayList(tasks));
        if (!ctrl.getErrorMessage().isEmpty()) {
            showError(ctrl.getErrorMessage());
            ctrl.clearMessages();
        } else {
            lblMsg.setText("");
        }
    }

    // 
    // Setup columns รวมปุ่ม "เลื่อน" ในแถวขวาสุด
    // 
    private void setupColumns() {
        colId.setCellValueFactory(d ->
            new SimpleIntegerProperty(ctrl.task().getTaskId(d.getValue())).asObject());
        colDesc.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.task().getDescription(d.getValue())));
        colPlantation.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.task().getPlantationName(d.getValue())));
        colDept.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.task().getDepartmentDisplay(d.getValue())));
        colComplete.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.task().getCompleteStatusDisplay(d.getValue())));
        colOrdered.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.task().getOrderedStatusDisplay(d.getValue())));
        colDeadline.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.task().getDeadline(d.getValue())));

        //  ปุ่ม "เลื่อน" ในแต่ละแถว 
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("เลื่อน");
            {
                btn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; " +
                             "-fx-cursor: hand; -fx-font-size: 11px; -fx-padding: 3 8;");
                btn.setOnAction(e -> {
                    TaskModel task = getTableView().getItems().get(getIndex());
                    // Step 2: trigger() → open(taskAndSelected)
                    openSchedulingDialog(task);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    // 
    // Step 2: open(taskAndSelected) → getDeadline() → display
    //         → reqSchedule() — เปิด popup ปฏิทิน
    // 
    private void openSchedulingDialog(TaskModel task) {
        int taskId = ctrl.task().getTaskId(task);

        // getDeadline(selectedTask) return deadline
        String currentDeadline = ctrl.task().getDeadline(task);
        LocalDate initDate = currentDeadline != null && !currentDeadline.isBlank()
            ? LocalDate.parse(currentDeadline)
            : LocalDate.now();

        //  สร้าง popup 
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("เลื่อน Deadline — Task #" + taskId);
        popup.setResizable(false);

        // display(deadline) — แสดงข้อมูล task + ปฏิทิน
        Label lblTask = new Label(
            "Task #" + taskId + " — " + ctrl.task().getDescription(task));
        lblTask.setStyle("-fx-text-fill: #ddeedd; -fx-font-size: 13px; -fx-font-weight: bold;");
        lblTask.setWrapText(true);

        Label lblCurrent = new Label("Deadline ปัจจุบัน: " + (currentDeadline != null ? currentDeadline : "—"));
        lblCurrent.setStyle("-fx-text-fill: #aaccaa; -fx-font-size: 12px;");

        Label lblNew = new Label("เลือก Deadline ใหม่:");
        lblNew.setStyle("-fx-text-fill: #ccddcc; -fx-font-size: 12px;");

        // reqSchedule() — ปฏิทินให้เลือกวัน
        LocalDate minDeadline = LocalDate.now().plusDays(1);
        // ถ้า deadline ปัจจุบันยังอยู่ในอนาคตมากกว่า min ให้ใช้ค่าเดิม มิฉะนั้นใช้ min
        LocalDate pickerInit = initDate.isAfter(minDeadline) ? initDate : minDeadline;
        DatePicker datePicker = new DatePicker(pickerInit);
        datePicker.setStyle("-fx-background-color: #2d3e2d;");
        datePicker.setPrefWidth(220);
        // ห้ามเลือกวันที่น้อยกว่าวันนี้ + 1
        // สามารถเลื่อน deadline เข้ามาได้
        datePicker.setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(minDeadline));
            }
        });

        Label lblPopupMsg = new Label("");
        lblPopupMsg.setStyle("-fx-font-size: 11px;");
        lblPopupMsg.setWrapText(true);

        Button btnConfirm = new Button("ยืนยัน");
        btnConfirm.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                            "-fx-font-size: 13px; -fx-font-weight: bold; " +
                            "-fx-cursor: hand; -fx-padding: 7 20; -fx-background-radius: 5;");

        Button btnCancel = new Button("ยกเลิก");
        btnCancel.setStyle("-fx-background-color: #607D8B; -fx-text-fill: white; " +
                           "-fx-cursor: hand; -fx-padding: 7 16; -fx-background-radius: 5;");
        btnCancel.setOnAction(e -> popup.close());

        // Step 3: กดยืนยัน → setDeadline(data) → UPDATE → notifyUpdate() → refreshAndDisplay()
        btnConfirm.setOnAction(e -> {
            LocalDate selected = datePicker.getValue();
            if (selected == null) {
                lblPopupMsg.setStyle("-fx-text-fill: #ff8888; -fx-font-size: 11px;");
                lblPopupMsg.setText("กรุณาเลือกวันที่");
                return;
            }
            if (selected.isBefore(LocalDate.now().plusDays(10))) {
                lblPopupMsg.setStyle("-fx-text-fill: #ff8888; -fx-font-size: 11px;");
                lblPopupMsg.setText("Deadline ต้องห่างจากวันนี้อย่างน้อย 10 วัน");
                return;
            }

            // setDeadline(data) → UPDATE task SET Deadline = '%newDeadline'
            boolean ok = ctrl.updateDeadline(taskId, selected.toString());
            if (ok) {
                // notifyUpdate() → refreshAndDisplay()
                popup.close();
                reqLatestOrdersByDeptAndGarden();   // refresh table
                showSuccess("เลื่อน Deadline Task #" + taskId + " เป็น " + selected + " สำเร็จ");
            } else {
                lblPopupMsg.setStyle("-fx-text-fill: #ff8888; -fx-font-size: 11px;");
                lblPopupMsg.setText(ctrl.getErrorMessage());
                ctrl.clearMessages();
            }
        });

        HBox btnRow = new HBox(10, btnConfirm, btnCancel);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        VBox content = new VBox(12, lblTask, lblCurrent, new Separator(), lblNew, datePicker, lblPopupMsg, btnRow);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: #1a2a1a;");
        content.setPrefWidth(340);

        String css = getClass().getResource("/com/palmfarm/view/style.css").toExternalForm();
        Scene scene = new Scene(content);
        scene.getStylesheets().add(css);
        popup.setScene(scene);
        popup.showAndWait();
    }

    @FXML
    public void handleRefresh() {
        reqLatestOrdersByDeptAndGarden();
    }

    private void showSuccess(String msg) {
        lblMsg.setStyle("-fx-text-fill: #88ff88; -fx-font-size: 12px;");
        lblMsg.setText(msg);
    }

    private void showError(String msg) {
        lblMsg.setStyle("-fx-text-fill: #ff8888; -fx-font-size: 12px;");
        lblMsg.setText(msg);
    }
}
