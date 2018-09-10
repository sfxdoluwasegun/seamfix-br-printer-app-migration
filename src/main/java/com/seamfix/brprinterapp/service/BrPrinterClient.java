package com.seamfix.brprinterapp.service;

import com.seamfix.brprinterapp.pojo.rest.*;
import retrofit2.Call;
import retrofit2.http.*;

/**
 * Created by rukevwe on 8/12/2018.
 */

public interface BrPrinterClient {

    @POST("crm/api/idcard/generate")
    Call<GenerateIDCardResponse> generateIDcard(@Body GenerateIDCardRequest generateIDCardRequest);

    @FormUrlEncoded
    @POST("access/api/login")
    Call<LoginResponse> userLogin(@Field(value = "email", encoded = true) String email, @Field("pw") String password);

    @FormUrlEncoded
    @POST("projects/api/config/tag")
    Call<TagResponse> tagDevice(@Field(value = "type", encoded = true) String deviceType, @Field("uniqueId") String uniqueId);

}
