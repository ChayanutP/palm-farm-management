package com.palmfarm.controller;

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
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class AccountingViewController implements Initializable {
    @FXML private TabPane tabPane;

    @FXML private TableView<BillModel>             tableUnstored;
    @FXML private TableColumn<BillModel, Integer>  colUsBillId;
    @FXML private TableColumn<BillModel, String>   colUsUploaded;
    @FXML private TableColumn<BillModel, String>   colUsStored;
    @FXML private ImageView imgBillPreview;
    @FXML private Label lblStoreMsg;

    @FXML private TableView<BillModel>             tableStored;
    @FXML private TableColumn<BillModel, Integer>  colStBillId;
    @FXML private TableColumn<BillModel, String>   colStNumber;
    @FXML private TableColumn<BillModel, String>   colStDate;
    @FXML private TableColumn<BillModel, String>   colStBuyer;
    @FXML private TableColumn<BillModel, String>   colStTotal;
    @FXML private ImageView imgStoredBill;
    @FXML private TextField  txtBillNumber, txtBuyer, txtQuantity, txtPriceUnit;
    @FXML private DatePicker dpSaleDate;
    @FXML private Label      lblTotalCalc;
    @FXML private Label      lblDataMsg;

    private final AccountingController ctrl = new AccountingController();
    private BillModel selectedUnstored;
    private BillModel selectedStored;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        var empMC = new com.palmfarm.controller.model.EmployeeModelController();

        setupTables();
        loadUnstoredBills();
        loadStoredBills();
        setupAutoCalc();
    }

    private void setupTables() {
        colUsBillId.setCellValueFactory(d ->
            new SimpleIntegerProperty(ctrl.bill().getBillId(d.getValue())).asObject());
        colUsUploaded.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.bill().getUploadedAt(d.getValue())));
        colUsStored.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.bill().isStored(d.getValue()) ? "" : ""));

        colStBillId.setCellValueFactory(d ->
            new SimpleIntegerProperty(ctrl.bill().getBillId(d.getValue())).asObject());
        colStNumber.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.bill().getBillNumber(d.getValue())));
        colStDate.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.bill().getSaleDate(d.getValue())));
        colStBuyer.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.bill().getBuyerName(d.getValue())));
        colStTotal.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.bill().getTotalFormatted(d.getValue())));

        tableUnstored.getSelectionModel().selectedItemProperty().addListener((obs, old, nw) -> {
            if (nw != null) {
                selectedUnstored = nw;
                loadImagePreview(ctrl.bill().getBillImagePath(nw), imgBillPreview);
            }
        });

        tableStored.getSelectionModel().selectedItemProperty().addListener((obs, old, nw) -> {
            if (nw != null) {
                selectedStored = nw;
                loadImagePreview(ctrl.bill().getBillImagePath(nw), imgStoredBill);
                populateForm(nw);
            }
        });
    }

    private void loadImagePreview(String path, ImageView iv) {
        if (path != null && !path.isBlank()) {
            try {
                File f = new File(path);
                if (f.exists()) iv.setImage(new Image(f.toURI().toString()));
            } catch (Exception ignored) {
                iv.setImage(null);
            }
        }
    }

    private void populateForm(BillModel b) {
        txtBillNumber.setText(ctrl.bill().getBillNumber(b) != null ? ctrl.bill().getBillNumber(b) : "");
        txtBuyer.setText(ctrl.bill().getBuyerName(b) != null ? ctrl.bill().getBuyerName(b) : "");
        txtQuantity.setText(ctrl.bill().getPalmQuantity(b) > 0 ? String.valueOf(ctrl.bill().getPalmQuantity(b)) : "");
        txtPriceUnit.setText(ctrl.bill().getPricePerUnit(b) > 0 ? String.valueOf(ctrl.bill().getPricePerUnit(b)) : "");
        if (ctrl.bill().getSaleDate(b) != null && !ctrl.bill().getSaleDate(b).isBlank())
            dpSaleDate.setValue(java.time.LocalDate.parse(ctrl.bill().getSaleDate(b)));
        updateTotalLabel();
    }

    private void setupAutoCalc() {
        txtQuantity.textProperty().addListener((o, a, b) -> updateTotalLabel());
        txtPriceUnit.textProperty().addListener((o, a, b) -> updateTotalLabel());
    }

    private void updateTotalLabel() {
        try {
            double q = Double.parseDouble(txtQuantity.getText());
            double p = Double.parseDouble(txtPriceUnit.getText());
            lblTotalCalc.setText(String.format("รวม: %.2f บาท", q * p));
        } catch (NumberFormatException ex) {
            lblTotalCalc.setText("รวม: —");
        }
    }

    private void loadUnstoredBills() {
        tableUnstored.setItems(FXCollections.observableArrayList(ctrl.getUnstoredBills()));
    }

    private void loadStoredBills() {
        tableStored.setItems(FXCollections.observableArrayList(ctrl.getStoredBills()));
    }

    @FXML public void handleUploadBill(ActionEvent e) {
        FileChooser fc = new FileChooser();
        fc.setTitle("เลือกรูปบิล");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files","*.png","*.jpg","*.jpeg"));
        File file = fc.showOpenDialog(tabPane.getScene().getWindow());
        if (file != null) {
            ctrl.uploadBillImage(file.getAbsolutePath());
            lblStoreMsg.setText(ctrl.getSuccessMessage().isEmpty() ? ctrl.getErrorMessage() : ctrl.getSuccessMessage());
            ctrl.clearMessages();
            loadUnstoredBills();
        }
    }

    @FXML public void handleStoreBill(ActionEvent e) {
        if (selectedUnstored == null) { lblStoreMsg.setText("กรุณาเลือกบิล"); return; }
                ctrl.storeBill(ctrl.bill().getBillId(selectedUnstored));
                lblStoreMsg.setText(ctrl.getSuccessMessage().isEmpty() ? ctrl.getErrorMessage() : ctrl.getSuccessMessage());
                ctrl.clearMessages();
                loadUnstoredBills();
                loadStoredBills();
            
    }

    @FXML public void handleSaveBillData(ActionEvent e) {
        if (selectedStored == null) { lblDataMsg.setText("กรุณาเลือกบิล"); return; }
        try {
            ctrl.bill().setBillNumber(selectedStored,  txtBillNumber.getText());
            ctrl.bill().setSaleDate(selectedStored,    dpSaleDate.getValue() != null ? dpSaleDate.getValue().toString() : "");
            ctrl.bill().setBuyerName(selectedStored,   txtBuyer.getText());
            ctrl.bill().setPalmQuantity(selectedStored,  Double.parseDouble(txtQuantity.getText()));
            ctrl.bill().setPricePerUnit(selectedStored,  Double.parseDouble(txtPriceUnit.getText()));
        } catch (NumberFormatException ex) {
            lblDataMsg.setText("ปริมาณและราคาต้องเป็นตัวเลข"); return;
        }
        ctrl.saveBillData(selectedStored);
        lblDataMsg.setText(ctrl.getSuccessMessage().isEmpty() ? ctrl.getErrorMessage() : ctrl.getSuccessMessage());
        ctrl.clearMessages();
        loadStoredBills();
    }

    @FXML public void handleRefresh(ActionEvent e) { loadUnstoredBills(); loadStoredBills(); }

    @FXML public void handleLogout(ActionEvent e) {
        SessionManager.clearSession();
        javafx.stage.Stage stage = (javafx.stage.Stage) tabPane.getScene().getWindow();
        stage.close();
        new com.palmfarm.window.LoginWindow(new javafx.stage.Stage()).show();
    }
}
