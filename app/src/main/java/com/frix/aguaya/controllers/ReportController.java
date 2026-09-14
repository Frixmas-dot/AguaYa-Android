package com.frix.aguaya.controllers;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ReportController {
    // IMPORTANTE: En el emulador de Android usa "10.0.2.2" en la IP
    private static final String BASE_URL = "http://192.168.20.179:3001/";
    private static Retrofit retrofit;

    public static ApiService getApi() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}

