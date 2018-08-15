package com.seamfix.brprinterapp;


import com.seamfix.brprinterapp.gui.BioPrinterStage;
import com.seamfix.brprinterapp.model.Tag;
import com.seamfix.brprinterapp.pojo.rest.TagResponse;
import com.seamfix.brprinterapp.service.DataService;
import com.seamfix.brprinterapp.service.HttpClient;
import com.seamfix.brprinterapp.service.ServiceGenerator;
import com.seamfix.brprinterapp.utils.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by rukevwe on 8/2/2018.
 */
@Log4j
public class App extends Application {


    private Label messageLabel;
    BioPrinterStage splashStage;

    public static void main(String[] args) {
        log.info("start");
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        splashStage = getSplashStage();
        showSplash(splashStage);

        initializeDatabase();

        initDeviceTag();
    }

    private void showSplash(BioPrinterStage splashScreen) {
        Stage stage2 = new Stage(StageStyle.TRANSPARENT);
        splashScreen.show();
        Platform.runLater(stage2::close);
        stage2.showAndWait();
    }

    public void initializeDatabase() {
        try {
            DataService.initialize();
        } catch (Throwable throwable) {
            String errorMessage = "Error while initializing database. (" + throwable.getMessage() + ")";
            AlertUtils.getError(errorMessage).showAndWait();
            System.exit(0);
        }
    }


    private void initDeviceTag() {
        Tag currentTag = DataService.getInstance().getTag();

        if (currentTag != null) {
            log.debug("Current tag: " + currentTag);

            SessionUtils.setCurrentTag(currentTag);
            validTagAction();
        } else {
            log.debug("Current tag is null. Proceed to fresh tagging");

            String uniqueId = CommonUtils.getSerialNumberMachineManufacturer();
            boolean validSerialAndManufacturer = StringUtils.isNotBlank(uniqueId);
            if (!validSerialAndManufacturer) {
                //For some reason, serial and manufacturer cannot be generated on this device. We are going to use an alternative

                //Alternative 1: Use UUID
                uniqueId = UUID.randomUUID().toString();
            }
            final String finalUniqueId = uniqueId;

            Service<TagResponse> service = getTagService(uniqueId);
            service.start();

            service.setOnFailed(event -> {
                log.error("Tagging error ", service.exceptionProperty().getValue());
                String message = "You require a working internet connection for initial setup. Do you want to retry or exit?";
                showRetryExitAlert(message, service);
            });


            service.setOnSucceeded(event -> {
                TagResponse response = service.getValue();
                log.debug("Tag response: " + response);

                if (response != null) {
                    int code = response.getCode();
                    if (code == 0 || code == -12) { //-12 for tag already exist
                        String returnedTag = response.getTag();

                        Tag tag = new Tag(returnedTag, finalUniqueId, new Timestamp(System.currentTimeMillis()));
                        DataService.getInstance().create(tag);
                        SessionUtils.setCurrentTag(tag);

                        validTagAction();
                    } else {
                        String description = response.getDescription();
                        showRetryExitAlert(description, service);
                    }
                }
            });
        }
    }

    private void showRetryExitAlert(String message, Service service) {
        ButtonType retry = new ButtonType("Retry");
        ButtonType exit = new ButtonType("Exit");

        Alert confirm = AlertUtils.getConfirm(message, new ButtonType[]{retry, exit});
        Optional<ButtonType> buttonType = confirm.showAndWait();
        if (buttonType.get() == retry) {
            service.reset();
            service.start();
        } else {
            System.exit(0);
        }
    }

    private void validTagAction() {

        splashStage.hide();
        BioPrinterStage loginStage = FXMLUtil.getLoginStage();
        loginStage.show();
    }

    private Service<TagResponse> getTagService(String uniqueId) {
        return new Service<TagResponse>() {
            @Override
            protected Task<TagResponse> createTask() {
                return new Task<TagResponse>() {
                    @Override
                    protected TagResponse call() throws Exception {
                        return new HttpClient(ServiceGenerator.getInstance()).tagDevice("windows", uniqueId);
                    }
                };
            }
        };
    }

    private BioPrinterStage getSplashStage() {
        BorderPane splash = new BorderPane();
        splash.setPrefWidth(400);
        splash.setPrefHeight(200);
        splash.setCenter(new ImageView(ImageHelper.getImage(ImageHelper.SPLASH_IMAGE)));

        messageLabel = new Label("Loading...");
        splash.setStyle("-fx-background-color: white");
        splash.setBottom(messageLabel);

        BioPrinterStage stage = new BioPrinterStage(new Scene(splash));
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setResizable(false);
        return stage;
    }
}
