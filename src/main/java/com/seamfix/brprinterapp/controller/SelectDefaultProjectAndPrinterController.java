package com.seamfix.brprinterapp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class SelectDefaultProjectAndPrinterController extends Controller {

    @FXML
    private Label lblMessage;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        lblMessage.setText("Select default Project and Printer name to proceed");
    }
}
