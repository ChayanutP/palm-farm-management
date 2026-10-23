package com.palmfarm.controller.resource;

import com.palmfarm.controller.ResourceController;
import com.palmfarm.model.ReservationLineModel;
import com.palmfarm.model.ResourceModel;
import com.palmfarm.model.TaskModel;
import com.palmfarm.util.SessionManager;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AssignResourceViewController implements Initializable {

    //  Task selector 
    @FXML private ComboBox<TaskModel> cbTask;
    @FXML private Label lblTaskDeadline;
    @FXML private Label lblTaskPlantation;

    //  Resource table with checkbox 
    @FXML private TableView<ResourceRowItem>           tableResource;
    @FXML private TableColumn<ResourceRowItem, Boolean> colCheck;
    @FXML private TableColumn<ResourceRowItem, Integer> colResId;
    @FXML private TableColumn<ResourceRowItem, String>  colResName;
    @FXML private TableColumn<ResourceRowItem, String>  colResType;
    @FXML private TableColumn<ResourceRowItem, String>  colResStatus;

    //  Reservation list 
    @FXML private TableView<ReservationLineModel>            tableReservation;
    @FXML private TableColumn<ReservationLineModel, String>  colRlResource;
    @FXML private TableColumn<ReservationLineModel, String>  colRlType;
    @FXML private TableColumn<ReservationLineModel, String>  colRlDate;
    @FXML private TableColumn<ReservationLineModel, String>  colRlStatus;

    @FXML private Label lblMsg;

    private final ResourceController ctrl = new ResourceController();
    private TaskModel selectedTask = null;

    //  wrapper สำหรับ checkbox 
    public static class ResourceRowItem {
        public final SimpleBooleanProperty selected = new SimpleBooleanProperty(false);
        public final ResourceModel resource;
        public ResourceRowItem(ResourceModel r) { this.resource = r; }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        setupTaskCombo();
        setupResourceTable();
        setupReservationTable();
        loadResources();
    }

    //  Task ComboBox 
    private void setupTaskCombo() {
        var tasks = ctrl.getAssignableTasks();
        cbTask.setItems(FXCollections.observableArrayList(tasks));
        cbTask.setConverter(new javafx.util.StringConverter<>() {
            public String toString(TaskModel t) {
                if (t == null) return "";
                return "Task #" + ctrl.task().getTaskId(t)
                    + " [" + ctrl.task().getPlantationName(t) + "] " + ctrl.task().getDescription(t);
            }
            public TaskModel fromString(String s) { return null; }
        });
        cbTask.getSelectionModel().selectedItemProperty().addListener((obs, old, nw) -> {
            if (nw != null) {
                selectedTask = nw;
                lblTaskDeadline.setText("กำหนดส่ง: " + ctrl.task().getDeadline(nw));
                lblTaskPlantation.setText("สวน: " + ctrl.task().getPlantationName(nw));
                loadReservations();
            } else {
                lblTaskDeadline.setText("กำหนดส่ง: —");
                lblTaskPlantation.setText("สวน: —");
                tableReservation.getItems().clear();
            }
        });
    }

    //  Resource TableView + CheckBox 
    private void setupResourceTable() {
        tableResource.setEditable(true);

        // checkbox column
        colCheck.setCellValueFactory(d -> d.getValue().selected);
        colCheck.setCellFactory(CheckBoxTableCell.forTableColumn(colCheck));
        colCheck.setEditable(true);

        colResId.setCellValueFactory(d ->
            new SimpleIntegerProperty(ctrl.resource().getResourceId(d.getValue().resource)).asObject());
        colResName.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.resource().getResourceName(d.getValue().resource)));
        colResType.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.resource().getResourceType(d.getValue().resource)));
        colResStatus.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.resource().getStatus(d.getValue().resource)));
    }

    //  Reservation TableView 
    private void setupReservationTable() {
        colRlResource.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.rl().getResourceName(d.getValue())));
        colRlType.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.rl().getResourceType(d.getValue())));
        colRlDate.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.rl().getDate(d.getValue())));
        colRlStatus.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.rl().getStatus(d.getValue())));
    }

    //  Load available resources 
    private void loadResources() {
        var items = new ArrayList<ResourceRowItem>();
        for (ResourceModel r : ctrl.getAvailableResources()) {
            items.add(new ResourceRowItem(r));
        }
        tableResource.setItems(FXCollections.observableArrayList(items));
    }

    //  Load reservations for selected task 
    private void loadReservations() {
        if (selectedTask == null) return;
        tableReservation.setItems(FXCollections.observableArrayList(
            ctrl.getReservationsByTask(ctrl.task().getTaskId(selectedTask))));
    }

    //  กดจัดสรร 
    @FXML public void handleAssign(ActionEvent e) {
        if (selectedTask == null) {
            lblMsg.setStyle("-fx-text-fill:#ff8888;");
            lblMsg.setText("กรุณาเลือก Task ก่อน");
            return;
        }

        // เก็บ resource_id ที่ tick checkbox ไว้
        List<Integer> selectedIds = new ArrayList<>();
        for (ResourceRowItem row : tableResource.getItems()) {
            if (row.selected.get()) {
                selectedIds.add(ctrl.resource().getResourceId(row.resource));
            }
        }

        if (selectedIds.isEmpty()) {
            lblMsg.setStyle("-fx-text-fill:#ff8888;");
            lblMsg.setText("กรุณาเลือกทรัพยากรอย่างน้อย 1 รายการ");
            return;
        }

        boolean ok = ctrl.assignResourcesToTask(
            ctrl.task().getTaskId(selectedTask), selectedIds);

        if (ok) {
            lblMsg.setStyle("-fx-text-fill:#88ff88;");
            lblMsg.setText(ctrl.getSuccessMessage());
        } else {
            lblMsg.setStyle("-fx-text-fill:#ff8888;");
            lblMsg.setText(ctrl.getErrorMessage());
        }
        ctrl.clearMessages();

        // auto-refresh หลังจัดสรร
        loadResources();
        loadReservations();
        refreshTaskCombo();
    }

    @FXML public void handleRefresh(ActionEvent e) {
        loadResources();
        loadReservations();
        refreshTaskCombo();
        lblMsg.setText("");
    }

    private void refreshTaskCombo() {
        TaskModel prev = selectedTask;
        var tasks = ctrl.getAssignableTasks();
        cbTask.setItems(FXCollections.observableArrayList(tasks));
        // คืน selection เดิมถ้ายังมีอยู่
        if (prev != null) {
            tasks.stream()
                .filter(t -> ctrl.task().getTaskId(t) == ctrl.task().getTaskId(prev))
                .findFirst()
                .ifPresent(t -> cbTask.getSelectionModel().select(t));
        }
    }
}
