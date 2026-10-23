package com.palmfarm.controller.harvest;

import com.palmfarm.controller.HarvestController;
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

    private final HarvestController ctrl = new HarvestController();
    private TaskModel selectedTask;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadTasks();
        btnMarkUsed.setDisable(true);
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
        tableTask.setItems(FXCollections.observableArrayList(ctrl.getHarvestTasks()));
        tableReservation.getItems().clear();
        selectedTask = null;
        btnMarkUsed.setDisable(true);
    }

    @FXML public void handleMarkUsed(ActionEvent e) {
        if (selectedTask == null) { lblMsg.setText("กรุณาเลือก Task"); return; }
        boolean ok = ctrl.markResourceAsUsed(ctrl.task().getTaskId(selectedTask));
        lblMsg.setStyle(ok ? "-fx-text-fill:#88ff88;" : "-fx-text-fill:#ff8888;");
        lblMsg.setText(ok ? ctrl.getSuccessMessage() : ctrl.getErrorMessage());
        ctrl.clearMessages();
        loadTasks();
    }

    @FXML public void handleRefresh(ActionEvent e) { loadTasks(); lblMsg.setText(""); }
}
