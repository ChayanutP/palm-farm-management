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
    @FXML private TableColumn<BillModel, String>  colNumber;
    @FXML private TableColumn<BillModel, String>  colDate;
    @FXML private TableColumn<BillModel, String>  colBuyer;
    @FXML private TableColumn<BillModel, String>  colTotal;
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
        txtQuantity.textProperty().addListener((o,a,b) -> updateTotal());
        txtPriceUnit.textProperty().addListener((o,a,b) -> updateTotal());
    }

    private void setupTable() {
        colBillId.setCellValueFactory(d ->
            new SimpleIntegerProperty(ctrl.bill().getBillId(d.getValue())).asObject());
        colNumber.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.bill().getBillNumber(d.getValue())));
        colDate.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.bill().getSaleDate(d.getValue())));
        colBuyer.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.bill().getBuyerName(d.getValue())));
        colTotal.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.bill().getTotalFormatted(d.getValue())));

        tableStored.getSelectionModel().selectedItemProperty().addListener((obs, old, nw) -> {
            if (nw != null) { selectedBill = nw; clearForm(); loadImage(ctrl.bill().getBillImagePath(nw)); }
        });
    }

    private void loadBills() {
        tableStored.setItems(FXCollections.observableArrayList(ctrl.getPendingRecordBills()));
    }

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
            // ลอง absolute path ก่อน
            File f = new File(path);
            if (f.exists()) { imgBill.setImage(new Image(f.toURI().toString())); return; }
            // ลอง path สัมพัทธ์จาก working directory
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
        if (selectedBill == null) { lblMsg.setText("กรุณาเลือกบิล"); return; }
        try {
            ctrl.bill().setAllBillData(selectedBill,
                txtBillNumber.getText(),
                dpSaleDate.getValue() != null ? dpSaleDate.getValue().toString() : "",
                txtBuyer.getText(),
                Double.parseDouble(txtQuantity.getText()),
                Double.parseDouble(txtPriceUnit.getText()));
        } catch (NumberFormatException ex) { lblMsg.setText("ปริมาณและราคาต้องเป็นตัวเลข"); return; }
        ctrl.saveBillData(selectedBill);
        lblMsg.setText(ctrl.getSuccessMessage().isEmpty() ? ctrl.getErrorMessage() : ctrl.getSuccessMessage());
        ctrl.clearMessages();
        loadBills();
    }



}
