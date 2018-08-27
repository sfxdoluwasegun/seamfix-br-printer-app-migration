package com.seamfix.brprinterapp.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.seamfix.brprinterapp.config.AppConfig;
import com.seamfix.brprinterapp.config.ConfigKeys;
import com.seamfix.brprinterapp.utils.CommonUtils;
import javafx.fxml.FXML;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import javafx.event.ActionEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class EnterPrinterNameController extends Controller {

    @FXML
    private JFXButton btnSave;

    @FXML
    private JFXTextField txtPrinterName;

    @Getter
    private String printer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
    }



    public void savePrinter(ActionEvent actionEvent) {
        String printerName = txtPrinterName.getText();

        if (StringUtils.isBlank(printerName)) {
            return;
        }
        printer = printerName;
        AppConfig.saveOrUpdate(ConfigKeys.LAST_PRINTER_NAME, printer);

        CommonUtils.getWindowForComponent(btnSave).hide();
    }
}
