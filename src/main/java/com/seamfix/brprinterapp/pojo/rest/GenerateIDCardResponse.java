package com.seamfix.brprinterapp.pojo.rest;

import com.seamfix.brprinterapp.pojo.GenerateIDCard;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class GenerateIDCardResponse extends Response {

    private String frontView;
    private String backView;

    public GenerateIDCardResponse(int code, String description) {
        super(code, description);
    }


}
