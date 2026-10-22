package com.palmfarm.controller.resource;

import com.palmfarm.controller.ResourceController;
import com.palmfarm.model.PlantationModel;
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
import java.time.LocalDate;
import java.util.ResourceBundle;

public class CreateTaskViewController implements Initializable {
    @FXML private TextField                 txtDescription;
    @FXML private ComboBox<String>          cbDept;
    @FXML private ComboBox<PlantationModel> cbPlantation;
    @FXML private DatePicker                dpDeadline;
    @FXML private Label                     lblMsg;

    @FXML private TableView<TaskModel>            tableTask;
    @FXML private TableColumn<TaskModel, Integer> colId;
    @FXML private TableColumn<TaskModel, String>  colDesc;
    @FXML private TableColumn<TaskModel, String>  colPlantation;
    @FXML private TableColumn<TaskModel, String>  colDept;
    @FXML private TableColumn<TaskModel, String>  colStatus;
    @FXML private TableColumn<TaskModel, String>  colOrdered;
    @FXML private TableColumn<TaskModel, String>  colDeadline;

    private final ResourceController ctrl = new ResourceController();

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // แสดงภาษาไทยใน ComboBox แต่ map กลับเป็น DB value ตอน save
        cbDept.setItems(FXCollections.observableArrayList(
            "ฝ่ายจัดสรรทรัพยากร",
            "ฝ่ายดูแล",
            "ฝ่ายตรวจสอบคุณภาพ",
            "ฝ่ายบัญชี",
            "ฝ่ายเก็บเกี่ยว/ขนส่ง",
            "ฝ่ายตัด/ปลูก"
        ));

        cbPlantation.setItems(FXCollections.observableArrayList(ctrl.getAllPlantations()));
        cbPlantation.setConverter(new javafx.util.StringConverter<>() {
            public String toString(PlantationModel p) {
                return p == null ? "" : ctrl.plantation().getDisplayName(p);
            }
            public PlantationModel fromString(String s) { return null; }
        });

        setupTable();
        loadTasks();

        // ห้ามเลือก deadline ที่น้อยกว่าวันนี้ + 10 วัน
        LocalDate minDeadline = LocalDate.now().plusDays(10);
        dpDeadline.setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(minDeadline));
            }
        });
        dpDeadline.setValue(minDeadline); // default = วันนี้ + 10
    }

    private void setupTable() {
        colId.setCellValueFactory(d ->
            new SimpleIntegerProperty(ctrl.task().getTaskId(d.getValue())).asObject());
        colDesc.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.task().getDescription(d.getValue())));
        colPlantation.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.task().getPlantationName(d.getValue())));
        colDept.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.task().getDepartmentDisplay(d.getValue())));
        // ใช้ display helper — แสดงชื่อสถานะแทนค่า raw
        colStatus.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.task().getCompleteStatusDisplay(d.getValue())));
        colOrdered.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.task().getOrderedStatusDisplay(d.getValue())));
        colDeadline.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.task().getDeadline(d.getValue())));
    }

    private void loadTasks() {
        // แสดงแค่ task ล่าสุดต่อกลุ่ม (dept + plantation) ไม่ซ้ำซ้อน
        var tasks = ctrl.getLatestTaskPerGroup();
        if (!ctrl.getErrorMessage().isEmpty()) {
            lblMsg.setStyle("-fx-text-fill: #ff8888;");
            lblMsg.setText("โหลดข้อมูลไม่สำเร็จ: " + ctrl.getErrorMessage());
            ctrl.clearMessages();
        }
        tableTask.setItems(FXCollections.observableArrayList(tasks));
    }

    @FXML public void handleCreateTask(ActionEvent e) {
        if (txtDescription.getText().isBlank()
                || cbDept.getValue() == null
                || cbPlantation.getValue() == null
                || dpDeadline.getValue() == null) {
            lblMsg.setStyle("-fx-text-fill: #ff8888;");
            lblMsg.setText("กรุณากรอกข้อมูลให้ครบทุกช่อง");
            return;
        }

        // ตรวจ deadline ต้อง >= วันนี้ + 10
        if (dpDeadline.getValue().isBefore(LocalDate.now().plusDays(10))) {
            lblMsg.setStyle("-fx-text-fill: #ff8888;");
            lblMsg.setText("Deadline ต้องห่างจากวันนี้อย่างน้อย 10 วัน");
            return;
        }

        TaskModel t = ctrl.task().createEmpty();
        ctrl.task().setDescription(t,  txtDescription.getText().trim());
        ctrl.task().setDepartment(t,   toDbDepartment(cbDept.getValue()));
        ctrl.task().setPlantationId(t, ctrl.plantation().getPlantationId(cbPlantation.getValue()));
        ctrl.task().setDeadline(t,     dpDeadline.getValue().toString());

        boolean ok = ctrl.createTask(t);
        lblMsg.setStyle(ok ? "-fx-text-fill: #88ff88;" : "-fx-text-fill: #ff8888;");
        lblMsg.setText(ok ? ctrl.getSuccessMessage() : ctrl.getErrorMessage());
        ctrl.clearMessages();

        if (ok) {
            txtDescription.clear();
            cbDept.setValue(null);
            cbPlantation.setValue(null);
            dpDeadline.setValue(null);
        }
        loadTasks();
    }


    /** แปลงชื่อฝ่ายภาษาไทย → DB value */
    private String toDbDepartment(String display) {
        return switch (display) {
            case "ฝ่ายจัดสรรทรัพยากร"   -> "RESOURCE";
            case "ฝ่ายดูแล"              -> "PALM_CARE";
            case "ฝ่ายตรวจสอบคุณภาพ"   -> "QUALITY";
            case "ฝ่ายบัญชี"             -> "ACCOUNTING";
            case "ฝ่ายเก็บเกี่ยว/ขนส่ง" -> "HARVEST";
            case "ฝ่ายตัด/ปลูก"          -> "CUTTING";
            default                      -> display;
        };
    }

    @FXML public void handleRefresh(ActionEvent e) { loadTasks(); }
}
