package com.seamfix.brprinterapp.pojo.rest;


import com.seamfix.brprinterapp.model.BioUser;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse extends Response {

    private BioUser bioUser;

    private boolean gdprCompliant;

    private boolean alreadyLoggedIn;

    public LoginResponse(int code, String description) {
        super(code, description);
    }
}
