package com.seamfix.brprinterapp.utils;


import com.seamfix.brprinterapp.controller.Controller;
import com.seamfix.brprinterapp.gui.BioPrinterStage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.util.Pair;
import lombok.extern.log4j.Log4j;

import javafx.stage.WindowEvent;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Created by rukevwe on 3/30/2017.
 */
@Log4j
public class FXMLUtil {


    //resources
    public static final String SIGNIN_FXML = "/com/seamfix/brprinterapp/gui/sign-in.fxml";
    public static final String LANDING_PAGE_FXML = "/com/seamfix/brprinterapp/gui/landing-page.fxml";
    public static final String SELECT_DEFAULT_PROJECT_FXML = "/com/seamfix/brprinterapp/gui/select-default-project.fxml";
    public static final String PRINTER_NAME_FXML = "/com/seamfix/brprinterapp/gui/enter-printer-name.fxml";


    public static Pair<Parent, Controller> loadParentControllerPair(String resource) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("com/seamfix/brprinterapp/i18n/br-printer", new Locale("en", "EN"));
            URL url = FXMLUtil.class.getResource(resource);

            FXMLLoader loader = new FXMLLoader(url, bundle);
            Parent parent = loader.load();
            addGlobalStyle(parent);

            return new Pair<>(parent, loader.getController());
        } catch (final Exception e) {
            log.error(e.getMessage());
            log.error("Error while loading " + resource, e);
        }

        return null;
    }
    public static void addGlobalStyle(Parent root) {
        String css = FXMLUtil.class.getResource("/com/seamfix/brprinterapp/css/css.css").toExternalForm();
        root.getStylesheets().add(css);
    }

    public static BioPrinterStage getLoginStage() {
        BioPrinterStage loginStage = new BioPrinterStage(loadScene(SIGNIN_FXML), "BioRegistra Printer App");
        loginStage.setResizable(false);
        loginStage.setOnCloseRequest(FXMLUtil:: handleLoginStageClosed);
        return loginStage;
    }

    public static Scene loadScene(String resource) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("com/seamfix/brprinterapp/i18n/br-printer", new Locale("en", "EN"));
            URL url = FXMLUtil.class.getResource(resource);

            Parent parent = FXMLLoader.load(url, bundle);
            addGlobalStyle(parent);
            return new Scene(parent);
        } catch (IOException e) {
            log.error("Error while loading " + resource, e);
        }

        return null;
    }
    private static void handleLoginStageClosed(WindowEvent event) {
        Optional<ButtonType> buttonType = AlertUtils.getConfirm("Are you sure you want to exit?").showAndWait();
        if (buttonType.get() != ButtonType.OK) {
            event.consume();
        } else {
            System.exit(0);
        }
    }
}
