package com.seamfix.brprinterapp.utils;

import javafx.scene.image.Image;

public class ImageHelper {


    private static final String FORMAT_JPG = "JPG";
    private static final String FORMAT_BMP = "BMP";

    public static final String SEAMFIX_LOGO_SEC = "/com/seamfix/brprinterapp/img/seamfix-logo-sec.png";
    public static final String APPLICATION_ICON = "/com/seamfix/brprinterapp/img/bioregistra-icon.png";

    public static final String LOADING_IMAGE = "/com/seamfix/brprinterapp/img/loading.gif";
    public static final String ERROR_IMAGE = "/com/seamfix/brprinterapp/img/error-icon.png";
    public static final String OK_IMAGE = "/com/seamfix/brprinterapp/img/ok-icon.png";


    public static Image getApplicationIconImage() {
        return getImage(APPLICATION_ICON);
    }

    public static Image getImage(String resource) {
        return new Image(ImageHelper.class.getResource(resource).toExternalForm());
    }
    public static Image getErrorImage(){
        return getImage(ERROR_IMAGE);
    }
    public static Image getLoadingImage(){
        return getImage(LOADING_IMAGE);
    }
    public static Image getOKImage(){
        return getImage(OK_IMAGE);
    }

}
