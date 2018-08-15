package com.seamfix.brprinterapp.utils;


import com.seamfix.brprinterapp.config.AppConfig;
import com.seamfix.brprinterapp.controller.LandingPageController;
import com.seamfix.brprinterapp.model.Project;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Window;
import javafx.util.StringConverter;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;

import java.io.Closeable;
import java.io.InputStream;
import java.util.Base64;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Created by rukevwe on 8/8/2018.
 */
@Log4j
public class CommonUtils {
    public static String encodeBytesToBase64String(byte[] bytes) {
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(bytes);
    }

    public static byte[] decodeBase64StringToBytes(String string) {
        if (StringUtils.isBlank(string)) {
            return null;
        }

        Base64.Decoder decoder = Base64.getDecoder();
        return decoder.decode(string);
    }
    public static boolean isEmailValid(String email) {
        String regex = ".+@.+";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(email).matches();
    }
    public static String getBiosElement(String biosKey) {
        log.debug("Getting bios element " + biosKey);
        String biosValue = null;

        InputStream is = null;
        try {
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec(new String[]{"wmic", "bios", "get", biosKey});

            is = process.getInputStream();

            Scanner sc = new Scanner(is);
            while (sc.hasNext()) {
                String next = sc.next();
                if (biosKey.equalsIgnoreCase(next)) {
                    biosValue = sc.next().trim();
                    break;
                }
            }

        } catch (Exception e) {
            log.error("getBiosElement " + biosKey, e);
        } finally {
            closeStream(is);
        }
        log.debug("bioskey: " + biosKey + "; " + "biosValue " + biosValue);
        return biosValue;
    }
    public static String getMachineSerialNumber() {
        return getBiosElement("SerialNumber");
    }

    public static String getMachineManufacturer() {
        return getBiosElement("Manufacturer");
    }

    public static String getSerialNumberMachineManufacturer() {
        String serial = getMachineSerialNumber();
        String manufacturer = getMachineManufacturer();

        log.info("Serial: " + serial + ", Manufacturer: " + manufacturer);
        boolean validSerialAndManufacturer = StringUtils.isNotBlank(serial) && StringUtils.isNotBlank(manufacturer);
        if (validSerialAndManufacturer) {
            return manufacturer.toUpperCase() + "-" + serial.toUpperCase();
        } else {
            return "";
        }
    }
    private static void closeStream(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception e) {
            log.error("closeStream", e);
        }
    }

    public static String getPriProjectForLoggedInuserKey() {
        String email = SessionUtils.getLoggedInUserEmail();
        return email + ":PRIMARY_PROJECT";
    }

    public static Window getWindowForComponent(Node node) {
        if (node == null) {
            return null;
        }

        Scene scene = node.getScene();
        if (scene == null) {
            return null;
        }

        return scene.getWindow();
    }
    public static Window getMainWindow() {
        LandingPageController landingPageController = SessionUtils.getLandingPageController();
        if (landingPageController != null && landingPageController.getRoot() != null) {
            return getWindowForComponent(landingPageController.getRoot());
        }
        return null;
    }


    public static StringConverter<Project> getProjectStringConverter() {
        StringConverter<Project> converter = new StringConverter<Project>() {
            @Override
            public String toString(Project object) {
                return object == null ? null : object.getName();
            }

            @Override
            public Project fromString(String string) {
                return null;
            }
        };
        return converter;
    }
    public static String getPrimaryProjectForLoggedInuser() {
        return AppConfig.get(getPriProjectForLoggedInuserKey());
    }

}
