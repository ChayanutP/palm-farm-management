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
import java.net.URL;
import java.util.ResourceBundle;

public class StoreBillViewController implements Initializable {
    @FXML private TableView<BillModel>            tableUnstored;
    @FXML private TableColumn<BillModel, Integer> colBillId;
    @FXML private TableColumn<BillModel, String>  colUploaded;
    @FXML private TableColumn<BillModel, String>  colStored;
    @FXML private ImageView imgPreview;
    @FXML private Label     lblMsg;

    private final AccountingController ctrl = new AccountingController();
    private BillModel selectedBill;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        var empMC = new com.palmfarm.controller.model.EmployeeModelController();
        setupTable();
        loadBills();
    }

    private void setupTable() {
        colBillId.setCellValueFactory(d ->
            new SimpleIntegerProperty(ctrl.bill().getBillId(d.getValue())).asObject());
        colUploaded.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.bill().getUploadedAt(d.getValue())));
        colStored.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.bill().isStored(d.getValue()) ? "" : ""));

        tableUnstored.getSelectionModel().selectedItemProperty().addListener((obs, old, nw) -> {
            if (nw != null) {
                selectedBill = nw;
                loadImage(ctrl.bill().getBillImagePath(nw));
            }
        });
    }

    private void loadBills() {
        tableUnstored.setItems(FXCollections.observableArrayList(ctrl.getUnstoredBills()));
    }

    private void loadImage(String path) {
        if (path != null && !path.isBlank()) {
            try {
                // ลอง absolute path ก่อน
                java.io.File f = new java.io.File(path);
                if (f.exists()) {
                    imgPreview.setImage(new Image(f.toURI().toString()));
                    return;
                }
                // ถ้าไม่พบ ลอง path สัมพัทธ์จาก working directory
                java.io.File rel = new java.io.File(System.getProperty("user.dir"), path);
                if (rel.exists()) {
                    imgPreview.setImage(new Image(rel.toURI().toString()));
                    return;
                }
                imgPreview.setImage(null);
            } catch (Exception ignored) { imgPreview.setImage(null); }
        }
    }

    @FXML public void handleStore(ActionEvent e) {
        if (selectedBill == null) { lblMsg.setText("กรุณาเลือกบิล"); return; }
                ctrl.storeBill(ctrl.bill().getBillId(selectedBill));
                lblMsg.setText(ctrl.getSuccessMessage().isEmpty() ? ctrl.getErrorMessage() : ctrl.getSuccessMessage());
                ctrl.clearMessages();
                loadBills();
            
    }

    @FXML public void handleRefresh(ActionEvent e) { loadBills(); }
}
