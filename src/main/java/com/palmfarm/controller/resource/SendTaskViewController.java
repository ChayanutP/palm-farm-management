package com.palmfarm.controller.resource;

import com.palmfarm.controller.ResourceController;
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

public class SendTaskViewController implements Initializable {

    @FXML private TableView<TaskModel>            tableTask;
    @FXML private TableColumn<TaskModel, Integer> colTaskId;
    @FXML private TableColumn<TaskModel, String>  colTaskDesc;
    @FXML private TableColumn<TaskModel, String>  colTaskPlantation;
    @FXML private TableColumn<TaskModel, String>  colTaskDept;
    @FXML private TableColumn<TaskModel, String>  colTaskOrdered;
    @FXML private TableColumn<TaskModel, String>  colTaskDeadline;
    @FXML private TableColumn<TaskModel, String>  colTaskDate;

    @FXML private Button btnSend;
    @FXML private Label  lblMsg;

    private final ResourceController ctrl = new ResourceController();
    private TaskModel selectedTask = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadTasks();
        btnSend.setDisable(true);
    }

    private void setupTable() {
        colTaskId.setCellValueFactory(d ->
            new SimpleIntegerProperty(ctrl.task().getTaskId(d.getValue())).asObject());
        colTaskDesc.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.task().getDescription(d.getValue())));
        colTaskPlantation.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.task().getPlantationName(d.getValue())));
        colTaskDept.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.task().getDepartmentDisplay(d.getValue())));
        colTaskOrdered.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.task().getOrderedStatusDisplay(d.getValue())));
        colTaskDeadline.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.task().getDeadline(d.getValue())));
        colTaskDate.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.task().getWorkOrderedDate(d.getValue())));

        tableTask.getSelectionModel().selectedItemProperty().addListener((obs, old, nw) -> {
            if (nw != null) {
                selectedTask = nw;
                btnSend.setDisable(false);
                lblMsg.setText("");
            }
        });
    }

    private void loadTasks() {
        tableTask.setItems(FXCollections.observableArrayList(ctrl.getUnorderedTasks()));
        selectedTask = null;
        btnSend.setDisable(true);
        if (!ctrl.getErrorMessage().isEmpty()) {
            lblMsg.setStyle("-fx-text-fill: #ff8888;");
            lblMsg.setText(ctrl.getErrorMessage());
            ctrl.clearMessages();
        }
    }

    @FXML public void handleSendTask(ActionEvent e) {
        if (selectedTask == null) {
            lblMsg.setStyle("-fx-text-fill: #ff8888;");
            lblMsg.setText("กรุณาเลือกงานที่ต้องการมอบหมาย");
            return;
        }
        boolean ok = ctrl.sendTask(ctrl.task().getTaskId(selectedTask));
        lblMsg.setStyle(ok ? "-fx-text-fill: #88ff88;" : "-fx-text-fill: #ff8888;");
        lblMsg.setText(ok ? ctrl.getSuccessMessage() : ctrl.getErrorMessage());
        ctrl.clearMessages();
        loadTasks();
    }

    @FXML public void handleRefresh(ActionEvent e) {
        loadTasks();
        lblMsg.setText("");
    }
}
