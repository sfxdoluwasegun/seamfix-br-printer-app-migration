package com.seamfix.brprinterapp.controller;

import com.google.gson.Gson;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import com.seamfix.brprinterapp.config.AppConfig;
import com.seamfix.brprinterapp.config.ConfigKeys;
import com.seamfix.brprinterapp.gui.BioPrinterDialog;
import com.seamfix.brprinterapp.gui.BioPrinterStage;
import com.seamfix.brprinterapp.model.BioUser;
import com.seamfix.brprinterapp.model.IdCard;
import com.seamfix.brprinterapp.model.Project;
import com.seamfix.brprinterapp.pojo.rest.LoginResponse;
import com.seamfix.brprinterapp.service.DataService;
import com.seamfix.brprinterapp.service.HttpClient;
import com.seamfix.brprinterapp.service.ServiceGenerator;
import com.seamfix.brprinterapp.utils.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Pair;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import javafx.scene.image.ImageView;

import java.net.URL;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Stream;

@Log4j
public class SignInController extends Controller {

    @FXML
    private Label lblVersion;
    @FXML
    private Label lblVendor;
    @FXML
    private JFXTextField txtUsername;
    @FXML
    private JFXPasswordField txtPassword;
    @FXML
    private Label lblMessage;
    @FXML
    private JFXCheckBox toRememberBox;
    @FXML
    private JFXButton btnSignIn;
    @FXML
    private ImageView imgStatus;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        //ensure all usernames are trimmed and in lowercase
        txtUsername.focusedProperty().addListener((observable, oldValue, newValue) -> {
            boolean lostFocus = BooleanUtils.isNotTrue(newValue);
            if (lostFocus) {
                txtUsername.setText(Optional.ofNullable(txtUsername.getText()).orElse("").toLowerCase().trim());
            }
        });
        lblVersion.setText("V" + AppConfig.getAppVersion());
        lblVendor.setTooltip(new Tooltip(AppConfig.getHttpIp() + "\n" + AppConfig.getFtpIp()));

        txtUsername.setText(AppConfig.get(ConfigKeys.LAST_SUCCESSFUL_LOGIN));
    }

    public void performSignIn(ActionEvent actionEvent) {
        log.info("performing sign in...");
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        log.debug("Signing in " + username + " with " + password);

        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            lblMessage.setText(getString("LoginStage.blankCredentialsMessage"));
            return;
        }

        if (!CommonUtils.isEmailValid(username)) {
            lblMessage.setText(getString("LoginStage.invalidEmail"));
            return;
        }

        Task<LoginResponse> onlineLoginTask = new Task<LoginResponse>() {
            @Override
            protected LoginResponse call() throws Exception {
                return new HttpClient(ServiceGenerator.getInstance()).userLogin(username, password);
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
                imgStatus.setImage(null);
                LoginResponse loginResponse = getValue();
                lblMessage.setText(loginResponse == null ? getString("LoginStage.defaultResponseMessage") : loginResponse.getDescription());

                if (loginResponse != null) {
                    int loginResponseCode = loginResponse.getCode();
                    boolean deactivatedUser = loginResponseCode == -4;
                    boolean expiredSubscriptionAccount = loginResponseCode == -8;

                    if (deactivatedUser) {
                        handleDeactivatedUser(username);
                        return;
                    }
                    if (expiredSubscriptionAccount) {
                        AlertUtils.getError("Your login failed because your account has expired. Kindly subscribe and try again.").showAndWait();
                        return;
                    }

                    boolean validUser = loginResponseCode == 0;
                    if (validUser) {
                        Boolean printIdCard = null;
                        BioUser user = loginResponse.getBioUser();
                        Set<Project> projects = user.getProjects();
                        for (Project project: projects) {
                            printIdCard = project.getAllowPrintIdCard();
                            if (printIdCard != null) {
                                if (printIdCard == true) {
                                    break;
                                }
                            }
                        }
                        if (printIdCard == null || printIdCard == false) {
                            AlertUtils.getError(getString("LoginStage.SignInUnauthorised.text")).show();
                            return;
                        }
                        BioUser bioUser = saveOrUpdateBioUserEntity(loginResponse.getBioUser(), password, loginResponse.isGdprCompliant());
                        performSuccessfulLoginAction(bioUser);

                    }

                }
            }

            @Override
            protected void done() {
                Platform.runLater(() -> txtUsername.requestFocus());
            }
        };


        lblMessage.visibleProperty().bind(onlineLoginTask.runningProperty().not());
        Node[] nods = {txtUsername, toRememberBox, txtPassword, btnSignIn};
        Arrays.stream(nods).forEach(node -> node.disableProperty().bind(onlineLoginTask.runningProperty()));
        new Thread(onlineLoginTask).start();
    }

    private void performSuccessfulLoginAction(BioUser user) {

        SessionUtils.setLoggedInUser(user);

        Window window = btnSignIn.getScene().getWindow();
        window.hide();

        if (toRememberBox.isSelected()) {
            AppConfig.saveOrUpdate(ConfigKeys.LAST_SUCCESSFUL_LOGIN, txtUsername.getText());
        } else {
            String lastLogin = AppConfig.get(ConfigKeys.LAST_SUCCESSFUL_LOGIN);
            if (StringUtils.equalsIgnoreCase(lastLogin, txtUsername.getText())) {
                AppConfig.saveOrUpdate(ConfigKeys.LAST_SUCCESSFUL_LOGIN, "");
            }
        }
        showLandingPage();
    }

    private void showLandingPage() {
        Pair<Parent, Controller> pair = FXMLUtil.loadParentControllerPair(FXMLUtil.LANDING_PAGE_FXML);
        Parent parent = pair.getKey();
        LandingPageController controller = (LandingPageController) pair.getValue();
        SessionUtils.setLandingPageController(controller);
        BioPrinterStage mainStage = new BioPrinterStage(new Scene(parent));
        String title = "BioRegistra Printer App";
        mainStage.setTitle(title);

        mainStage.setResizable(false);
        mainStage.setOnShown(event -> handleLandingStageShown(controller));
        mainStage.show();

        mainStage.setOnCloseRequest(this::handleLandingStageClosed);
    }

    private void handleLandingStageShown(LandingPageController landingPageController) {
        Set<Project> loggedInUserProjects = SessionUtils.getLoggedInUserProjects();
        if (loggedInUserProjects.isEmpty()) {
            return;
        }

        boolean singleProject = loggedInUserProjects.size() == 1;
        if (singleProject) {
            Project selectedProject = loggedInUserProjects.stream().findFirst().get();
            SessionUtils.setCurrentProject(selectedProject);
            AppConfig.saveOrUpdate(CommonUtils.getPriProjectForLoggedInuserKey(), selectedProject.getPId());
            landingPageController.setSelectedProject(selectedProject);
        } else {
            String primaryProjectPid = CommonUtils.getPrimaryProjectForLoggedInuser();
            if (StringUtils.isBlank(primaryProjectPid)) {
                //prompt user to select primary project
                Pair<Parent, Controller> pair = FXMLUtil.loadParentControllerPair(FXMLUtil.SELECT_DEFAULT_PROJECT_FXML);
                SelectDefaultProjectAndPrinterController controller = (SelectDefaultProjectAndPrinterController) pair.getValue();
                Parent parent = pair.getKey();
                BioPrinterDialog dialog = new BioPrinterDialog(CommonUtils.getMainWindow(), new Scene(parent), "Select primary project", false);
                dialog.showAndWait();

                landingPageController.setSelectedPrinterName(controller.getPrinterName());
                landingPageController.setSelectedProject(controller.getSelectedProject());
            }
        }
    }

    private void handleLandingStageClosed(WindowEvent event) {
        Optional<ButtonType> buttonType = AlertUtils.getConfirm("Are you sure you want to exit?").showAndWait();
        if (buttonType.get() != ButtonType.OK) {
            event.consume();
        } else {
            System.exit(0);
        }
    }


    public BioUser saveOrUpdateBioUserEntity(BioUser bioUserFromServer, String password, boolean gdprCompliant) {
        DataService ds = DataService.getInstance();

        String email = bioUserFromServer.getEmail();
        BioUser existingBioUser = ds.getBioUserByUsername(email);
        boolean userExistsLocally = existingBioUser != null;
        log.debug("User exists locally: " + userExistsLocally);
        if (userExistsLocally) {
            //update
            BioUser user = handleCachedUser(existingBioUser, bioUserFromServer, password, gdprCompliant);
            return user;
        } else {
            //create
            BioUser user = handleNewUser(bioUserFromServer, password, gdprCompliant);
            return user;
        }
    }

    private void handleDeactivatedUser(String username) {
        DataService ds = DataService.getInstance();

        BioUser existingUser = ds.getBioUserByUsername(username);
        if (existingUser != null) {
            existingUser.setActive(false);
            ds.createOrUpdate(existingUser);
        }
    }

    private BioUser handleNewUser(BioUser bioUserFromServer, String password, boolean gdprCompliant) {
        DataService ds = DataService.getInstance();
        Set<Project> projects = new HashSet<>();
        for (Project project : bioUserFromServer.getProjects()) {
            Project existingProject = ds.getProjectByPid(project.getPId());
            if (existingProject != null) {
                Long id = existingProject.getId();
                existingProject = project;
                existingProject.setId(id);
                ds.createOrUpdate(existingProject);
                projects.add(existingProject);
            } else {
                ds.create(project);
                projects.add(project);
            }
        }
        bioUserFromServer.setProjects(projects);
        bioUserFromServer.setPw(password);
        bioUserFromServer.setGdprCompliant(gdprCompliant);

        ds.create(bioUserFromServer);

        return bioUserFromServer;
    }

    private BioUser handleCachedUser(BioUser existingBioUser, BioUser bioUserFromServer, String password, boolean gdprCompliant) {
        DataService ds = DataService.getInstance();

        Long bId = existingBioUser.getId();
        Set<Project> existingBioUserProjects = existingBioUser.getProjects();

        existingBioUser = new Gson().fromJson(new Gson().toJson(bioUserFromServer), BioUser.class); //clone
        existingBioUser.setPw(password);

        existingBioUser.setId(bId);
        existingBioUser.setProjects(existingBioUserProjects);
        existingBioUser.setGdprCompliant(gdprCompliant);

        ds.createOrUpdate(existingBioUser);

        existingBioUserProjects.removeIf(projectEntity -> {
            boolean obsolete = !bioUserFromServer.getProjects().contains(projectEntity);
            log.debug(projectEntity.getPId() + "is obsolete: " + obsolete);
            return obsolete;
        });

        Set<Project> projects = new HashSet<>();
        for (Project project : bioUserFromServer.getProjects()) {
            Project existingProject = ds.getProjectByPid(project.getPId());
            if (existingProject != null) {
                Long id = existingProject.getId();
                byte[] configBytes = existingProject.getConfigBytes();

                existingProject = new Gson().fromJson(new Gson().toJson(project), Project.class);

                existingProject.setId(id);
                existingProject.setConfigBytes(configBytes);

                ds.createOrUpdate(existingProject);
                projects.add(existingProject);
            } else {
                ds.create(project);
                projects.add(project);
            }
        }

        existingBioUser.setProjects(projects);
        ds.createOrUpdate(existingBioUser);

        return existingBioUser;

    }

}