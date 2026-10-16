package com.palmfarm.controller.palmcare;

import com.palmfarm.controller.PalmCareController;
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

    @FXML private TableView<TaskModel>              tableAssigned;
    @FXML private TableColumn<TaskModel, Integer>   colAssignId;
    @FXML private TableColumn<TaskModel, String>    colAssignDesc;
    @FXML private TableColumn<TaskModel, String>    colAssignPlantation;
    @FXML private TableColumn<TaskModel, String>    colAssignStatus;
    @FXML private TableColumn<TaskModel, String>    colAssignOrdered;

    @FXML private TableView<ReservationLineModel>           tableReturn;
    @FXML private TableColumn<ReservationLineModel, String> colRetName;
    @FXML private TableColumn<ReservationLineModel, String> colRetReturn;

    @FXML private Button btnComplete;
    @FXML private Label  lblMsg;

    private final PalmCareController ctrl = new PalmCareController();
    private TaskModel selectedTask;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadTasks();
    }

    private void setupTable() {
        colAssignId.setCellValueFactory(d ->
            new SimpleIntegerProperty(ctrl.task().getTaskId(d.getValue())).asObject());
        colAssignDesc.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.task().getDescription(d.getValue())));
        colAssignPlantation.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.task().getPlantationName(d.getValue())));
        // ใช้ display helpers
        colAssignStatus.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.task().getCompleteStatusDisplay(d.getValue())));
        colAssignOrdered.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.task().getOrderedStatusDisplay(d.getValue())));

        colRetName.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.rl().getResourceName(d.getValue())));
        colRetReturn.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.rl().getReturnStatus(d.getValue())));

        tableAssigned.getSelectionModel().selectedItemProperty().addListener((obs, old, nw) -> {
            if (nw != null) {
                selectedTask = nw;
                tableReturn.setItems(FXCollections.observableArrayList(
                    ctrl.getReservationsForTask(ctrl.task().getTaskId(nw))));
                updateCompleteButton();
            }
        });
    }

    /** ปิดปุ่มเสร็จสิ้นถ้า task ยังไม่ได้ส่ง (is_ordered = FALSE) */
    private void updateCompleteButton() {
        if (selectedTask == null) {
            btnComplete.setDisable(true);
            lblMsg.setText("");
            return;
        }
        boolean ordered = ctrl.task().isOrdered(selectedTask);
        btnComplete.setDisable(!ordered);
        if (!ordered) {
            lblMsg.setStyle("-fx-text-fill: #ff8888;");
            lblMsg.setText("งานนี้ยังไม่ได้รับการมอบหมาย (ยังไม่ส่งงาน) ไม่สามารถกดเสร็จสิ้นได้");
        } else {
            lblMsg.setText("");
        }
    }

    private void loadTasks() {
        tableAssigned.setItems(FXCollections.observableArrayList(ctrl.getAssignedTasks()));
        selectedTask = null;
        btnComplete.setDisable(true);
        tableReturn.getItems().clear();
    }

    @FXML public void handleCompleteTask(ActionEvent e) {
        if (selectedTask == null) { lblMsg.setText("กรุณาเลือกงาน"); return; }

        // guard เพิ่มอีกชั้น — เผื่อ button ยังโดนกดได้
        if (!ctrl.task().isOrdered(selectedTask)) {
            lblMsg.setStyle("-fx-text-fill: #ff8888;");
            lblMsg.setText("งานนี้ยังไม่ได้รับการมอบหมาย ไม่สามารถเสร็จสิ้นได้");
            return;
        }
                boolean ok = ctrl.completeTaskAndReturnResources(ctrl.task().getTaskId(selectedTask));
                lblMsg.setStyle(ok ? "-fx-text-fill: #88ff88;" : "-fx-text-fill: #ff8888;");
                lblMsg.setText(ok ? ctrl.getSuccessMessage() : ctrl.getErrorMessage());
                ctrl.clearMessages();
                loadTasks();
            
    }

    @FXML public void handleRefresh(ActionEvent e) { loadTasks(); lblMsg.setText(""); }
}
