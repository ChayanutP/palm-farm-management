package com.palmfarm.controller;

import com.palmfarm.model.*;
import com.palmfarm.util.SessionManager;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class QualityViewController implements Initializable {
    @FXML private TabPane tabPane;

    @FXML private TableView<TaskModel>              tableTask;
    @FXML private TableColumn<TaskModel, Integer>   colTaskId;
    @FXML private TableColumn<TaskModel, String>    colTaskDesc;
    @FXML private TableColumn<TaskModel, String>    colTaskStatus;
    @FXML private TableColumn<TaskModel, String>    colTaskDeadline;

    @FXML private TableView<QualityInspectionModel>              tableInspection;
    @FXML private TableColumn<QualityInspectionModel, Integer>   colQiId;
    @FXML private TableColumn<QualityInspectionModel, String>    colQiQuality;
    @FXML private TableColumn<QualityInspectionModel, String>    colQiDefect;
    @FXML private TableColumn<QualityInspectionModel, String>    colQiDate;
    @FXML private TableColumn<QualityInspectionModel, String>    colQiAmount;
    @FXML private TableColumn<QualityInspectionModel, String>    colQiInspector;

    @FXML private ComboBox<TaskModel>  cbInspectTask;
    @FXML private ComboBox<String>     cbQualityGrade;
    @FXML private TextArea             txtDefect;
    @FXML private TextField            txtAmount;
    @FXML private TextField            txtInspector;
    @FXML private DatePicker           dpInspectDate;
    @FXML private Label                lblQualityMsg;

    private final QualityController ctrl = new QualityController();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        var empMC = new com.palmfarm.controller.model.EmployeeModelController();

        cbQualityGrade.setItems(FXCollections.observableArrayList("A","B","C","D","REJECT"));
        setupTables();
        loadTasks();
        loadInspections();
    }

    private void setupTables() {
        colTaskId.setCellValueFactory(d ->
            new SimpleIntegerProperty(ctrl.task().getTaskId(d.getValue())).asObject());
        colTaskDesc.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.task().getDescription(d.getValue())));
        colTaskStatus.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(ctrl.task().getCompleteStatusDisplay(d.getValue())));
        colTaskDeadline.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.task().getDeadline(d.getValue())));

        colQiId.setCellValueFactory(d ->
            new SimpleIntegerProperty(ctrl.inspection().getInspectionId(d.getValue())).asObject());
        colQiQuality.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.inspection().getQualityId(d.getValue())));
        colQiDefect.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.inspection().getDefectDetails(d.getValue())));
        colQiDate.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.inspection().getInspectionDate(d.getValue())));
        colQiAmount.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.inspection().getAmountFormatted(d.getValue())));
        colQiInspector.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.inspection().getInspectorName(d.getValue())));
    }

    private void loadTasks() {
        var tasks = ctrl.getQualityTasks();
        tableTask.setItems(FXCollections.observableArrayList(tasks));
        cbInspectTask.setItems(FXCollections.observableArrayList(tasks));
        cbInspectTask.setConverter(new javafx.util.StringConverter<>() {
            public String toString(TaskModel t) {
                return t == null ? "" : "Task #" + ctrl.task().getTaskId(t)
                    + " (" + ctrl.task().getDeadline(t) + ")";
            }
            public TaskModel fromString(String s) { return null; }
        });
    }

    private void loadInspections() {
        tableInspection.setItems(FXCollections.observableArrayList(ctrl.getAllInspections()));
    }

    @FXML public void handleSaveInspection(ActionEvent e) {
        TaskModel task = cbInspectTask.getValue();
        if (task == null || cbQualityGrade.getValue() == null
                || txtAmount.getText().isBlank() || txtInspector.getText().isBlank()
                || dpInspectDate.getValue() == null) {
            lblQualityMsg.setText("กรุณากรอกข้อมูลให้ครบ"); return;
        }
        double amount;
        try { amount = Double.parseDouble(txtAmount.getText()); }
        catch (NumberFormatException ex) { lblQualityMsg.setText("ปริมาณต้องเป็นตัวเลข"); return; }

        QualityInspectionModel qi = ctrl.inspection().createEmpty();
        ctrl.inspection().setTaskId(qi,         ctrl.task().getTaskId(task));
        ctrl.inspection().setPlantationId(qi,   ctrl.task().getPlantationId(task));
        ctrl.inspection().setQualityId(qi,      cbQualityGrade.getValue());
        ctrl.inspection().setDefectDetails(qi,  txtDefect.getText().trim());
        ctrl.inspection().setAmount(qi,         amount);
        ctrl.inspection().setInspectorName(qi,  txtInspector.getText().trim());
        ctrl.inspection().setInspectionDate(qi, dpInspectDate.getValue().toString());

        boolean ok = ctrl.saveInspection(qi);
        lblQualityMsg.setText(ok ? ctrl.getSuccessMessage() : ctrl.getErrorMessage());
        ctrl.clearMessages();
        if (ok) { loadInspections(); txtDefect.clear(); txtAmount.clear(); txtInspector.clear(); }
    }

    @FXML public void handleRefresh(ActionEvent e) { loadTasks(); loadInspections(); }

    @FXML public void handleLogout(ActionEvent e) {
        SessionManager.clearSession();
        javafx.stage.Stage stage = (javafx.stage.Stage) tabPane.getScene().getWindow();
        stage.close();
        new com.palmfarm.window.LoginWindow(new javafx.stage.Stage()).show();
    }
}
