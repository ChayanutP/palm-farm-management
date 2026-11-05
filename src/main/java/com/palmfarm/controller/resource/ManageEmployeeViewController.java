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
        txtPassword.setText(ctrl.employee().getPassword(e));
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
        if (txtUsername.getText().isBlank())  return "กรุณากรอก Username";
        if (txtFirstName.getText().isBlank()) return "กรุณากรอกชื่อ";
        if (txtLastName.getText().isBlank())  return "กรุณากรอกนามสกุล";
        if (cbDept.getValue() == null)        return "กรุณาเลือกฝ่าย";

        // Username ต้องไม่ซ้ำกับพนักงานคนอื่น (ข้ามพนักงานที่กำลังแก้ไขอยู่)
        String username   = txtUsername.getText().trim();
        String selectedId = ctrl.employee().getNationalId(selectedEmp);
        for (EmployeeModel emp : ctrl.getAllEmployees()) {
            if (ctrl.employee().getNationalId(emp).equals(selectedId)) continue;
            if (ctrl.employee().getUsername(emp).equalsIgnoreCase(username))
                return "Username \"" + username + "\" มีอยู่แล้วในระบบ";
        }
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
        if (ok) loadEmployees();
        show(ok); 
    }

    @FXML public void handleUpdate(ActionEvent e) {
        if (selectedEmp == null) { showError("กรุณาเลือกพนักงานจากตาราง"); return; }
        String err = validateUpdate();
        if (err != null) { showError(err); return; }

        ctrl.employee().setAllForUpdate(selectedEmp,
            txtUsername.getText().trim(),
            txtFirstName.getText().trim(), txtLastName.getText().trim(),
            cbDept.getValue(), txtPosition.getText().trim(), txtPhone.getText().trim());
        boolean ok = ctrl.updateEmployee(selectedEmp, txtPassword.getText().trim()); // ว่าง = ไม่เปลี่ยนรหัสผ่าน
        if (ok) loadEmployees();
        show(ok);
    }



    private void show(boolean ok) {
        lblMsg.setStyle(ok ? "-fx-text-fill:#88ff88;" : "-fx-text-fill:#ff8888;");
        lblMsg.setText(ok ? ctrl.getSuccessMessage() : ctrl.getErrorMessage());
        ctrl.clearMessages();
    }
    private void showError(String msg) {
        lblMsg.setStyle("-fx-text-fill:#ff8888;"); lblMsg.setText(msg);
    }
}
