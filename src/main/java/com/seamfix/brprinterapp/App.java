package com.seamfix.brprinterapp;


import com.seamfix.brprinterapp.controller.Controller;
import com.seamfix.brprinterapp.controller.LandingPageController;
import com.seamfix.brprinterapp.controller.SignInController;
import com.seamfix.brprinterapp.gui.BioPrinterStage;
import com.seamfix.brprinterapp.service.DataService;
import com.seamfix.brprinterapp.utils.AlertUtils;
import com.seamfix.brprinterapp.utils.FXMLUtil;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;
import lombok.extern.log4j.Log4j;

/**
 * Created by rukevwe on 8/2/2018.
 */
@Log4j
public class App extends Application {


    public static void main(String[] args) {
        log.info("start");
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        initializeDatabase();
        BioPrinterStage loginStage = FXMLUtil.getLoginStage();
        loginStage.show();
     }

     public void initializeDatabase() {
         try {
            DataService.initialize();
        } catch(Throwable throwable) {
            String errorMessage = "Error while initializing database. (" + throwable.getMessage() + ")";
            AlertUtils.getError(errorMessage).showAndWait();
            System.exit(0);
        }
     }
}
