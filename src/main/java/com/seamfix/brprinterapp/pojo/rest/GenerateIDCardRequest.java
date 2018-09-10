package com.seamfix.brprinterapp.pojo.rest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@AllArgsConstructor
public class GenerateIDCardRequest {

    private String pId;
    private ArrayList<String> uniqueIds;
}
