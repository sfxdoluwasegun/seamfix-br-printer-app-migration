package com.seamfix.brprinterapp.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.seamfix.brprinterapp.model.BioUser;
import com.seamfix.brprinterapp.model.Project;
import com.seamfix.brprinterapp.service.DataService;
import com.seamfix.brprinterapp.utils.CommonUtils;
import com.seamfix.brprinterapp.utils.ImageHelper;
import com.seamfix.brprinterapp.utils.SessionUtils;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;

import javafx.event.ActionEvent;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import lombok.Getter;
import lombok.extern.log4j.Log4j;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

@Log4j
public class LandingPageController extends Controller {

    @FXML
    private JFXButton btnSend;
    @FXML
    private JFXComboBox<Project> projectsCombo;
    @FXML
    @Getter
    private BorderPane root;

    private ChangeListener<Project> projectChangeListener;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        initializeProjectsComboBox();
    }


//    public void sendToPrinter(ActionEvent actionEvent) {
//        new Alert(Alert.AlertType.CONFIRMATION, "Information has been sent successfully to the printer for printing").showAndWait();
//    }

    public void setSelectedProject(Project selectedProject) {
        projectChangeListener.changed(projectsCombo.valueProperty(), projectsCombo.getValue(), selectedProject);
    }

    public void initializeProjectsComboBox() {
        BioUser loggedInUser = SessionUtils.getLoggedInUser();
        if (loggedInUser != null) {
            projectsCombo.setItems(FXCollections.observableList(new ArrayList<>(SessionUtils.getLoggedInUserProjects())));

            Image defaultProjLogo = ImageHelper.getImage(ImageHelper.DEFAULT_PROJECT_LOGO);
            Callback<ListView<Project>, ListCell<Project>> cellFactory = new Callback<ListView<Project>, ListCell<Project>>() {
                @Override
                public ListCell<Project> call(ListView param) {
                    return new ListCell<Project>() {
                        @Override
                        protected void updateItem(Project item, boolean empty) {
                            super.updateItem(item, empty);
                            if (item != null) {
                                setText(item.getName());
                                byte[] bytes = CommonUtils.decodeBase64StringToBytes(item.getLogo());
                                Image imageFromByteArray = bytes == null ? defaultProjLogo : ImageHelper.getJavaFXImageFromByteArray(bytes);
                                ImageView imageView = new ImageView(imageFromByteArray);
                                imageView.setFitWidth(20);
                                imageView.setFitHeight(20);
                                setGraphic(imageView);
                                setPrefHeight(30);

                            }
                        }
                    };
                }
            };

            projectsCombo.setConverter(CommonUtils.getProjectStringConverter());

            projectsCombo.setCellFactory(cellFactory);

            projectChangeListener = (observable, oldValue, newValue) -> {
                root.setDisable(true);
                projectsCombo.setValue(newValue);
                SessionUtils.setCurrentProject(newValue);
            };
            projectsCombo.getSelectionModel().selectedItemProperty().addListener(projectChangeListener);

            String primaryProjectPid = CommonUtils.getPrimaryProjectForLoggedInuser();
            projectChangeListener.changed(projectsCombo.valueProperty(), projectsCombo.getValue(), DataService.getInstance().getProjectByPid(primaryProjectPid));
        }
    }

}
