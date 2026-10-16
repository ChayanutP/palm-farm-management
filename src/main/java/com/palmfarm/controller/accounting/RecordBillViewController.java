package com.palmfarm.controller.accounting;

import com.palmfarm.controller.AccountingController;
import com.palmfarm.model.BillModel;
import com.palmfarm.util.SessionManager;
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
        var empMC = new com.palmfarm.controller.model.EmployeeModelController();
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
            if (nw != null) { selectedBill = nw; populateForm(nw); loadImage(ctrl.bill().getBillImagePath(nw)); }
        });
    }

    private void loadBills() {
        tableStored.setItems(FXCollections.observableArrayList(ctrl.getStoredBills()));
    }

    private void populateForm(BillModel b) {
        txtBillNumber.setText(ctrl.bill().getBillNumber(b) != null ? ctrl.bill().getBillNumber(b) : "");
        txtBuyer.setText(ctrl.bill().getBuyerName(b) != null ? ctrl.bill().getBuyerName(b) : "");
        txtQuantity.setText(ctrl.bill().getPalmQuantity(b) > 0 ? String.valueOf(ctrl.bill().getPalmQuantity(b)) : "");
        txtPriceUnit.setText(ctrl.bill().getPricePerUnit(b) > 0 ? String.valueOf(ctrl.bill().getPricePerUnit(b)) : "");
        if (ctrl.bill().getSaleDate(b) != null && !ctrl.bill().getSaleDate(b).isBlank())
            dpSaleDate.setValue(java.time.LocalDate.parse(ctrl.bill().getSaleDate(b)));
        updateTotal();
    }

    private void loadImage(String path) {
        if (path != null && !path.isBlank()) {
            try { File f = new File(path); if (f.exists()) imgBill.setImage(new Image(f.toURI().toString())); }
            catch (Exception ignored) { imgBill.setImage(null); }
        }
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
            ctrl.bill().setBillNumber(selectedBill,  txtBillNumber.getText());
            ctrl.bill().setSaleDate(selectedBill,    dpSaleDate.getValue() != null ? dpSaleDate.getValue().toString() : "");
            ctrl.bill().setBuyerName(selectedBill,   txtBuyer.getText());
            ctrl.bill().setPalmQuantity(selectedBill,  Double.parseDouble(txtQuantity.getText()));
            ctrl.bill().setPricePerUnit(selectedBill,  Double.parseDouble(txtPriceUnit.getText()));
        } catch (NumberFormatException ex) { lblMsg.setText("ปริมาณและราคาต้องเป็นตัวเลข"); return; }
        ctrl.saveBillData(selectedBill);
        lblMsg.setText(ctrl.getSuccessMessage().isEmpty() ? ctrl.getErrorMessage() : ctrl.getSuccessMessage());
        ctrl.clearMessages();
        loadBills();
    }

    @FXML public void handleRefresh(ActionEvent e) { loadBills(); }
}
