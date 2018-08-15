package com.seamfix.brprinterapp.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import com.seamfix.brprinterapp.config.AppConfig;
import com.seamfix.brprinterapp.model.Project;
import com.seamfix.brprinterapp.utils.CommonUtils;
import com.seamfix.brprinterapp.utils.SessionUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import lombok.Getter;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

@Getter
public class SelectDefaultProjectAndPrinterController extends Controller {

    @FXML
    private Label lblMessage;
    @FXML
    private JFXComboBox<Project> projectsCombo;
    @FXML
    private JFXTextField txtPrinterName;
    @FXML
    private JFXButton btnSave;

    private Project selectedProject;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        lblMessage.setText("Select default Project and Printer name to proceed");

        projectsCombo.setItems(FXCollections.observableList(new ArrayList<>(SessionUtils.getLoggedInUserProjects())));
    }

    public void performDefaultProjectSave(ActionEvent actionEvent) {
        selectedProject = projectsCombo.getValue();

        if (selectedProject == null) {
            lblMessage.setText("Select a project");
            return;
        }
        AppConfig.saveOrUpdate(CommonUtils.getPriProjectForLoggedInuserKey(), selectedProject.getPId());

        CommonUtils.getWindowForComponent(btnSave).hide();
    }
}
