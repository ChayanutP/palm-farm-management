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

public class ReturnResourceViewController implements Initializable {

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
    @FXML private TableColumn<ReservationLineModel, String> colRlReturn;

    @FXML private Button btnComplete;
    @FXML private Label  lblMsg;

    private final HarvestController ctrl = new HarvestController();
    private TaskModel selectedTask;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadTasks();
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
        colRlReturn.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.rl().getReturnStatus(d.getValue())));

        tableTask.getSelectionModel().selectedItemProperty().addListener((obs, old, nw) -> {
            if (nw != null) {
                selectedTask = nw;
                tableReservation.setItems(FXCollections.observableArrayList(
                    ctrl.getReservationsForTask(ctrl.task().getTaskId(nw))));
                updateCompleteButton();
            }
        });
    }

    private void updateCompleteButton() {
        if (selectedTask == null) { btnComplete.setDisable(true); lblMsg.setText(""); return; }
        boolean ordered = ctrl.task().isOrdered(selectedTask);
        btnComplete.setDisable(!ordered);
        if (!ordered) {
            lblMsg.setStyle("-fx-text-fill:#ff8888;");
            lblMsg.setText("งานนี้ยังไม่ได้รับการมอบหมาย ไม่สามารถกดเสร็จสิ้นได้");
        } else {
            lblMsg.setText("");
        }
    }

    private void loadTasks() {
        tableTask.setItems(FXCollections.observableArrayList(ctrl.getHarvestTasks()));
        tableReservation.getItems().clear();
        selectedTask = null;
        btnComplete.setDisable(true);
    }

    @FXML public void handleCompleteTask(ActionEvent e) {
        if (selectedTask == null) { lblMsg.setText("กรุณาเลือกงาน"); return; }
        if (!ctrl.task().isOrdered(selectedTask)) {
            lblMsg.setStyle("-fx-text-fill:#ff8888;");
            lblMsg.setText("งานนี้ยังไม่ได้รับการมอบหมาย ไม่สามารถเสร็จสิ้นได้");
            return;
        }
                boolean ok = ctrl.completeTask(ctrl.task().getTaskId(selectedTask));
                lblMsg.setStyle(ok ? "-fx-text-fill:#88ff88;" : "-fx-text-fill:#ff8888;");
                lblMsg.setText(ok ? ctrl.getSuccessMessage() : ctrl.getErrorMessage());
                ctrl.clearMessages();
                loadTasks();
            
    }

    @FXML public void handleRefresh(ActionEvent e) { loadTasks(); lblMsg.setText(""); }
}
