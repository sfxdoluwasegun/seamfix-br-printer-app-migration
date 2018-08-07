package com.seamfix.brprinterapp.controller;

import com.seamfix.brprinterapp.config.AppConfig;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class SignInController extends Controller {

    @FXML
    private Label lblVersion;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
//        lblVersion.setText("V" + AppConfig.getAppVersion());
    }

}