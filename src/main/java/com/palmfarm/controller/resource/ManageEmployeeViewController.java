package com.palmfarm.controller.resource;

import com.palmfarm.controller.ResourceController;
import com.palmfarm.model.EmployeeModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ManageEmployeeViewController implements Initializable {

    @FXML private TableView<EmployeeModel>           tableEmployee;
    @FXML private TableColumn<EmployeeModel, String> colId;
    @FXML private TableColumn<EmployeeModel, String> colName;
    @FXML private TableColumn<EmployeeModel, String> colDept;
    @FXML private TableColumn<EmployeeModel, String> colPosition;
    @FXML private TableColumn<EmployeeModel, String> colPhone;

    @FXML private TextField        txtNationalId;
    @FXML private TextField        txtUsername;
    @FXML private TextField        txtPassword;
    @FXML private TextField        txtFirstName;
    @FXML private TextField        txtLastName;
    @FXML private ComboBox<String> cbDept;
    @FXML private TextField        txtPosition;
    @FXML private TextField        txtPhone;
    @FXML private Label            lblMsg;

    private final ResourceController ctrl = new ResourceController();
    private EmployeeModel selectedEmp = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(d -> new SimpleStringProperty(ctrl.employee().getNationalId(d.getValue())));
        colName.setCellValueFactory(d -> new SimpleStringProperty(ctrl.employee().getFullName(d.getValue())));
        colDept.setCellValueFactory(d -> new SimpleStringProperty(ctrl.employee().getDepartment(d.getValue())));
        colPosition.setCellValueFactory(d -> new SimpleStringProperty(ctrl.employee().getPosition(d.getValue())));
        colPhone.setCellValueFactory(d -> new SimpleStringProperty(ctrl.employee().getPhone(d.getValue())));

        cbDept.setItems(FXCollections.observableArrayList(
            "RESOURCE","PALM_CARE","ACCOUNTING","HARVEST","CUTTING"));

        tableEmployee.getSelectionModel().selectedItemProperty().addListener((obs, old, nw) -> {
            if (nw != null) { selectedEmp = nw; populateForm(nw); }
        });
        loadEmployees();
    }

    private void loadEmployees() {
        tableEmployee.setItems(FXCollections.observableArrayList(ctrl.getAllEmployees()));
        selectedEmp = null;
        clearForm();
    }

    private void populateForm(EmployeeModel e) {
        txtNationalId.setText(ctrl.employee().getNationalId(e));
        txtNationalId.setDisable(true);
        txtUsername.setText(ctrl.employee().getUsername(e));
        txtPassword.clear();
        txtFirstName.setText(ctrl.employee().getFirstName(e));
        txtLastName.setText(ctrl.employee().getLastName(e));
        cbDept.setValue(ctrl.employee().getDepartment(e));
        txtPosition.setText(ctrl.employee().getPosition(e));
        txtPhone.setText(ctrl.employee().getPhone(e));
        lblMsg.setText("");
    }

    private void clearForm() {
        txtNationalId.clear(); txtNationalId.setDisable(false);
        txtUsername.clear(); txtPassword.clear();
        txtFirstName.clear(); txtLastName.clear();
        cbDept.setValue(null); txtPosition.clear(); txtPhone.clear();
        lblMsg.setText("");
    }

    private String validateAdd(String nationalId, String username) {
        if (nationalId.isBlank())                  return "กรุณากรอกเลขบัตรประชาชน";
        if (!nationalId.matches("\\d{13}"))        return "เลขบัตรประชาชนต้องเป็นตัวเลข 13 หลัก";
        if (txtUsername.getText().isBlank())        return "กรุณากรอก Username";
        if (txtPassword.getText().isBlank())        return "กรุณากรอก Password";
        if (txtFirstName.getText().isBlank())       return "กรุณากรอกชื่อ";
        if (txtLastName.getText().isBlank())        return "กรุณากรอกนามสกุล";
        if (cbDept.getValue() == null)              return "กรุณาเลือกฝ่าย";

        List<EmployeeModel> all = ctrl.getAllEmployees();
        for (EmployeeModel emp : all) {
            if (ctrl.employee().getNationalId(emp).equals(nationalId))
                return "เลขบัตรประชาชน " + nationalId + " มีอยู่แล้วในระบบ";
            if (ctrl.employee().getUsername(emp).equalsIgnoreCase(username))
                return "Username \"" + username + "\" มีอยู่แล้วในระบบ";
        }
        return null;
    }

    private String validateUpdate() {
        if (txtFirstName.getText().isBlank()) return "กรุณากรอกชื่อ";
        if (txtLastName.getText().isBlank())  return "กรุณากรอกนามสกุล";
        if (cbDept.getValue() == null)        return "กรุณาเลือกฝ่าย";
        return null;
    }

    @FXML public void handleAdd(ActionEvent e) {
        String nationalId = txtNationalId.getText().trim();
        String username   = txtUsername.getText().trim();
        String err = validateAdd(nationalId, username);
        if (err != null) { showError(err); return; }

        EmployeeModel emp = ctrl.employee().createEmpty();
        ctrl.employee().setAllForAdd(emp,
            nationalId, username, txtPassword.getText().trim(),
            txtFirstName.getText().trim(), txtLastName.getText().trim(),
            cbDept.getValue(), txtPosition.getText().trim(), txtPhone.getText().trim());
        boolean ok = ctrl.addEmployee(emp);
        show(ok); if (ok) loadEmployees();
    }

    @FXML public void handleUpdate(ActionEvent e) {
        if (selectedEmp == null) { showError("กรุณาเลือกพนักงานจากตาราง"); return; }
        String err = validateUpdate();
        if (err != null) { showError(err); return; }

        ctrl.employee().setAllForUpdate(selectedEmp,
            txtFirstName.getText().trim(), txtLastName.getText().trim(),
            cbDept.getValue(), txtPosition.getText().trim(), txtPhone.getText().trim());
        boolean ok = ctrl.updateEmployee(selectedEmp);
        show(ok); if (ok) loadEmployees();
    }

    @FXML public void handleExport(ActionEvent e) {
        var list = ctrl.getAllEmployees();
        if (list.isEmpty()) { showError("ไม่มีข้อมูลพนักงาน"); return; }
        int[] widths = {15, 26, 18, 18, 14};
        String[] headers = {"บัตรประชาชน", "ชื่อ-สกุล", "ฝ่าย", "ตำแหน่ง", "โทรศัพท์"};
        StringBuilder sb = com.palmfarm.util.ReportExporter.header(
            "Employee Report / รายงานพนักงานทั้งหมด");
        sb.append(com.palmfarm.util.ReportExporter.separator(widths)).append("\n");
        sb.append(com.palmfarm.util.ReportExporter.row(headers, widths)).append("\n");
        sb.append(com.palmfarm.util.ReportExporter.separator(widths)).append("\n");
        for (var emp : list) {
            String[] row = {
                ctrl.employee().getNationalId(emp),
                ctrl.employee().getFullName(emp),
                ctrl.employee().getDepartment(emp),
                ctrl.employee().getPosition(emp) != null ? ctrl.employee().getPosition(emp) : "",
                ctrl.employee().getPhone(emp) != null ? ctrl.employee().getPhone(emp) : ""
            };
            sb.append(com.palmfarm.util.ReportExporter.row(row, widths)).append("\n");
        }
        sb.append(com.palmfarm.util.ReportExporter.separator(widths)).append("\n");
        sb.append(com.palmfarm.util.ReportExporter.footer(list.size()));
        boolean ok = com.palmfarm.util.ReportExporter.saveToFile(
            tableEmployee.getScene().getWindow(), "employee_report.txt", sb.toString());
        lblMsg.setStyle(ok ? "-fx-text-fill:#88ff88;" : "-fx-text-fill:#aaaaaa;");
        lblMsg.setText(ok ? "Export สำเร็จ" : "ยกเลิก");
    }

    @FXML public void handleRefresh(ActionEvent e) { loadEmployees(); }

    private void show(boolean ok) {
        lblMsg.setStyle(ok ? "-fx-text-fill:#88ff88;" : "-fx-text-fill:#ff8888;");
        lblMsg.setText(ok ? ctrl.getSuccessMessage() : ctrl.getErrorMessage());
        ctrl.clearMessages();
    }
    private void showError(String msg) {
        lblMsg.setStyle("-fx-text-fill:#ff8888;"); lblMsg.setText(msg);
    }
}
