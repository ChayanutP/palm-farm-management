package com.palmfarm.controller.resource;

import com.palmfarm.controller.ResourceController;
import com.palmfarm.model.PlantationModel;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ManagePlantationViewController implements Initializable {

    @FXML private TableView<PlantationModel>            tablePlantation;
    @FXML private TableColumn<PlantationModel, Integer> colId;
    @FXML private TableColumn<PlantationModel, String>  colName;
    @FXML private TableColumn<PlantationModel, String>  colArea;
    @FXML private TableColumn<PlantationModel, String>  colLocation;

    @FXML private TextField txtName;
    @FXML private TextField txtArea;
    @FXML private TextField txtLocation;
    @FXML private Label     lblMsg;

    private final ResourceController ctrl = new ResourceController();
    private PlantationModel selectedPlantation = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(d -> new SimpleIntegerProperty(ctrl.plantation().getPlantationId(d.getValue())).asObject());
        colName.setCellValueFactory(d -> new SimpleStringProperty(ctrl.plantation().getName(d.getValue())));
        colArea.setCellValueFactory(d -> new SimpleStringProperty(ctrl.plantation().getAreaRai(d.getValue()) + " ไร่"));
        colLocation.setCellValueFactory(d -> new SimpleStringProperty(ctrl.plantation().getLocation(d.getValue())));

        tablePlantation.getSelectionModel().selectedItemProperty().addListener((obs, old, nw) -> {
            if (nw != null) { selectedPlantation = nw; populateForm(nw); }
        });
        loadPlantations();
    }

    private void loadPlantations() {
        tablePlantation.setItems(FXCollections.observableArrayList(ctrl.getAllPlantations()));
        selectedPlantation = null;
        clearForm();
    }

    private void populateForm(PlantationModel p) {
        txtName.setText(ctrl.plantation().getName(p));
        txtArea.setText(String.valueOf(ctrl.plantation().getAreaRai(p)));
        txtLocation.setText(ctrl.plantation().getLocation(p));
        lblMsg.setText("");
    }

    private void clearForm() {
        txtName.clear(); txtArea.clear(); txtLocation.clear(); lblMsg.setText("");
    }

    private String validate(String name, String area, boolean isAdd) {
        if (name.isBlank())     return "กรุณากรอกชื่อสวน";
        if (txtLocation.getText().isBlank()) return "กรุณากรอกที่ตั้ง";
        if (area.isBlank())     return "กรุณากรอกพื้นที่";
        double areaVal;
        try { areaVal = Double.parseDouble(area); }
        catch (NumberFormatException ex) { return "พื้นที่ต้องเป็นตัวเลข"; }
        if (areaVal <= 0)       return "พื้นที่ต้องมากกว่า 0";

        List<PlantationModel> all = ctrl.getAllPlantations();
        for (PlantationModel p : all) {
            String existing = ctrl.plantation().getName(p);
            if (existing.equalsIgnoreCase(name)) {
                if (!isAdd && selectedPlantation != null &&
                    ctrl.plantation().getPlantationId(p) == ctrl.plantation().getPlantationId(selectedPlantation))
                    continue;
                return "ชื่อสวน \"" + name + "\" มีอยู่แล้วในระบบ";
            }
        }
        return null;
    }

    @FXML public void handleAdd(ActionEvent e) {
        String name = txtName.getText().trim();
        String area = txtArea.getText().trim();
        String err  = validate(name, area, true);
        if (err != null) { showError(err); return; }

        PlantationModel p = ctrl.plantation().createEmpty();
        ctrl.plantation().setAll(p, name,
            Double.parseDouble(area), txtLocation.getText().trim());
        boolean ok = ctrl.addPlantation(p);
        show(ok); if (ok) loadPlantations();
    }

    @FXML public void handleUpdate(ActionEvent e) {
        if (selectedPlantation == null) { showError("กรุณาเลือกสวนจากตาราง"); return; }
        String name = txtName.getText().trim();
        String area = txtArea.getText().trim();
        String err  = validate(name, area, false);
        if (err != null) { showError(err); return; }

        ctrl.plantation().setAll(selectedPlantation, name,
            Double.parseDouble(area), txtLocation.getText().trim());
        boolean ok = ctrl.updatePlantation(selectedPlantation);
        show(ok); if (ok) loadPlantations();
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
