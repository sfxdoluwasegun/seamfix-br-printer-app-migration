package com.seamfix.brprinterapp.utils;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Created by rukevwe on 8/7/2018
 */
public class AlertUtils {

    public static Alert getInfoAlert(String message) {
        return new Alert(Alert.AlertType.INFORMATION, message);
    }
    public static Alert getError(String message) {
        return new Alert(Alert.AlertType.ERROR, message);
    }
    public static Alert getConfirm(String message) {
        return new Alert(Alert.AlertType.CONFIRMATION, message);
    }

    public static Alert getConfirm(String message, ButtonType[] types) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message);
        alert.getButtonTypes().clear();
        alert.getButtonTypes().addAll(types);

        // so we can dismiss the dialog
        alert.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        Node closeButton = alert.getDialogPane().lookupButton(ButtonType.CLOSE);
        closeButton.managedProperty().bind(closeButton.visibleProperty());
        closeButton.setVisible(false);

        return alert;
    }
}
