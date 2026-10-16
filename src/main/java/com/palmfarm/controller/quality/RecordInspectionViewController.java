package com.palmfarm.controller.quality;

import com.palmfarm.controller.QualityController;
import com.palmfarm.model.PlantationModel;
import com.palmfarm.model.QualityInspectionModel;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.util.ResourceBundle;

public class RecordInspectionViewController implements Initializable {

    // ── Form ──
    @FXML private ComboBox<PlantationModel> cbPlantation;  // เลือกสวนแทน task
    @FXML private ComboBox<String>          cbGrade;
    @FXML private TextArea                  txtDefect;
    @FXML private TextField                 txtInspector;
    @FXML private DatePicker                dpInspectDate;
    @FXML private Label                     lblMsg;

    // ── Table ──
    @FXML private TableView<QualityInspectionModel>            tableInspection;
    @FXML private TableColumn<QualityInspectionModel, Integer> colQiId;
    @FXML private TableColumn<QualityInspectionModel, String>  colQiPlantation;
    @FXML private TableColumn<QualityInspectionModel, String>  colQiGrade;
    @FXML private TableColumn<QualityInspectionModel, String>  colQiDefect;
    @FXML private TableColumn<QualityInspectionModel, String>  colQiDate;
    @FXML private TableColumn<QualityInspectionModel, String>  colQiInspector;

    private final QualityController ctrl = new QualityController();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbGrade.setItems(FXCollections.observableArrayList("A", "B", "C", "D", "REJECT"));

        // โหลด plantation combo
        var plantations = ctrl.getAllPlantations();
        cbPlantation.setItems(FXCollections.observableArrayList(plantations));
        cbPlantation.setConverter(new javafx.util.StringConverter<>() {
            public String toString(PlantationModel p) {
                return p == null ? "" : ctrl.plantation().getName(p);
            }
            public PlantationModel fromString(String s) { return null; }
        });

        setupTable();
        loadInspections();
    }

    private void setupTable() {
        colQiId.setCellValueFactory(d ->
            new SimpleIntegerProperty(ctrl.inspection().getInspectionId(d.getValue())).asObject());
        colQiPlantation.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.inspection().getPlantationName(d.getValue())));
        colQiGrade.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.inspection().getQualityId(d.getValue())));
        colQiDefect.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.inspection().getDefectDetails(d.getValue())));
        colQiDate.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.inspection().getInspectionDate(d.getValue())));
        colQiInspector.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.inspection().getInspectorName(d.getValue())));
    }

    private void loadInspections() {
        tableInspection.setItems(FXCollections.observableArrayList(ctrl.getAllInspections()));
    }

    @FXML public void handleSave(ActionEvent e) {
        if (cbPlantation.getValue() == null || cbGrade.getValue() == null
                || txtInspector.getText().isBlank() || dpInspectDate.getValue() == null) {
            lblMsg.setStyle("-fx-text-fill:#ff8888;");
            lblMsg.setText("กรุณากรอกข้อมูลให้ครบ");
            return;
        }

        QualityInspectionModel qi = ctrl.inspection().createEmpty();
        ctrl.inspection().setPlantationId(qi,   ctrl.plantation().getPlantationId(cbPlantation.getValue()));
        ctrl.inspection().setQualityId(qi,      cbGrade.getValue());
        ctrl.inspection().setDefectDetails(qi,  txtDefect.getText().trim());
        ctrl.inspection().setInspectorName(qi,  txtInspector.getText().trim());
        ctrl.inspection().setInspectionDate(qi, dpInspectDate.getValue().toString());
        ctrl.inspection().setDate(qi,           java.time.LocalDate.now().toString());

        boolean ok = ctrl.saveInspection(qi);
        lblMsg.setStyle(ok ? "-fx-text-fill:#88ff88;" : "-fx-text-fill:#ff8888;");
        lblMsg.setText(ok ? ctrl.getSuccessMessage() : ctrl.getErrorMessage());
        ctrl.clearMessages();
        if (ok) {
            loadInspections();
            cbPlantation.setValue(null);
            cbGrade.setValue(null);
            txtDefect.clear();
            txtInspector.clear();
            dpInspectDate.setValue(null);
        }
    }

    @FXML public void handleRefresh(ActionEvent e) { loadInspections(); }
}
