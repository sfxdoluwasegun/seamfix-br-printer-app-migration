package com.seamfix.brprinterapp.pojo.rest;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class TagResponse extends Response {

    private String tag;

    public TagResponse(int code, String description) {
        super(code, description);
    }
}
