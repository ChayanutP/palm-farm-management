package com.palmfarm.controller.palmcare;

import com.palmfarm.controller.PalmCareController;
import com.palmfarm.model.ReservationLineModel;
import com.palmfarm.model.TaskModel;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.util.ResourceBundle;

public class UseResourceViewController implements Initializable {
    @FXML private TableView<TaskModel>              tableTask;
    @FXML private TableColumn<TaskModel, Integer>   colTaskId;
    @FXML private TableColumn<TaskModel, String>    colTaskDesc;
    @FXML private TableColumn<TaskModel, String>    colTaskStatus;
    @FXML private TableColumn<TaskModel, String>    colTaskDeadline;
    @FXML private TableView<ReservationLineModel>           tableReservation;
    @FXML private TableColumn<ReservationLineModel, String> colRlName;
    @FXML private TableColumn<ReservationLineModel, String> colRlType;
    @FXML private TableColumn<ReservationLineModel, String> colRlUsed;
    @FXML private Label lblMsg;

    private final PalmCareController ctrl = new PalmCareController();
    private TaskModel selectedTask;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadTasks();
    }

    private void setupTable() {
        colTaskId.setCellValueFactory(d ->
            new javafx.beans.property.SimpleIntegerProperty(ctrl.task().getTaskId(d.getValue())).asObject());
        colTaskDesc.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(ctrl.task().getDescription(d.getValue())));
        colTaskStatus.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(ctrl.task().getCompleteStatusDisplay(d.getValue())));
        colTaskDeadline.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(ctrl.task().getDeadline(d.getValue())));
        colRlName.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(ctrl.rl().getResourceName(d.getValue())));
        colRlType.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(ctrl.rl().getResourceType(d.getValue())));
        colRlUsed.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(ctrl.rl().getUsedStatus(d.getValue())));

        tableTask.getSelectionModel().selectedItemProperty().addListener((obs, old, nw) -> {
            if (nw != null) {
                selectedTask = nw;
                tableReservation.setItems(FXCollections.observableArrayList(
                    ctrl.getReservationsForTask(ctrl.task().getTaskId(nw))));
            }
        });
    }

    private void loadTasks() {
        tableTask.setItems(FXCollections.observableArrayList(ctrl.getUncompletedTasksForCurrentDept()));
    }

    @FXML public void handleMarkUsed(ActionEvent e) {
        if (selectedTask == null) { lblMsg.setText("กรุณาเลือก Task"); return; }
        boolean ok = ctrl.markResourceAsUsed(ctrl.task().getTaskId(selectedTask));
        lblMsg.setText(ok ? ctrl.getSuccessMessage() : ctrl.getErrorMessage());
        ctrl.clearMessages();
        loadTasks();
        tableReservation.setItems(FXCollections.observableArrayList(
            ctrl.getReservationsForTask(ctrl.task().getTaskId(selectedTask))));
    }

    @FXML public void handleRefresh(ActionEvent e) { loadTasks(); }
}
