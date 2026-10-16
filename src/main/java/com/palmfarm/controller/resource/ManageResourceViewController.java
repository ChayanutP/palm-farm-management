package com.palmfarm.controller.resource;

import com.palmfarm.controller.ResourceController;
import com.palmfarm.model.ResourceModel;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.util.ResourceBundle;

public class ManageResourceViewController implements Initializable {

    @FXML private TableView<ResourceModel>            tableResource;
    @FXML private TableColumn<ResourceModel, Integer> colResId;
    @FXML private TableColumn<ResourceModel, String>  colResName;
    @FXML private TableColumn<ResourceModel, String>  colResType;
    @FXML private TableColumn<ResourceModel, String>  colResStatus;

    private final ResourceController ctrl = new ResourceController();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadResources();
    }

    private void setupTable() {
        colResId.setCellValueFactory(d ->
            new SimpleIntegerProperty(ctrl.resource().getResourceId(d.getValue())).asObject());
        colResName.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.resource().getResourceName(d.getValue())));
        colResType.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.resource().getResourceType(d.getValue())));
        colResStatus.setCellValueFactory(d ->
            new SimpleStringProperty(ctrl.resource().getStatus(d.getValue())));
    }

    private void loadResources() {
        tableResource.setItems(FXCollections.observableArrayList(ctrl.getAllResources()));
    }

    @FXML public void handleRefresh(ActionEvent e) { loadResources(); }
}
