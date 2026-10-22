package com.palmfarm.controller.resource;

import com.palmfarm.controller.ResourceController;
import com.palmfarm.model.ReservationLineModel;
import com.palmfarm.model.TaskModel;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class DeallocateTaskViewController implements Initializable {

    // ── Task table ──
    @FXML private TableView<TaskModel>            tableTask;
    @FXML private TableColumn<TaskModel, Integer> colTaskId;
    @FXML private TableColumn<TaskModel, String>  colTaskDesc;
    @FXML private TableColumn<TaskModel, String>  colTaskPlantation;
    @FXML private TableColumn<TaskModel, String>  colTaskDept;
    @FXML private TableColumn<TaskModel, String>  colTaskDeadline;
    @FXML private TableColumn<TaskModel, String>  colTaskAllocated;

    // ── Reservation table ──
    @FXML private TableView<ReservationLineModel>            tableReservation;
    @FXML private TableColumn<ReservationLineModel, Integer> colRlId;
    @FXML private TableColumn<ReservationLineModel, String>  colRlResource;
    @FXML private TableColumn<ReservationLineModel, String>  colRlType;
    @FXML private TableColumn<ReservationLineModel, String>  colRlDate;
    @FXML private TableColumn<ReservationLineModel, String>  colRlStatus;

    // ── Actions ──
    @FXML private Button btnDeallocate;
    @FXML private Label  lblMsg;

    private final ResourceController ctrl = new ResourceController();
    private TaskModel selectedTask = null;

    // ─────────────────────────────────────────────────────────────
    // Step 1: initialize → reqLatestOrders
    // ─────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTaskTable();
        setupReservationTable();
        reqLatestOrders();
        btnDeallocate.setDisable(true);
    }

    private void reqLatestOrders() {
        var tasks = ctrl.getAllocatedUnorderedTasks();
        tableTask.setItems(FXCollections.observableArrayList(tasks));
        if (!ctrl.getErrorMessage().isEmpty()) {
            showError(ctrl.getErrorMessage());
            ctrl.clearMessages();
        }
        tableReservation.getItems().clear();
        btnDeallocate.setDisable(true);
        selectedTask = null;
    }

    // ─────────────────────────────────────────────────────────────
    // Step 2: เลือก task → trigger → โหลด reservation
    // ─────────────────────────────────────────────────────────────
    private void setupTaskTable() {
        colTaskId.setCellValueFactory(d ->
            new SimpleIntegerProperty(ctrl.task().getTaskId(d.getValue())).asObject());
        colTaskDesc.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.task().getDescription(d.getValue())));
        colTaskPlantation.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.task().getPlantationName(d.getValue())));
        colTaskDept.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.task().getDepartmentDisplay(d.getValue())));
        colTaskDeadline.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.task().getDeadline(d.getValue())));
        colTaskAllocated.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.task().getAllocatedStatusDisplay(d.getValue())));

        tableTask.getSelectionModel().selectedItemProperty()
            .addListener((obs, old, nw) -> { if (nw != null) trigger(nw); });
    }

    private void trigger(TaskModel task) {
        selectedTask = task;
        var lines = ctrl.getReservationsByTask(ctrl.task().getTaskId(task));
        tableReservation.setItems(FXCollections.observableArrayList(lines));
        btnDeallocate.setDisable(lines.isEmpty());
        lblMsg.setText("");
    }

    private void setupReservationTable() {
        colRlId.setCellValueFactory(d ->
            new SimpleIntegerProperty(ctrl.rl().getReservedId(d.getValue())).asObject());
        colRlResource.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.rl().getResourceName(d.getValue())));
        colRlType.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.rl().getResourceType(d.getValue())));
        colRlDate.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.rl().getDate(d.getValue())));
        colRlStatus.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.rl().getUsedStatus(d.getValue())));
    }

    // ─────────────────────────────────────────────────────────────
    // Step 3: กดยกเลิกจัดสรร → deallocateTask → refreshAndDisplay
    // ─────────────────────────────────────────────────────────────
    @FXML
    public void handleDeallocate(ActionEvent e) {
        if (selectedTask == null) { showError("กรุณาเลือก Task"); return; }

        int taskId    = ctrl.task().getTaskId(selectedTask);
        String desc   = ctrl.task().getDescription(selectedTask);
        int lineCount = tableReservation.getItems().size();
                boolean ok = ctrl.deallocateTask(taskId);
                if (ok) showSuccess(ctrl.getSuccessMessage());
                else    showError(ctrl.getErrorMessage());
                ctrl.clearMessages();
                reqLatestOrders();
            
    }

    @FXML
    public void handleRefresh(ActionEvent e) {
        reqLatestOrders();
        lblMsg.setText("");
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
