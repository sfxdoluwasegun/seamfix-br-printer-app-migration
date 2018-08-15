package com.seamfix.brprinterapp.config;

import lombok.Getter;

import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
public enum ConfigKeys {

    INSTALL_DATE(new SimpleDateFormat("dd MMMM yyyy HH:MM").format(new Date())),

    LAST_SUCCESSFUL_LOGIN("");

    private String defaultValue;

    ConfigKeys(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
