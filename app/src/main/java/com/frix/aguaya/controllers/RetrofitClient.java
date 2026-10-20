package com.frix.aguaya.controllers;

import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.Buffer;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String TAG = "RetrofitClient";
    // URL base de la API backend. Modificar según sea necesario.
    // Para Emulador usa "http://10.0.2.2:3001/"
    // IP del Ubuntu Server en VirtualBox (Adaptador Puente)
    private static final String BASE_URL = "http://192.168.20.229:3001/";
    private static final String WEATHER_BASE_URL = "https://api.open-meteo.c" +
            "om/";

    private static Retrofit retrofit = null;
    private static Retrofit weatherRetrofit = null;

    private static OkHttpClient getClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(6, TimeUnit.SECONDS)
                .readTimeout(8, TimeUnit.SECONDS)
                .writeTimeout(8, TimeUnit.SECONDS)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();
                        Log.d(TAG, "=== REQUEST ===");
                        Log.d(TAG, "URL: " + request.url());
                        Log.d(TAG, "Method: " + request.method());
                        if (request.body() != null) {
                            Log.d(TAG, "Content-Type: " + request.body().contentType());
                            Log.d(TAG, "Content-Length: " + request.body().contentLength());
                            if (request.body().contentLength() < 10000) {
                                try {
                                    Buffer buffer = new Buffer();
                                    request.body().writeTo(buffer);
                                    String body = buffer.readUtf8();
                                    Log.d(TAG, "Body: " + body.substring(0, Math.min(body.length(), 2000)));
                                } catch (Exception e) {
                                    Log.d(TAG, "Could not read body: " + e.getMessage());
                                }
                            }
                        }
                        Response response = chain.proceed(request);
                        Log.d(TAG, "=== RESPONSE ===");
                        Log.d(TAG, "Code: " + response.code());
                        return response;
                    }
                })
                .build();
    }

    public static ApiService getApi() {
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(getClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(ApiService.class);
    }

    public static WeatherApiService getWeatherApi() {
        if (weatherRetrofit == null) {
            weatherRetrofit = new Retrofit.Builder()
                    .baseUrl(WEATHER_BASE_URL)
                    .client(getClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return weatherRetrofit.create(WeatherApiService.class);
    }
}
