package com.palmfarm.controller.accounting;

import com.palmfarm.controller.AccountingController;
import com.palmfarm.model.BillModel;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.*;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class RecordBillViewController implements Initializable {

    @FXML private TableView<BillModel>            tableStored;
    @FXML private TableColumn<BillModel, Integer> colBillId;
    @FXML private TableColumn<BillModel, String>  colUploaded;

    @FXML private ImageView  imgBill;
    @FXML private TextField  txtBillNumber;
    @FXML private DatePicker dpSaleDate;
    @FXML private TextField  txtBuyer;
    @FXML private TextField  txtQuantity;
    @FXML private TextField  txtPriceUnit;
    @FXML private Label      lblTotalCalc;
    @FXML private Label      lblMsg;

    private final AccountingController ctrl = new AccountingController();
    private BillModel selectedBill;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadBills();
        txtQuantity.textProperty().addListener((o, a, b) -> updateTotal());
        txtPriceUnit.textProperty().addListener((o, a, b) -> updateTotal());
    }

    private void setupTable() {
        colBillId.setCellValueFactory(d ->
            new SimpleIntegerProperty(ctrl.bill().getBillId(d.getValue())).asObject());
        colUploaded.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.bill().getUploadedAt(d.getValue())));

        tableStored.getSelectionModel().selectedItemProperty().addListener((obs, old, nw) -> {
            if (nw != null) {
                selectedBill = nw;
                clearForm();               // ฟอร์มว่างเสมอ ผู้ใช้กรอกเองจากรูป
                loadImage(ctrl.bill().getBillImagePath(nw));
                lblMsg.setText("");
            }
        });
    }

    private void loadBills() {
        tableStored.setItems(FXCollections.observableArrayList(ctrl.getStoredBills()));
        selectedBill = null;
        clearForm();
        imgBill.setImage(null);
    }

    /** ล้างฟอร์มทุกครั้งที่เลือกบิลใหม่ */
    private void clearForm() {
        txtBillNumber.clear();
        txtBuyer.clear();
        txtQuantity.clear();
        txtPriceUnit.clear();
        dpSaleDate.setValue(null);
        lblTotalCalc.setText("รวม: —");
    }

    private void loadImage(String path) {
        if (path == null || path.isBlank()) { imgBill.setImage(null); return; }
        try {
            File f = new File(path);
            if (f.exists()) { imgBill.setImage(new Image(f.toURI().toString())); return; }
            File rel = new File(System.getProperty("user.dir"), path);
            if (rel.exists()) { imgBill.setImage(new Image(rel.toURI().toString())); return; }
            imgBill.setImage(null);
        } catch (Exception ignored) { imgBill.setImage(null); }
    }

    private void updateTotal() {
        try {
            double q = Double.parseDouble(txtQuantity.getText());
            double p = Double.parseDouble(txtPriceUnit.getText());
            lblTotalCalc.setText(String.format("รวม: %.2f บาท", q * p));
        } catch (NumberFormatException ex) { lblTotalCalc.setText("รวม: —"); }
    }

    @FXML public void handleSave(ActionEvent e) {
        if (selectedBill == null) { showError("กรุณาเลือกบิล"); return; }
        if (txtBillNumber.getText().isBlank() || txtBuyer.getText().isBlank()
                || dpSaleDate.getValue() == null
                || txtQuantity.getText().isBlank() || txtPriceUnit.getText().isBlank()) {
            showError("กรุณากรอกข้อมูลให้ครบทุกช่อง");
            return;
        }
        try {
            ctrl.bill().setBillNumber(selectedBill,   txtBillNumber.getText().trim());
            ctrl.bill().setSaleDate(selectedBill,     dpSaleDate.getValue().toString());
            ctrl.bill().setBuyerName(selectedBill,    txtBuyer.getText().trim());
            ctrl.bill().setPalmQuantity(selectedBill, Double.parseDouble(txtQuantity.getText()));
            ctrl.bill().setPricePerUnit(selectedBill, Double.parseDouble(txtPriceUnit.getText()));
        } catch (NumberFormatException ex) { showError("ปริมาณและราคาต้องเป็นตัวเลข"); return; }

        boolean ok = ctrl.saveBillData(selectedBill);
        if (ok) {
            lblMsg.setStyle("-fx-text-fill:#88ff88;");
            lblMsg.setText(ctrl.getSuccessMessage());
            ctrl.clearMessages();
            loadBills();
        } else {
            showError(ctrl.getErrorMessage());
            ctrl.clearMessages();
        }
    }

    @FXML public void handleRefresh(ActionEvent e) { loadBills(); lblMsg.setText(""); }

    private void showError(String msg) {
        lblMsg.setStyle("-fx-text-fill:#ff8888;");
        lblMsg.setText(msg);
    }
}
