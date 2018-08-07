package com.seamfix.brprinterapp.controller;

import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;

import javafx.event.ActionEvent;
import java.net.URL;
import java.util.ResourceBundle;

public class LandingPageController extends Controller {

    @FXML
    private JFXButton btnSend;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
    }


    public void sendToPrinter(ActionEvent actionEvent) {
        new Alert(Alert.AlertType.CONFIRMATION, "Information has been sent successfully to the printer for printing").showAndWait();
    }

}
