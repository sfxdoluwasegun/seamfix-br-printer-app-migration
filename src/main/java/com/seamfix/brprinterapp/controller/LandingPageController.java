package com.seamfix.brprinterapp.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.seamfix.brprinterapp.FargoPrinter.PrinterClass;
import com.seamfix.brprinterapp.gui.BioPrinterDialog;
import com.seamfix.brprinterapp.model.BioUser;
import com.seamfix.brprinterapp.model.Project;
import com.seamfix.brprinterapp.pojo.GenerateIDCard;
import com.seamfix.brprinterapp.pojo.rest.GenerateIDCardRequest;
import com.seamfix.brprinterapp.pojo.rest.GenerateIDCardResponse;
import com.seamfix.brprinterapp.service.DataService;
import com.seamfix.brprinterapp.service.HttpClient;
import com.seamfix.brprinterapp.service.ServiceGenerator;
import com.seamfix.brprinterapp.utils.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import javafx.event.ActionEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import javafx.util.Pair;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;

@Log4j
public class LandingPageController extends Controller {

    @FXML
    private JFXButton btnSend;
    @FXML
    private JFXComboBox<Project> projectsCombo;
    @FXML
    @Getter
    private BorderPane root;
    @FXML
    private JFXButton btnChange;
    @FXML
    private Label lblPrinter;
    @FXML
    private TextArea txtEnterSysIds;
    @FXML
    private ImageView imgStatus;
    @FXML
    private ImageView logoutImage;
    @FXML
    private Label lblPrint;

    private ChangeListener<Project> projectChangeListener;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logoutImage.setImage(ImageHelper.getImage(ImageHelper.LOGOUT_ICON));
        initializePrinterName();
        initializeProjectsComboBox();
        super.initialize(location, resources);
        logoutImage.setOnMouseClicked(event -> performLogout());


    }


    public void sendToPrinter(ActionEvent actionEvent) {
        btnSend.setDisable(true);
        String systemIds = txtEnterSysIds.getText();
        String[] lists = systemIds.split(",");
        ArrayList<String> uniqueIds = new ArrayList<>(Arrays.asList(lists));
        if (uniqueIds.size() >= 5) {
            AlertUtils.getError("You cannot enter more than 5 System Generated Ids").show();
            btnSend.setDisable(false);
            return;
        }
        if (uniqueIds.get(0).equals("")) {
            uniqueIds = null;
        }
        String pId = SessionUtils.getCurrentProject().getPId();
        GenerateIDCardRequest generateIDCardRequest = new GenerateIDCardRequest(pId, uniqueIds);
        sendIDCardRequest(generateIDCardRequest);
        log.info("Sending GeneratedIDCard Request");

    }


    public void sendIDCardRequest(GenerateIDCardRequest request) {
        Task<GenerateIDCardResponse> idCardResponseTask = new Task<GenerateIDCardResponse>() {
            @Override
            protected GenerateIDCardResponse call() throws Exception {
                return new HttpClient(ServiceGenerator.getInstance()).generateIDCard(request);
            }

            @Override
            protected void running() {
                super.running();
                imgStatus.setImage(ImageHelper.getLoadingImage());
            }

            @Override
            protected void failed() {
                super.failed();
                imgStatus.setImage(ImageHelper.getErrorImage());
                log.error("Error with login", exceptionProperty().getValue());
                AlertUtils.getConfirm("You require a working internet connection to login").showAndWait();
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                imgStatus.setImage(ImageHelper.getOKImage());
                GenerateIDCardResponse response = getValue();

                if (response != null) {
                    int responseCode = response.getCode();
                    ArrayList<GenerateIDCard> responseList = response.getIdcards();
                    boolean availableIds = responseList.isEmpty();
                    String responseDescription = response.getDescription();
                    lblPrint.setText(responseDescription);
                    if (responseCode == 0) {
                        for (GenerateIDCard generateIDCard : responseList) {
                            if (!availableIds) {
                                String frontImage = generateIDCard.getFrontView();
                                File images = new File("IdCardImages");
                                images.mkdirs();
                                try {
                                    BufferedImage bImage2 = ImageIO.read(writeImage(frontImage));
                                    ImageIO.write(bImage2, "jpg", new File(images, "front.jpg"));
                                    System.out.println("image created");
                                } catch (Exception e) {
                                    log.info(e.getMessage());
                                }
                                String backImage = generateIDCard.getBackView();
                                try {
                                    BufferedImage bImage2 = ImageIO.read(writeImage(backImage));
                                    ImageIO.write(bImage2, "jpg", new File(images,"back.jpg"));
                                    System.out.println("image created");
                                } catch (Exception e) {
                                    log.info(e.getMessage());
                                }
                             printerTask();
                            }

                        }
                    }


                }
            }

            @Override
            protected void done() {
                btnSend.setDisable(false);
            }

        };


        new Thread(idCardResponseTask).start();
    }

    public ByteArrayInputStream writeImage(String base64String) {

        byte[] bytes = CommonUtils.decodeBase64StringToBytes(base64String);

        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);

        return bis;
    }

    private void printerTask() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                PrinterClass.startPrinting(lblPrinter.getText());
            }
        };
        log.info("Started print task");
        new Thread(runnable).start();
    }
    public void setSelectedPrinterName(String printerName) {
        lblPrinter.setText(printerName);
    }

    public void initializePrinterName() {
        String lastPrinterName = CommonUtils.getLastPrinterName();
        if (lastPrinterName != null) {
            lblPrinter.setText(lastPrinterName);
        }
    }

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
                projectsCombo.setValue(newValue);
                SessionUtils.setCurrentProject(newValue);
            };
            projectsCombo.getSelectionModel().selectedItemProperty().addListener(projectChangeListener);

            String primaryProjectPid = CommonUtils.getPrimaryProjectForLoggedInuser();
            projectChangeListener.changed(projectsCombo.valueProperty(), projectsCombo.getValue(), DataService.getInstance().getProjectByPid(primaryProjectPid));
        }
    }

    private void performLogout() {
        Optional<ButtonType> buttonType = AlertUtils.getConfirm("Are you sure you want to logout?").showAndWait();
        if (buttonType.get() == ButtonType.OK) {
            CommonUtils.performLogout(CommonUtils.getMainWindow());
        }
    }

    public void changePrinter(ActionEvent actionEvent) {
        Pair<Parent, Controller> pair = FXMLUtil.loadParentControllerPair(FXMLUtil.PRINTER_NAME_FXML);
        Parent parent = pair.getKey();
        EnterPrinterNameController controller = (EnterPrinterNameController) pair.getValue();
        BioPrinterDialog dialog = new BioPrinterDialog(CommonUtils.getMainWindow(), new Scene(parent), "Enter Printer Name", false);
        dialog.showAndWait();

        String printer = controller.getPrinter();
        if (!StringUtils.isBlank(printer)) {
            setPrinter(printer);
        }

    }

    private void setPrinter(String printerName) {
        lblPrinter.setText(printerName);
    }
}
