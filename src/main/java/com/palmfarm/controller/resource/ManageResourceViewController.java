package com.palmfarm.controller.resource;

import com.palmfarm.controller.ResourceController;
import com.palmfarm.model.ResourceModel;
import com.palmfarm.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.util.ResourceBundle;

public class ManageResourceViewController implements Initializable {
    @FXML private TextField    txtResName;
    @FXML private ComboBox<String> cbResType;
    @FXML private Label        lblMsg;
    @FXML private TableView<ResourceModel>            tableResource;
    @FXML private TableColumn<ResourceModel, Integer> colResId;
    @FXML private TableColumn<ResourceModel, String>  colResName;
    @FXML private TableColumn<ResourceModel, String>  colResType;
    @FXML private TableColumn<ResourceModel, String>  colResStatus;

    private final ResourceController ctrl = new ResourceController();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbResType.setItems(FXCollections.observableArrayList("VEHICLE","EQUIPMENT","OTHER"));
        setupTable();
        loadResources();
    }

    private void setupTable() {
        colResId.setCellValueFactory(d ->
            new javafx.beans.property.SimpleIntegerProperty(ctrl.resource().getResourceId(d.getValue())).asObject());
        colResName.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(ctrl.resource().getResourceName(d.getValue())));
        colResType.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(ctrl.resource().getResourceType(d.getValue())));
        colResStatus.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(ctrl.resource().getStatus(d.getValue())));
    }

    private void loadResources() {
        tableResource.setItems(FXCollections.observableArrayList(ctrl.getAllResources()));
    }

    @FXML public void handleAddResource(ActionEvent e) {
        if (txtResName.getText().isBlank() || cbResType.getValue() == null) {
            lblMsg.setText("กรุณากรอกชื่อและประเภท"); return;
        }
        ResourceModel r = ctrl.resource().createEmpty();
        ctrl.resource().setResourceName(r, txtResName.getText().trim());
        ctrl.resource().setResourceType(r, cbResType.getValue());
        ctrl.addResource(r);
        lblMsg.setText(ctrl.getSuccessMessage().isEmpty() ? ctrl.getErrorMessage() : ctrl.getSuccessMessage());
        ctrl.clearMessages();
        loadResources();
        txtResName.clear();
    }

    @FXML public void handleRefresh(ActionEvent e) { loadResources(); }
}
