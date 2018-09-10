package com.seamfix.brprinterapp.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private ResourceBundle resources;

    @FXML
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
    }

    public String getString(String key) {
        return resources.getString(key);
    }
}