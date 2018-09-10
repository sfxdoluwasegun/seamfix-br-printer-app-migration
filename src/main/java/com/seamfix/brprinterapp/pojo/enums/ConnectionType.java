package com.seamfix.brprinterapp.pojo.enums;

public enum ConnectionType {

    HTTPS("https"), HTTP("http");

    private final String shortName;

    ConnectionType(String shortName) {
        this.shortName = shortName;
    }
}
