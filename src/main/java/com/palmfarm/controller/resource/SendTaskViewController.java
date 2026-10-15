package com.palmfarm.controller.resource;

import com.palmfarm.controller.ResourceController;
import com.palmfarm.model.TaskModel;
import com.palmfarm.util.SessionManager;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller สำหรับหน้าส่งใบสั่งงาน
 *
 * Sequence:
 * 1. initialize()  → requestUnorderedTask() → โหลด task ที่ is_ordered = FALSE
 * 2. เลือกแถว      → trigger() → getTask(selectedTask) → แสดงรายละเอียด
 * 3. กดส่งงาน      → save(isOrderedStatus=true) → saveOrderedStatus() → refreshAndDisplay()
 */
public class SendTaskViewController implements Initializable {

    //  Header 

    //  ตาราง Task ที่ยังไม่ส่ง 
    @FXML private TableView<TaskModel>            tableTask;
    @FXML private TableColumn<TaskModel, Integer> colTaskId;
    @FXML private TableColumn<TaskModel, String>  colTaskDesc;
    @FXML private TableColumn<TaskModel, String>  colTaskDept;
    @FXML private TableColumn<TaskModel, String>  colTaskOrdered;
    @FXML private TableColumn<TaskModel, String>  colTaskDeadline;
    @FXML private TableColumn<TaskModel, String>  colTaskDate;

    //  Panel รายละเอียด task ที่เลือก 
    @FXML private Label lblDetailId;
    @FXML private Label lblDetailDesc;
    @FXML private Label lblDetailDept;
    @FXML private Label lblDetailDeadline;
    @FXML private Label lblDetailStatus;
    @FXML private Label lblDetailOrdered;

    //  ปุ่มและข้อความ 
    @FXML private Button btnSend;
    @FXML private Label  lblMsg;

    private final ResourceController ctrl = new ResourceController();
    private TaskModel selectedTask = null;

    // 
    // Step 1: initialize → requestUnorderedTask()
    // 
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        setupTable();
        requestUnorderedTask();   // ตาม sequence diagram
        btnSend.setDisable(true); // ยังไม่ได้เลือก task
    }

    // 
    // requestUnorderedTask() → โหลด task ที่ is_ordered = FALSE
    // 
    private void requestUnorderedTask() {
        List<TaskModel> tasks = ctrl.getUnorderedTasks();
        tableTask.setItems(FXCollections.observableArrayList(tasks));
        clearDetail();
    }

    // 
    // Setup TableView columns
    // 
    private void setupTable() {
        colTaskId.setCellValueFactory(d ->
            new SimpleIntegerProperty(ctrl.task().getTaskId(d.getValue())).asObject());
        colTaskDesc.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.task().getDescription(d.getValue())));
        colTaskDept.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.task().getDepartment(d.getValue())));
        colTaskOrdered.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(ctrl.task().getOrderedStatusDisplay(d.getValue())));
        colTaskDeadline.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.task().getDeadline(d.getValue())));
        colTaskDate.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.task().getWorkOrderedDate(d.getValue())));

        // Step 2: เลือกแถว → trigger() → getTask(selectedTask) → display(task)
        tableTask.getSelectionModel().selectedItemProperty()
            .addListener((obs, old, nw) -> {
                if (nw != null) trigger(nw);
            });
    }

    // 
    // Step 2: trigger() → getTask → display(task)
    // 
    private void trigger(TaskModel task) {
        // getTask(selectedTask) return task
        selectedTask = task;
        displayTask(selectedTask);
        btnSend.setDisable(false);
        lblMsg.setText("");
    }

    // display(task) — แสดงรายละเอียดใน panel
    private void displayTask(TaskModel t) {
        lblDetailId.setText("Task ID: " + ctrl.task().getTaskId(t));
        lblDetailDesc.setText(ctrl.task().getDescription(t));
        lblDetailDept.setText("ฝ่าย: " + ctrl.task().getDepartment(t));
        lblDetailDeadline.setText("กำหนดส่ง: " + ctrl.task().getDeadline(t));
        lblDetailStatus.setText("สถานะงาน: " + ctrl.task().getCompleteStatusDisplay(t));
        lblDetailOrdered.setText("สถานะการส่ง: " + ctrl.task().getOrderedStatusDisplay(t));
    }

    private void clearDetail() {
        lblDetailId.setText("Task ID: —");
        lblDetailDesc.setText("—");
        lblDetailDept.setText("ฝ่าย: —");
        lblDetailDeadline.setText("กำหนดส่ง: —");
        lblDetailStatus.setText("สถานะงาน: —");
        lblDetailOrdered.setText("สถานะการส่ง: —");
        btnSend.setDisable(true);
        selectedTask = null;
    }

    // 
    // Step 3: กดส่งงาน → save(isOrderedStatus=true)
    //         → saveOrderedStatus() → refreshAndDisplay()
    // 
    @FXML
    public void handleSendTask(ActionEvent e) {
        if (selectedTask == null) {
            lblMsg.setText("กรุณาเลือกงานที่ต้องการส่ง");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "ยืนยันการส่งงาน Task #" + ctrl.task().getTaskId(selectedTask)
                + "\n\"" + ctrl.task().getDescription(selectedTask) + "\"?",
            ButtonType.YES, ButtonType.NO);
        confirm.setTitle("ยืนยันส่งงาน");
        confirm.setHeaderText(null);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                // saveOrderedStatus() → setStatus(isOrderedStatus=true) → UPDATE
                boolean ok = ctrl.sendTask(ctrl.task().getTaskId(selectedTask));

                lblMsg.setText(ok
                    ? ctrl.getSuccessMessage()
                    : ctrl.getErrorMessage());
                ctrl.clearMessages();

                // refreshAndDisplay()
                refreshAndDisplay();
            }
        });
    }

    // refreshAndDisplay() — โหลดใหม่และล้าง panel
    private void refreshAndDisplay() {
        requestUnorderedTask();
        clearDetail();
    }

    //  Refresh button 
    @FXML
    public void handleRefresh(ActionEvent e) {
        refreshAndDisplay();
        lblMsg.setText("");
    }
}
