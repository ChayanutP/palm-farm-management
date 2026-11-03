package com.palmfarm.controller.resource;

import com.palmfarm.controller.ResourceController;
import com.palmfarm.model.ResourceModel;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ManageResourceViewController implements Initializable {

    // ── Vehicle ──
    @FXML private TableView<ResourceModel>            tableVehicle;
    @FXML private TableColumn<ResourceModel, Integer> colVId;
    @FXML private TableColumn<ResourceModel, String>  colVPlate;
    @FXML private TableColumn<ResourceModel, String>  colVSize;
    @FXML private TableColumn<ResourceModel, String>  colVSeat;
    @FXML private TableColumn<ResourceModel, String>  colVStatus;
    @FXML private TextField txtVPlate;
    @FXML private TextField txtVSize;
    @FXML private TextField txtVSeat;
    @FXML private Label     lblVMsg;

    // ── Equipment ──
    @FXML private TableView<ResourceModel>            tableEquip;
    @FXML private TableColumn<ResourceModel, Integer> colEId;
    @FXML private TableColumn<ResourceModel, String>  colEEquipId;
    @FXML private TableColumn<ResourceModel, String>  colEName;
    @FXML private TableColumn<ResourceModel, String>  colEStatus;
    @FXML private TextField txtEEquipId;
    @FXML private TextField txtEName;
    @FXML private Label     lblEMsg;

    private final ResourceController ctrl = new ResourceController();
    private ResourceModel selectedVehicle = null;
    private ResourceModel selectedEquip   = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colVId.setCellValueFactory(d -> new SimpleIntegerProperty(ctrl.resource().getResourceId(d.getValue())).asObject());
        colVPlate.setCellValueFactory(d -> new SimpleStringProperty(ctrl.resource().getLicensePlate(d.getValue())));
        colVSize.setCellValueFactory(d -> new SimpleStringProperty(ctrl.resource().getVehicleSize(d.getValue())));
        colVSeat.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(ctrl.resource().getSeatCount(d.getValue()))));
        colVStatus.setCellValueFactory(d -> new SimpleStringProperty(ctrl.resource().getStatus(d.getValue())));
        tableVehicle.getSelectionModel().selectedItemProperty().addListener((obs, old, nw) -> {
            if (nw != null) {
                selectedVehicle = nw;
                txtVPlate.setText(ctrl.resource().getLicensePlate(nw));
                txtVSize.setText(ctrl.resource().getVehicleSize(nw));
                txtVSeat.setText(String.valueOf(ctrl.resource().getSeatCount(nw)));
                lblVMsg.setText("");
            }
        });

        colEId.setCellValueFactory(d -> new SimpleIntegerProperty(ctrl.resource().getResourceId(d.getValue())).asObject());
        colEEquipId.setCellValueFactory(d -> new SimpleStringProperty(ctrl.resource().getEquipId(d.getValue())));
        colEName.setCellValueFactory(d -> new SimpleStringProperty(ctrl.resource().getEquipmentName(d.getValue())));
        colEStatus.setCellValueFactory(d -> new SimpleStringProperty(ctrl.resource().getStatus(d.getValue())));
        tableEquip.getSelectionModel().selectedItemProperty().addListener((obs, old, nw) -> {
            if (nw != null) {
                selectedEquip = nw;
                txtEEquipId.setText(ctrl.resource().getEquipId(nw));
                txtEName.setText(ctrl.resource().getEquipmentName(nw));
                lblEMsg.setText("");
            }
        });
        loadAll();
    }

    private void loadAll() {
        var all = ctrl.getAllResources();
        tableVehicle.setItems(FXCollections.observableArrayList(
            all.stream().filter(r -> "VEHICLE".equals(ctrl.resource().getResourceType(r))).toList()));
        tableEquip.setItems(FXCollections.observableArrayList(
            all.stream().filter(r -> "EQUIPMENT".equals(ctrl.resource().getResourceType(r))).toList()));
        selectedVehicle = null; selectedEquip = null;
    }

    // ── Vehicle validation ──
    private String validateVehicle(String plate, String seatStr, boolean isAdd) {
        if (plate.isBlank())  return "กรุณากรอกทะเบียน";
        if (txtVSize.getText().isBlank()) return "กรุณากรอกขนาดรถ";
        int seat = 2;
        if (!seatStr.isBlank()) {
            try { seat = Integer.parseInt(seatStr); }
            catch (NumberFormatException ex) { return "จำนวนที่นั่งต้องเป็นตัวเลข"; }
            if (seat <= 0) return "จำนวนที่นั่งต้องมากกว่า 0";
        }
        // ตรวจทะเบียนซ้ำ
        List<ResourceModel> all = ctrl.getAllResources();
        for (ResourceModel r : all) {
            if (!"VEHICLE".equals(ctrl.resource().getResourceType(r))) continue;
            if (plate.equalsIgnoreCase(ctrl.resource().getLicensePlate(r))) {
                if (!isAdd && selectedVehicle != null &&
                    ctrl.resource().getResourceId(r) == ctrl.resource().getResourceId(selectedVehicle))
                    continue;
                return "ทะเบียน \"" + plate + "\" มีอยู่แล้วในระบบ";
            }
        }
        return null;
    }

    // ── Equipment validation ──
    private String validateEquip(String name, boolean isAdd) {
        if (name.isBlank()) return "กรุณากรอกชื่ออุปกรณ์";
        // ตรวจชื่อซ้ำ
        List<ResourceModel> all = ctrl.getAllResources();
        for (ResourceModel r : all) {
            if (!"EQUIPMENT".equals(ctrl.resource().getResourceType(r))) continue;
            if (name.equalsIgnoreCase(ctrl.resource().getEquipmentName(r))) {
                if (!isAdd && selectedEquip != null &&
                    ctrl.resource().getResourceId(r) == ctrl.resource().getResourceId(selectedEquip))
                    continue;
                return "ชื่ออุปกรณ์ \"" + name + "\" มีอยู่แล้วในระบบ";
            }
        }
        return null;
    }

    // ── Vehicle actions ──
    @FXML public void handleAddVehicle(ActionEvent e) {
        String plate = txtVPlate.getText().trim();
        String seat  = txtVSeat.getText().trim();
        String err   = validateVehicle(plate, seat, true);
        if (err != null) { vError(err); return; }

        ResourceModel r = ctrl.resource().createEmpty();
        ctrl.resource().setAllNewVehicle(r, plate,
            txtVSize.getText().trim(),
            seat.isBlank() ? 2 : Integer.parseInt(seat));
        boolean ok = ctrl.addVehicle(r);
        lblVMsg.setStyle(ok ? "-fx-text-fill:#88ff88;" : "-fx-text-fill:#ff8888;");
        lblVMsg.setText(ok ? ctrl.getSuccessMessage() : ctrl.getErrorMessage());
        ctrl.clearMessages();
        if (ok) { loadAll(); txtVPlate.clear(); txtVSize.clear(); txtVSeat.clear(); }
    }

    @FXML public void handleUpdateVehicle(ActionEvent e) {
        if (selectedVehicle == null) { vError("กรุณาเลือกรถจากตาราง"); return; }
        String plate = txtVPlate.getText().trim();
        String seat  = txtVSeat.getText().trim();
        String err   = validateVehicle(plate, seat, false);
        if (err != null) { vError(err); return; }

        ctrl.resource().setAllVehicle(selectedVehicle, plate,
            txtVSize.getText().trim(),
            seat.isBlank() ? 2 : Integer.parseInt(seat));
        boolean ok = ctrl.updateVehicle(selectedVehicle);
        lblVMsg.setStyle(ok ? "-fx-text-fill:#88ff88;" : "-fx-text-fill:#ff8888;");
        lblVMsg.setText(ok ? ctrl.getSuccessMessage() : ctrl.getErrorMessage());
        ctrl.clearMessages(); if (ok) loadAll();
    }

    // ── Equipment actions ──
    @FXML public void handleAddEquip(ActionEvent e) {
        String name = txtEName.getText().trim();
        String err  = validateEquip(name, true);
        if (err != null) { eError(err); return; }

        ResourceModel r = ctrl.resource().createEmpty();
        ctrl.resource().setAllNewEquipment(r,
            txtEEquipId.getText().trim(), name);
        boolean ok = ctrl.addEquipment(r);
        lblEMsg.setStyle(ok ? "-fx-text-fill:#88ff88;" : "-fx-text-fill:#ff8888;");
        lblEMsg.setText(ok ? ctrl.getSuccessMessage() : ctrl.getErrorMessage());
        ctrl.clearMessages();
        if (ok) { loadAll(); txtEEquipId.clear(); txtEName.clear(); }
    }

    @FXML public void handleUpdateEquip(ActionEvent e) {
        if (selectedEquip == null) { eError("กรุณาเลือกอุปกรณ์จากตาราง"); return; }
        String name = txtEName.getText().trim();
        String err  = validateEquip(name, false);
        if (err != null) { eError(err); return; }

        ctrl.resource().setAllEquipment(selectedEquip,
            txtEEquipId.getText().trim(), name);
        boolean ok = ctrl.updateEquipment(selectedEquip);
        lblEMsg.setStyle(ok ? "-fx-text-fill:#88ff88;" : "-fx-text-fill:#ff8888;");
        lblEMsg.setText(ok ? ctrl.getSuccessMessage() : ctrl.getErrorMessage());
        ctrl.clearMessages(); if (ok) loadAll();
    }


    @FXML public void handleExportVehicle(ActionEvent e) {
        var list = tableVehicle.getItems();
        if (list.isEmpty()) { vError("ไม่มีข้อมูลรถ"); return; }
        int[] widths = {6, 20, 24, 8, 14};
        String[] headers = {"ID", "ทะเบียน", "ขนาดรถ", "ที่นั่ง", "สถานะ"};
        StringBuilder sb = com.palmfarm.util.ReportExporter.header(
            "Vehicle Report / รายงานรถยนต์ทั้งหมด");
        sb.append(com.palmfarm.util.ReportExporter.separator(widths)).append("\n");
        sb.append(com.palmfarm.util.ReportExporter.row(headers, widths)).append("\n");
        sb.append(com.palmfarm.util.ReportExporter.separator(widths)).append("\n");
        for (var r : list) {
            String[] row = {
                String.valueOf(ctrl.resource().getResourceId(r)),
                ctrl.resource().getLicensePlate(r) != null ? ctrl.resource().getLicensePlate(r) : "",
                ctrl.resource().getVehicleSize(r) != null ? ctrl.resource().getVehicleSize(r) : "",
                String.valueOf(ctrl.resource().getSeatCount(r)),
                ctrl.resource().getStatus(r)
            };
            sb.append(com.palmfarm.util.ReportExporter.row(row, widths)).append("\n");
        }
        sb.append(com.palmfarm.util.ReportExporter.separator(widths)).append("\n");
        sb.append(com.palmfarm.util.ReportExporter.footer(list.size()));
        boolean ok = com.palmfarm.util.ReportExporter.saveToFile(
            tableVehicle.getScene().getWindow(), "vehicle_report.txt", sb.toString());
        lblVMsg.setStyle(ok ? "-fx-text-fill:#88ff88;" : "-fx-text-fill:#aaaaaa;");
        lblVMsg.setText(ok ? "Export สำเร็จ" : "ยกเลิก");
    }

    @FXML public void handleExportEquip(ActionEvent e) {
        var list = tableEquip.getItems();
        if (list.isEmpty()) { eError("ไม่มีข้อมูลอุปกรณ์"); return; }
        int[] widths = {6, 16, 34, 14};
        String[] headers = {"ID", "รหัสอุปกรณ์", "ชื่ออุปกรณ์", "สถานะ"};
        StringBuilder sb = com.palmfarm.util.ReportExporter.header(
            "Equipment Report / รายงานอุปกรณ์เครื่องมือทั้งหมด");
        sb.append(com.palmfarm.util.ReportExporter.separator(widths)).append("\n");
        sb.append(com.palmfarm.util.ReportExporter.row(headers, widths)).append("\n");
        sb.append(com.palmfarm.util.ReportExporter.separator(widths)).append("\n");
        for (var r : list) {
            String[] row = {
                String.valueOf(ctrl.resource().getResourceId(r)),
                ctrl.resource().getEquipId(r) != null ? ctrl.resource().getEquipId(r) : "",
                ctrl.resource().getEquipmentName(r) != null ? ctrl.resource().getEquipmentName(r) : "",
                ctrl.resource().getStatus(r)
            };
            sb.append(com.palmfarm.util.ReportExporter.row(row, widths)).append("\n");
        }
        sb.append(com.palmfarm.util.ReportExporter.separator(widths)).append("\n");
        sb.append(com.palmfarm.util.ReportExporter.footer(list.size()));
        boolean ok = com.palmfarm.util.ReportExporter.saveToFile(
            tableEquip.getScene().getWindow(), "equipment_report.txt", sb.toString());
        lblEMsg.setStyle(ok ? "-fx-text-fill:#88ff88;" : "-fx-text-fill:#aaaaaa;");
        lblEMsg.setText(ok ? "Export สำเร็จ" : "ยกเลิก");
    }

    @FXML public void handleRefresh(ActionEvent e) { loadAll(); lblVMsg.setText(""); lblEMsg.setText(""); }

    private void vError(String msg) { lblVMsg.setStyle("-fx-text-fill:#ff8888;"); lblVMsg.setText(msg); }
    private void eError(String msg) { lblEMsg.setStyle("-fx-text-fill:#ff8888;"); lblEMsg.setText(msg); }
}
