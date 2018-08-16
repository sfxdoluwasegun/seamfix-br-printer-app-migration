package com.seamfix.brprinterapp.service;

import com.seamfix.brprinterapp.pojo.GenerateIDCard;
import com.seamfix.brprinterapp.pojo.rest.GenerateIDCardRequest;
import com.seamfix.brprinterapp.pojo.rest.GenerateIDCardResponse;
import com.seamfix.brprinterapp.pojo.rest.LoginResponse;
import com.seamfix.brprinterapp.pojo.rest.TagResponse;
import retrofit2.Call;
import retrofit2.http.*;

/**
 * Created by rukevwe on 8/12/2018.
 */

public interface BrPrinterClient {

    @FormUrlEncoded
    @POST("access/api/login")
    Call<LoginResponse> userLogin(@Field("email") String email, @Field("pw") String password);

    @POST("idcard/generate")
    Call<GenerateIDCardResponse> generateIDcard(@Body GenerateIDCardRequest generateIDCardRequest);


    @FormUrlEncoded
    @POST("projects/api/config/tag")
    Call<TagResponse> tagDevice(@Field("type") String deviceType, @Field("uniqueId") String uniqueId);

}
