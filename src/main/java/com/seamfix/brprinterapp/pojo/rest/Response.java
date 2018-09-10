package com.seamfix.brprinterapp.pojo.rest;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public abstract class Response {
    protected int code;
    protected String description;

    public Response(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public String concat() {
        return description + ": " + code;
    }
}
