package com.palmfarm.controller;

import com.palmfarm.model.*;
import com.palmfarm.util.SessionManager;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ResourceViewController implements Initializable {
    @FXML private TabPane tabPane;

    //  Tab: Task 
    @FXML private TableView<TaskModel>                tableTask;
    @FXML private TableColumn<TaskModel, Integer>     colTaskId;
    @FXML private TableColumn<TaskModel, String>      colTaskDesc;
    @FXML private TableColumn<TaskModel, String>      colTaskDept;
    @FXML private TableColumn<TaskModel, String>      colTaskStatus;
    @FXML private TableColumn<TaskModel, String>      colTaskDeadline;
    @FXML private TextField                           txtTaskDescription;
    @FXML private ComboBox<String>                    cbTaskDept;
    @FXML private DatePicker                          dpDeadline;
    @FXML private Label                               lblTaskMsg;

    //  Tab: Resource 
    @FXML private TableView<ResourceModel>            tableResource;
    @FXML private TableColumn<ResourceModel, Integer> colResId;
    @FXML private TableColumn<ResourceModel, String>  colResName;
    @FXML private TableColumn<ResourceModel, String>  colResType;
    @FXML private TableColumn<ResourceModel, String>  colResStatus;
    @FXML private TextField                           txtResName;
    @FXML private ComboBox<String>                    cbResType;
    @FXML private Label                               lblResMsg;

    //  Tab: จัดสรร 
    @FXML private ComboBox<TaskModel>                         cbAssignTask;
    @FXML private ComboBox<ResourceModel>                     cbAssignResource;
    @FXML private DatePicker                                  dpAssignDate;
    @FXML private TableView<ReservationLineModel>             tableReservation;
    @FXML private TableColumn<ReservationLineModel, String>   colRlResource;
    @FXML private TableColumn<ReservationLineModel, String>   colRlType;
    @FXML private TableColumn<ReservationLineModel, String>   colRlDate;
    @FXML private TableColumn<ReservationLineModel, String>   colRlStatus;
    @FXML private Label                                       lblAssignMsg;

    private final ResourceController ctrl = new ResourceController();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTaskTable();
        setupResourceTable();
        setupReservationTable();
        setupComboBoxes();
        loadTasks();
        loadResources();
    }

    //  Setup 

    private void setupTaskTable() {
        colTaskId.setCellValueFactory(d ->
            new javafx.beans.property.SimpleIntegerProperty(ctrl.task().getTaskId(d.getValue())).asObject());
        colTaskDesc.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.task().getDescription(d.getValue())));
        colTaskDept.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.task().getDepartmentDisplay(d.getValue())));
        colTaskStatus.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(ctrl.task().getCompleteStatusDisplay(d.getValue())));
        colTaskDeadline.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.task().getDeadline(d.getValue())));
    }

    private void setupResourceTable() {
        colResId.setCellValueFactory(d ->
            new javafx.beans.property.SimpleIntegerProperty(ctrl.resource().getResourceId(d.getValue())).asObject());
        colResName.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.resource().getResourceName(d.getValue())));
        colResType.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.resource().getResourceType(d.getValue())));
        colResStatus.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.resource().getStatus(d.getValue())));
    }

    private void setupReservationTable() {
        colRlResource.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.rl().getResourceName(d.getValue())));
        colRlType.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.rl().getResourceType(d.getValue())));
        colRlDate.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.rl().getDate(d.getValue())));
        colRlStatus.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.rl().getUsedStatus(d.getValue())));
    }

    private void setupComboBoxes() {
        cbTaskDept.setItems(FXCollections.observableArrayList(
            "RESOURCE","PALM_CARE","QUALITY","ACCOUNTING","HARVEST","CUTTING"));
        // ตัด OTHER ออกเพื่อให้ตรงกับ ENUM('VEHICLE', 'EQUIPMENT') ตาม ER Diagram
        cbResType.setItems(FXCollections.observableArrayList("VEHICLE","EQUIPMENT"));
    }

    //  Load 

    private void loadTasks() {
        List<TaskModel> tasks = ctrl.getAllTasks();
        tableTask.setItems(FXCollections.observableArrayList(tasks));
        cbAssignTask.setItems(FXCollections.observableArrayList(tasks));
        cbAssignTask.setConverter(new javafx.util.StringConverter<>() {
            public String toString(TaskModel t) {
                return t == null ? "" : "Task #" + ctrl.task().getTaskId(t) + " - " + ctrl.task().getDescription(t);
            }
            public TaskModel fromString(String s) { return null; }
        });
    }

    private void loadResources() {
        tableResource.setItems(FXCollections.observableArrayList(ctrl.getAllResources()));
        List<ResourceModel> avail = ctrl.getAvailableResources();
        cbAssignResource.setItems(FXCollections.observableArrayList(avail));
        cbAssignResource.setConverter(new javafx.util.StringConverter<>() {
            public String toString(ResourceModel r) {
                return r == null ? "" : ctrl.resource().getDisplayName(r);
            }
            public ResourceModel fromString(String s) { return null; }
        });
    }

    //  Actions: Task 

    @FXML public void handleCreateTask(ActionEvent e) {
        if (txtTaskDescription.getText().isBlank() || cbTaskDept.getValue() == null || dpDeadline.getValue() == null) {
            lblTaskMsg.setText("กรุณากรอกข้อมูลให้ครบ"); return;
        }
        TaskModel t = ctrl.task().createEmpty();
        ctrl.task().setDescription(t, txtTaskDescription.getText().trim());
        ctrl.task().setDepartment(t,  cbTaskDept.getValue());
        ctrl.task().setDeadline(t,    dpDeadline.getValue().toString());

        ctrl.createTask(t);
        lblTaskMsg.setText(ctrl.getSuccessMessage().isEmpty() ? ctrl.getErrorMessage() : ctrl.getSuccessMessage());
        ctrl.clearMessages();
        loadTasks();
        txtTaskDescription.clear();
    }

    @FXML public void handleRefreshTask(ActionEvent e)     { loadTasks(); }
    @FXML public void handleRefreshResource(ActionEvent e) { loadResources(); }

    //  Actions: Resource 

    @FXML public void handleAddResource(ActionEvent e) {
        if (txtResName.getText().isBlank() || cbResType.getValue() == null) {
            lblResMsg.setText("กรุณากรอกชื่อและประเภท"); return;
        }
        ResourceModel r = ctrl.resource().createEmpty();
        // ต้องตั้งค่า Type ก่อน Name เพื่อให้ ResourceModelController แยกใส่ฟิลด์ลูกได้ถูกตาราง
        ctrl.resource().setResourceType(r, cbResType.getValue());
        ctrl.resource().setResourceName(r, txtResName.getText().trim());

        ctrl.addResource(r);
        lblResMsg.setText(ctrl.getSuccessMessage().isEmpty() ? ctrl.getErrorMessage() : ctrl.getSuccessMessage());
        ctrl.clearMessages();
        loadResources();
        txtResName.clear();
    }

    //  Actions: Assign 

    @FXML public void handleAssignResource(ActionEvent e) {
        TaskModel task    = cbAssignTask.getValue();
        ResourceModel res = cbAssignResource.getValue();
        if (task == null || res == null) {
            lblAssignMsg.setText("กรุณาเลือก Task และทรัพยากร"); return;
        }
        ctrl.assignResourcesToTask(
            ctrl.task().getTaskId(task),
            java.util.List.of(ctrl.resource().getResourceId(res)));
        lblAssignMsg.setText(ctrl.getSuccessMessage().isEmpty() ? ctrl.getErrorMessage() : ctrl.getSuccessMessage());
        ctrl.clearMessages();
        loadResources();
        tableReservation.setItems(FXCollections.observableArrayList(
            ctrl.getReservationsByTask(ctrl.task().getTaskId(task))));
    }

    @FXML public void handleLoadReservations(ActionEvent e) {
        TaskModel task = cbAssignTask.getValue();
        if (task == null) { lblAssignMsg.setText("กรุณาเลือก Task"); return; }
        tableReservation.setItems(FXCollections.observableArrayList(
            ctrl.getReservationsByTask(ctrl.task().getTaskId(task))));
    }

    @FXML public void handleLogout(ActionEvent e) {
        SessionManager.clearSession();
        javafx.stage.Stage stage = (javafx.stage.Stage) tabPane.getScene().getWindow();
        stage.close();
        new com.palmfarm.window.LoginWindow(new javafx.stage.Stage()).show();
    }
}